package ua.nure.holovashenko.medvisionspring.service;

import org.junit.jupiter.api.Test;
import ua.nure.holovashenko.medvisionspring.entity.AnalysisJob;
import ua.nure.holovashenko.medvisionspring.enums.AnalysisJobStatus;
import ua.nure.holovashenko.medvisionspring.repository.AnalysisJobRepository;
import ua.nure.holovashenko.medvisionspring.repository.HospitalRepository;
import ua.nure.holovashenko.medvisionspring.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnalysisJobServiceTests {

    private final AnalysisJobRepository analysisJobRepository = mock(AnalysisJobRepository.class);
    private final HospitalRepository hospitalRepository = mock(HospitalRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final HospitalAccessPolicy accessPolicy = mock(HospitalAccessPolicy.class);

    private final AnalysisJobService analysisJobService = new AnalysisJobService(
            analysisJobRepository,
            hospitalRepository,
            userRepository,
            accessPolicy
    );

    @Test
    void failJobMarksJobAsFailedAndStoresErrorMessage() {
        AnalysisJob job = AnalysisJob.builder()
                .analysisJobId(1L)
                .status(AnalysisJobStatus.PROCESSING)
                .build();

        when(analysisJobRepository.save(any(AnalysisJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AnalysisJob failed = analysisJobService.failJob(job, "SVM unavailable");

        assertThat(failed.getStatus()).isEqualTo(AnalysisJobStatus.FAILED);
        assertThat(failed.getErrorMessage()).isEqualTo("SVM unavailable");
        assertThat(failed.getFinishedAt()).isNotNull();
    }
}
