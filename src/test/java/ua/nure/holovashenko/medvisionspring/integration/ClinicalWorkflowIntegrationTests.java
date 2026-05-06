package ua.nure.holovashenko.medvisionspring.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import ua.nure.holovashenko.medvisionspring.entity.Doctor;
import ua.nure.holovashenko.medvisionspring.entity.Hospital;
import ua.nure.holovashenko.medvisionspring.entity.ImageAnalysis;
import ua.nure.holovashenko.medvisionspring.entity.Patient;
import ua.nure.holovashenko.medvisionspring.entity.Treatment;
import ua.nure.holovashenko.medvisionspring.entity.User;
import ua.nure.holovashenko.medvisionspring.entity.UserHospitalMembership;
import ua.nure.holovashenko.medvisionspring.enums.AnalysisJobStatus;
import ua.nure.holovashenko.medvisionspring.enums.Gender;
import ua.nure.holovashenko.medvisionspring.enums.HospitalRole;
import ua.nure.holovashenko.medvisionspring.enums.HospitalStatus;
import ua.nure.holovashenko.medvisionspring.enums.MembershipStatus;
import ua.nure.holovashenko.medvisionspring.enums.TreatmentStatus;
import ua.nure.holovashenko.medvisionspring.enums.UserRole;
import ua.nure.holovashenko.medvisionspring.repository.DoctorRepository;
import ua.nure.holovashenko.medvisionspring.repository.HospitalRepository;
import ua.nure.holovashenko.medvisionspring.repository.ImageAnalysisRepository;
import ua.nure.holovashenko.medvisionspring.repository.PatientRepository;
import ua.nure.holovashenko.medvisionspring.repository.TreatmentRepository;
import ua.nure.holovashenko.medvisionspring.repository.UserHospitalMembershipRepository;
import ua.nure.holovashenko.medvisionspring.repository.UserRepository;
import ua.nure.holovashenko.medvisionspring.svm.DiagnosisInfo;
import ua.nure.holovashenko.medvisionspring.svm.ImageUtils;
import ua.nure.holovashenko.medvisionspring.svm.MetricsCalculator;
import ua.nure.holovashenko.medvisionspring.svm.ModelMetrics;
import ua.nure.holovashenko.medvisionspring.svm.SvmClassificationResult;
import ua.nure.holovashenko.medvisionspring.svm.SvmClient;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClinicalWorkflowIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private UserHospitalMembershipRepository membershipRepository;

    @Autowired
    private TreatmentRepository treatmentRepository;

    @Autowired
    private ImageAnalysisRepository imageAnalysisRepository;

    @MockBean
    private SvmClient svmClient;

    @MockBean
    private ImageUtils imageUtils;

    @BeforeEach
    void setUp() throws Exception {
        MetricsCalculator.ClassMetrics classMetrics = new MetricsCalculator.ClassMetrics(0.91, 0.89, 0.90);
        when(svmClient.classify(any())).thenReturn(new SvmClassificationResult(
                2,
                new DiagnosisInfo("SVM analysis details", "Pneumonia pattern", "Review with radiologist"),
                new ModelMetrics(0.96, new int[][]{{1, 0}, {0, 1}}, Map.of(2, classMetrics)),
                classMetrics,
                new Mat()
        ));
        doNothing().when(imageUtils).saveMatToFile(nullable(Mat.class), any(File.class));
    }

    @Test
    void hospitalTreatmentAnalysisJobAndModelEndpointsWorkThroughMockMvc() throws Exception {
        String suffix = uniqueSuffix();
        User admin = saveUser("Admin " + suffix, "admin-" + suffix + "@example.com", UserRole.ADMIN);
        User doctor = saveDoctor("Doctor " + suffix, "doctor-" + suffix + "@example.com");
        User patient = savePatient("Patient " + suffix, "patient-" + suffix + "@example.com");

        Long hospitalId = createHospital(admin, "Integration Clinic " + suffix, "INT-" + suffix);

        addMembership(admin, hospitalId, doctor.getUserId(), HospitalRole.DOCTOR);
        addMembership(admin, hospitalId, patient.getUserId(), HospitalRole.PATIENT);

        mockMvc.perform(post("/api/hospitals/{hospitalId}/select", hospitalId)
                        .with(user(doctor.getEmail()).roles("DOCTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hospitalId").value(hospitalId))
                .andExpect(jsonPath("$.selected").value(true));

        String treatmentJson = """
                {
                  "patientId": %d,
                  "title": "Initial pneumonia treatment",
                  "description": "Full patient examination flow"
                }
                """.formatted(patient.getUserId());

        String treatmentBody = mockMvc.perform(post("/api/hospitals/{hospitalId}/treatments", hospitalId)
                        .with(user(doctor.getEmail()).roles("DOCTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(treatmentJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hospitalId").value(hospitalId))
                .andExpect(jsonPath("$.patientId").value(patient.getUserId()))
                .andExpect(jsonPath("$.primaryDoctorId").value(doctor.getUserId()))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long treatmentId = objectMapper.readTree(treatmentBody).get("treatmentId").asLong();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "scan.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake-scan-content".getBytes()
        );

        String analysisBody = mockMvc.perform(multipart(
                                "/api/hospitals/{hospitalId}/treatments/{treatmentId}/analyses",
                                hospitalId,
                                treatmentId
                        )
                        .file(file)
                        .with(user(doctor.getEmail()).roles("DOCTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hospitalId").value(hospitalId))
                .andExpect(jsonPath("$.treatmentId").value(treatmentId))
                .andExpect(jsonPath("$.jobId").exists())
                .andExpect(jsonPath("$.diagnosisClass").value(2))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long jobId = objectMapper.readTree(analysisBody).get("jobId").asLong();

        mockMvc.perform(get("/api/hospitals/{hospitalId}/analysis-jobs/{jobId}", hospitalId, jobId)
                        .with(user(doctor.getEmail()).roles("DOCTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(jobId))
                .andExpect(jsonPath("$.hospitalId").value(hospitalId))
                .andExpect(jsonPath("$.treatmentId").value(treatmentId))
                .andExpect(jsonPath("$.analysisId").exists())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        String modelsBody = mockMvc.perform(get("/api/svm/models")
                        .with(user(admin.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.version == 'full-linear-legacy')]").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode fullImageModel = objectMapper.readTree(modelsBody).findValues("version").stream()
                .filter(node -> node.asText().equals("full-linear-legacy"))
                .findFirst()
                .orElseThrow();
        assertThat(fullImageModel.asText()).isEqualTo("full-linear-legacy");

        Mockito.verify(svmClient).classify(any());
    }

    @Test
    void legacyAnalyzeEndpointCreatesLegacyTreatmentAndCompletedJob() throws Exception {
        String suffix = uniqueSuffix();
        User doctor = saveDoctor("Legacy Doctor " + suffix, "legacy-doctor-" + suffix + "@example.com");
        User patient = savePatient("Legacy Patient " + suffix, "legacy-patient-" + suffix + "@example.com");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "legacy-scan.png",
                MediaType.IMAGE_PNG_VALUE,
                "legacy-fake-scan-content".getBytes()
        );

        mockMvc.perform(multipart("/api/doctor/images/analyze")
                        .file(file)
                        .param("patientId", patient.getUserId().toString())
                        .param("doctorId", doctor.getUserId().toString())
                        .with(user(doctor.getEmail()).roles("DOCTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.startsWith("Analysis saved. ID:")));

        Hospital legacyHospital = hospitalRepository.findByCode("LEGACY").orElseThrow();
        Treatment legacyTreatment = treatmentRepository.findByHospitalAndPatientAndPrimaryDoctorAndStatus(
                legacyHospital,
                patient,
                doctor,
                TreatmentStatus.OPEN
        ).orElseThrow();

        assertThat(legacyTreatment.getTitle()).isEqualTo("Legacy treatment");

        ImageAnalysis analysis = imageAnalysisRepository.findAllByTreatmentOrderByCreationDatetimeAsc(legacyTreatment)
                .stream()
                .findFirst()
                .orElseThrow();

        assertThat(analysis.getHospital().getHospitalId()).isEqualTo(legacyHospital.getHospitalId());
        assertThat(analysis.getAnalysisJob()).isNotNull();
        assertThat(analysis.getAnalysisJob().getStatus()).isEqualTo(AnalysisJobStatus.COMPLETED);
        assertThat(analysis.getModelVersion()).isNotNull();
        assertThat(analysis.getModelVersion().getVersion()).isEqualTo("full-linear-legacy");
    }

    @Test
    void doctorCannotReadOtherHospitalTreatmentOrJob() throws Exception {
        String suffix = uniqueSuffix();
        User doctorA = saveDoctor("Doctor A " + suffix, "doctor-a-" + suffix + "@example.com");
        User patientA = savePatient("Patient A " + suffix, "patient-a-" + suffix + "@example.com");
        User doctorB = saveDoctor("Doctor B " + suffix, "doctor-b-" + suffix + "@example.com");
        User patientB = savePatient("Patient B " + suffix, "patient-b-" + suffix + "@example.com");

        Hospital hospitalA = saveHospital("Clinic A " + suffix, "A-" + suffix);
        Hospital hospitalB = saveHospital("Clinic B " + suffix, "B-" + suffix);

        saveMembership(doctorA, hospitalA, HospitalRole.DOCTOR);
        saveMembership(patientA, hospitalA, HospitalRole.PATIENT);
        saveMembership(doctorB, hospitalB, HospitalRole.DOCTOR);
        saveMembership(patientB, hospitalB, HospitalRole.PATIENT);

        Treatment treatmentB = treatmentRepository.save(Treatment.builder()
                .hospital(hospitalB)
                .patient(patientB)
                .primaryDoctor(doctorB)
                .title("Private treatment")
                .description("Other hospital case")
                .status(TreatmentStatus.OPEN)
                .openedAt(LocalDateTime.now())
                .createdBy(doctorB)
                .build());

        mockMvc.perform(get("/api/hospitals/{hospitalId}/treatments/{treatmentId}",
                                hospitalB.getHospitalId(),
                                treatmentB.getTreatmentId()
                        )
                        .with(user(doctorA.getEmail()).roles("DOCTOR")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("Немає доступу до цієї лікарні"))
                .andExpect(jsonPath("$.path").value("/api/hospitals/" + hospitalB.getHospitalId()
                        + "/treatments/" + treatmentB.getTreatmentId()));

        mockMvc.perform(get("/api/hospitals/{hospitalId}/treatments",
                                hospitalB.getHospitalId()
                        )
                        .with(user(doctorA.getEmail()).roles("DOCTOR")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("Немає доступу до цієї лікарні"));
    }

    @Test
    void treatmentDynamicsEndpointsPersistComparisonConclusionAndReport() throws Exception {
        String suffix = uniqueSuffix();
        User admin = saveUser("Dynamics Admin " + suffix, "dynamics-admin-" + suffix + "@example.com", UserRole.ADMIN);
        User doctor = saveDoctor("Dynamics Doctor " + suffix, "dynamics-doctor-" + suffix + "@example.com");
        User patient = savePatient("Dynamics Patient " + suffix, "dynamics-patient-" + suffix + "@example.com");

        Long hospitalId = createHospital(admin, "Dynamics Clinic " + suffix, "DYN-" + suffix);
        addMembership(admin, hospitalId, doctor.getUserId(), HospitalRole.DOCTOR);
        addMembership(admin, hospitalId, patient.getUserId(), HospitalRole.PATIENT);

        String treatmentBody = mockMvc.perform(post("/api/hospitals/{hospitalId}/treatments", hospitalId)
                        .with(user(doctor.getEmail()).roles("DOCTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "patientId": %d,
                                  "title": "Dynamic observation",
                                  "description": "Two analyses and conclusion"
                                }
                                """.formatted(patient.getUserId())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long treatmentId = objectMapper.readTree(treatmentBody).get("treatmentId").asLong();
        Long firstAnalysisId = uploadAnalysis(hospitalId, treatmentId, doctor, "first-scan.png");
        Long secondAnalysisId = uploadAnalysis(hospitalId, treatmentId, doctor, "second-scan.png");

        String comparisonBody = mockMvc.perform(post(
                                "/api/hospitals/{hospitalId}/treatments/{treatmentId}/comparisons",
                                hospitalId,
                                treatmentId
                        )
                        .with(user(doctor.getEmail()).roles("DOCTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromAnalysisId": %d,
                                  "toAnalysisId": %d,
                                  "doctorNotes": "No negative dynamics"
                                }
                                """.formatted(firstAnalysisId, secondAnalysisId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.treatmentId").value(treatmentId))
                .andExpect(jsonPath("$.fromAnalysisId").value(firstAnalysisId))
                .andExpect(jsonPath("$.toAnalysisId").value(secondAnalysisId))
                .andExpect(jsonPath("$.diagnosisChanged").value(false))
                .andExpect(jsonPath("$.doctorNotes").value("No negative dynamics"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long comparisonId = objectMapper.readTree(comparisonBody).get("comparisonId").asLong();

        mockMvc.perform(put("/api/hospitals/{hospitalId}/treatments/{treatmentId}/conclusion", hospitalId, treatmentId)
                        .with(user(doctor.getEmail()).roles("DOCTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "conclusionText": "Patient condition is stable after repeated SVM-supported analyses.",
                                  "recommendations": "Continue observation and repeat CT only if symptoms worsen."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.treatmentId").value(treatmentId))
                .andExpect(jsonPath("$.conclusionText").value("Patient condition is stable after repeated SVM-supported analyses."));

        mockMvc.perform(get("/api/hospitals/{hospitalId}/treatments/{treatmentId}/report", hospitalId, treatmentId)
                        .with(user(patient.getEmail()).roles("PATIENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.treatment.treatmentId").value(treatmentId))
                .andExpect(jsonPath("$.analyses.length()").value(2))
                .andExpect(jsonPath("$.comparisons[0].comparisonId").value(comparisonId))
                .andExpect(jsonPath("$.conclusion.recommendations").value("Continue observation and repeat CT only if symptoms worsen."));

        mockMvc.perform(post(
                                "/api/hospitals/{hospitalId}/treatments/{treatmentId}/comparisons",
                                hospitalId,
                                treatmentId
                        )
                        .with(user(patient.getEmail()).roles("PATIENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromAnalysisId": %d,
                                  "toAnalysisId": %d,
                                  "doctorNotes": "Patient should not write comparison"
                                }
                                """.formatted(firstAnalysisId, secondAnalysisId)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("Пацієнт не може змінювати динаміку лікування"))
                .andExpect(jsonPath("$.path").value("/api/hospitals/" + hospitalId
                        + "/treatments/" + treatmentId + "/comparisons"));
    }

    private Long createHospital(User admin, String name, String code) throws Exception {
        String hospitalJson = """
                {
                  "name": "%s",
                  "legalName": "%s LLC",
                  "code": "%s",
                  "address": "Kharkiv",
                  "phone": "+380000000000",
                  "email": "clinic@example.com"
                }
                """.formatted(name, name, code);

        String body = mockMvc.perform(post("/api/admin/hospitals")
                        .with(user(admin.getEmail()).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(hospitalJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.code").value(code.toUpperCase()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(body).get("hospitalId").asLong();
    }

    private void addMembership(User admin, Long hospitalId, Long userId, HospitalRole role) throws Exception {
        String membershipJson = """
                {
                  "userId": %d,
                  "hospitalRole": "%s"
                }
                """.formatted(userId, role.name());

        mockMvc.perform(post("/api/hospitals/{hospitalId}/memberships", hospitalId)
                        .with(user(admin.getEmail()).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(membershipJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.hospitalRole").value(role.name()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    private Long uploadAnalysis(Long hospitalId, Long treatmentId, User doctor, String fileName) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                fileName,
                MediaType.IMAGE_PNG_VALUE,
                ("fake-" + fileName).getBytes()
        );

        String body = mockMvc.perform(multipart(
                                "/api/hospitals/{hospitalId}/treatments/{treatmentId}/analyses",
                                hospitalId,
                                treatmentId
                        )
                        .file(file)
                        .with(user(doctor.getEmail()).roles("DOCTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisId").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(body).get("analysisId").asLong();
    }

    private User saveDoctor(String name, String email) {
        return transactionTemplate.execute(status -> {
            User user = saveUser(name, email, UserRole.DOCTOR);
            doctorRepository.save(Doctor.builder()
                    .user(user)
                    .position("Pulmonologist")
                    .department("Diagnostics")
                    .licenseNumber("LIC-" + user.getUserId())
                    .build());
            return user;
        });
    }

    private User savePatient(String name, String email) {
        return transactionTemplate.execute(status -> {
            User user = saveUser(name, email, UserRole.PATIENT);
            patientRepository.save(Patient.builder()
                    .user(user)
                    .gender(Gender.MALE)
                    .build());
            return user;
        });
    }

    private User saveUser(String name, String email, UserRole role) {
        return userRepository.save(User.builder()
                .userName(name)
                .email(email)
                .pw("password")
                .userRole(role)
                .creationDatetime(LocalDateTime.now())
                .build());
    }

    private Hospital saveHospital(String name, String code) {
        return hospitalRepository.save(Hospital.builder()
                .name(name)
                .legalName(name + " LLC")
                .code(code.toUpperCase())
                .status(HospitalStatus.ACTIVE)
                .build());
    }

    private void saveMembership(User user, Hospital hospital, HospitalRole role) {
        membershipRepository.save(UserHospitalMembership.builder()
                .user(user)
                .hospital(hospital)
                .hospitalRole(role)
                .status(MembershipStatus.ACTIVE)
                .build());
    }

    private String uniqueSuffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
