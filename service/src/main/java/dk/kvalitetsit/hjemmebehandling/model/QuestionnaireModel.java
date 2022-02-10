package dk.kvalitetsit.hjemmebehandling.model;

import java.util.List;

public class QuestionnaireModel extends BaseModel {
    private String title;
    private String status;
    private List<QuestionModel> questions;
    private List<QuestionModel> callToActions;

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

    public List<QuestionModel> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionModel> questions) {
        this.questions = questions;
    }

    public List<QuestionModel> getCallToActions() {
        return callToActions;
    }

    public void setCallToActions(List<QuestionModel> callToActions) {
        this.callToActions = callToActions;
    }
}
