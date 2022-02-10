package dk.kvalitetsit.hjemmebehandling.model;

import dk.kvalitetsit.hjemmebehandling.constants.AnswerType;

import java.util.Objects;

public class AnswerModel {
    private String linkId;
    private String value;
    private AnswerType answerType;

    public String getLinkId() {
        return linkId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public AnswerType getAnswerType() {
        return answerType;
    }

    public void setAnswerType(AnswerType answerType) {
        this.answerType = answerType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnswerModel that = (AnswerModel) o;
        return Objects.equals(this.linkId, that.linkId) && Objects.equals(this.value, that.value) && this.answerType == that.answerType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(linkId, value, answerType);
    }
}
