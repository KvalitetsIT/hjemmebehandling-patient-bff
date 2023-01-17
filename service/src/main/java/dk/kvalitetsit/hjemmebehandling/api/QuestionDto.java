package dk.kvalitetsit.hjemmebehandling.api;

import dk.kvalitetsit.hjemmebehandling.constants.QuestionType;
import dk.kvalitetsit.hjemmebehandling.model.QuestionModel;

import java.util.List;

public class QuestionDto {
    private String linkId;
    private String text;
    private String abbreviation;
    private boolean required;
    private QuestionType questionType;
    private List<String> options;
    private String helperText;
    private List<QuestionModel.EnableWhen> enableWhens;

    private MeasurementTypeDto measurementType;

    private boolean deprecated;

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public String getLinkId() {
        return linkId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public boolean isRequired() {
        return required;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean getRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public void setHelperText(String helperText) {
        this.helperText = helperText;
    }

    public String getHelperText() {
        return helperText;
    }

    public List<QuestionModel.EnableWhen> getEnableWhens() {
        return enableWhens;
    }

    public void setEnableWhens(List<QuestionModel.EnableWhen> enableWhens) {
        this.enableWhens = enableWhens;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public MeasurementTypeDto getMeasurementType() {
        return measurementType;
    }

    public void setMeasurementType(MeasurementTypeDto measurementType) {
        this.measurementType = measurementType;
    }
}
