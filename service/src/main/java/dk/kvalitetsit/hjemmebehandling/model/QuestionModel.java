package dk.kvalitetsit.hjemmebehandling.model;

import dk.kvalitetsit.hjemmebehandling.constants.EnableWhenOperator;
import dk.kvalitetsit.hjemmebehandling.constants.QuestionType;

import java.util.List;

public class QuestionModel {
    private String linkId;
    private String text;
    private boolean required;
    private QuestionType questionType;
    private List<String> options;
    private String helperText;
    private List<EnableWhen> enableWhens;

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

    public List<EnableWhen> getEnableWhens() {
        return enableWhens;
    }

    public void setEnableWhens(List<EnableWhen> enableWhens) {
        this.enableWhens = enableWhens;
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
