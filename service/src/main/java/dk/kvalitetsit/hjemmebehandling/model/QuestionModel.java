package dk.kvalitetsit.hjemmebehandling.model;

import dk.kvalitetsit.hjemmebehandling.constants.EnableWhenOperator;
import dk.kvalitetsit.hjemmebehandling.constants.QuestionType;

import java.util.List;

public class QuestionModel {
    private String linkId;
    private String text;
    private String abbreviation;
    private boolean required;
    private QuestionType questionType;
    private MeasurementTypeModel measurementType;
    private List<Option> options;
    private String helperText;
    private List<EnableWhen> enableWhens;
    private List<QuestionModel> subQuestions;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isRequired() {
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

    public MeasurementTypeModel getMeasurementType() {
        return measurementType;
    }

    public void setMeasurementType(MeasurementTypeModel measurementType) {
        this.measurementType = measurementType;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    public void setHelperText(String helperText) {
        this.helperText = helperText;
    }

    public String getHelperText() {
        return helperText;
    }

    public List<EnableWhen> getEnableWhens() {
        return enableWhens;
    }

    public void setEnableWhens(List<EnableWhen> enableWhens) {
        this.enableWhens = enableWhens;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public List<QuestionModel> getSubQuestions() {
        return subQuestions;
    }

    public void setSubQuestions(List<QuestionModel> subQuestions) {
        this.subQuestions = subQuestions;
    }

    public static class EnableWhen {
        private AnswerModel answer; // contains linkId for another question and desired answer[type,value]
        private EnableWhenOperator operator;

        public AnswerModel getAnswer() {
            return answer;
        }

        public void setAnswer(AnswerModel answer) {
            this.answer = answer;
        }

        public EnableWhenOperator getOperator() {
            return operator;
        }

        public void setOperator(EnableWhenOperator operator) {
            this.operator = operator;
        }
    }
}
