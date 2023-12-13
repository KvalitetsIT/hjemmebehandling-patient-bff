package dk.kvalitetsit.hjemmebehandling.api;

import java.util.List;

public class QuestionnaireDto extends BaseDto {
    private String title;
    private String status;
    private List<QuestionDto> questions;

    /**
     * Some blob of html specifying the phone hours and other details associated to the contact of the organisation
     */
    private String blob;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<QuestionDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionDto> questions) {
        this.questions = questions;
    }

    public String getBlob() {
        return blob;
    }

    public void setBlob(String blob) {
        this.blob = blob;
    }
}
