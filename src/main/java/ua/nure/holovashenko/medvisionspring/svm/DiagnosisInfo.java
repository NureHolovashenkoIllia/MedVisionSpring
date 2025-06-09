package ua.nure.holovashenko.medvisionspring.svm;

public class DiagnosisInfo {
    private final String analysisDetails;
    private final String analysisDiagnosis;
    private final String treatmentRecommendations;

    public DiagnosisInfo(String analysisDetails, String analysisDiagnosis, String treatmentRecommendations) {
        this.analysisDetails = analysisDetails;
        this.analysisDiagnosis = analysisDiagnosis;
        this.treatmentRecommendations = treatmentRecommendations;
    }

    public DiagnosisInfo() {
        this.analysisDetails = "Щось пішло не так";
        this.analysisDiagnosis = "Не вдалося точно визначити діагноз — результат невідомий.";
        this.treatmentRecommendations = "Повторіть спробу.";
    }

    public String getAnalysisDetails() {
        return analysisDetails;
    }

    public String getAnalysisDiagnosis() {
        return analysisDiagnosis;
    }

    public String getTreatmentRecommendations() {
        return treatmentRecommendations;
    }
}
