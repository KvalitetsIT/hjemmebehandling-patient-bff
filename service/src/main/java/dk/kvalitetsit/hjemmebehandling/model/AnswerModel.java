package dk.kvalitetsit.hjemmebehandling.model;

import dk.kvalitetsit.hjemmebehandling.api.AnswerDto;
import dk.kvalitetsit.hjemmebehandling.constants.AnswerType;
import dk.kvalitetsit.hjemmebehandling.constants.TriagingCategory;

import java.util.List;
import java.util.Objects;

public class AnswerModel {
    private String linkId;
    private String value;
    private AnswerType answerType;
    private List<AnswerModel> subAnswers;

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

    public List<AnswerModel> getSubAnswers() {
        return subAnswers;
    }

    public void setSubAnswers(List<AnswerModel> subAnswers) {
        this.subAnswers = subAnswers;
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
