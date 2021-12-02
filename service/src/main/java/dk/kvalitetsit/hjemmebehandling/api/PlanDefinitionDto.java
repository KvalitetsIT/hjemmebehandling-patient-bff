package dk.kvalitetsit.hjemmebehandling.api;

import java.util.List;

public class PlanDefinitionDto extends BaseDto {
    private String name;
    private String title;
    private List<QuestionnaireWrapperDto> questionnaires;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<QuestionnaireWrapperDto> getQuestionnaires() {
        return questionnaires;
    }

    public void setQuestionnaires(List<QuestionnaireWrapperDto> questionnaires) {
        this.questionnaires = questionnaires;
    }
}
