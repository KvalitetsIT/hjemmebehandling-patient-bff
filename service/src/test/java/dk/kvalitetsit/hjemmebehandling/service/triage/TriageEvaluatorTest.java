package dk.kvalitetsit.hjemmebehandling.service.triage;

import dk.kvalitetsit.hjemmebehandling.constants.AnswerType;
import dk.kvalitetsit.hjemmebehandling.constants.TriagingCategory;
import dk.kvalitetsit.hjemmebehandling.model.AnswerModel;
import dk.kvalitetsit.hjemmebehandling.model.ThresholdModel;
import dk.kvalitetsit.hjemmebehandling.types.ThresholdType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TriageEvaluatorTest {
    private TriageEvaluator subject = new TriageEvaluator();

    @Test
    public void determineTriagingCategory_badArguments_throwsException() {
        // Arrange
        List<AnswerModel> answers = List.of();
        List<ThresholdModel> thresholds = null;

        // Act

        // Assert
        assertThrows(IllegalArgumentException.class, () -> subject.determineTriagingCategory(answers, thresholds));
    }

    @Test
    public void determineTriagingCategory_handlesBooleanAnswer_normal() {
        // Arrange
        var answers = List.of(buildBooleanAnswer("1", true));
        var thresholds = List.of(buildBooleanThreshold("1", ThresholdType.NORMAL, true));

        // Act
        var result = subject.determineTriagingCategory(answers, thresholds);

        // Assert
        assertEquals(TriagingCategory.GREEN, result);
    }

    @Test
    public void determineTriagingCategory_handlesBooleanAnswer_critical() {
        // Arrange
        var answers = List.of(buildBooleanAnswer("1", true));
        var thresholds = List.of(buildBooleanThreshold("1", ThresholdType.CRITICAL, true));

        // Act
        var result = subject.determineTriagingCategory(answers, thresholds);

        // Assert
        assertEquals(TriagingCategory.RED, result);
    }

    @Test
    public void determineTriagingCategory_multipleAnswers_returnsMostCriticalCategory() {
        // Arrange
        var answers = List.of(buildBooleanAnswer("1", true), buildBooleanAnswer("2", true));
        var thresholds = List.of(buildBooleanThreshold("1", ThresholdType.NORMAL, true), buildBooleanThreshold("2", ThresholdType.CRITICAL, true));

        // Act
        var result = subject.determineTriagingCategory(answers, thresholds);

        // Assert
        assertEquals(TriagingCategory.RED, result);
    }

    @Test
    public void determineTriagingCategory_handlesQuantityAnswer_halfOpenToTheLeft() {
        // Arrange
        var answers = List.of(buildQuantityAnswer("1", 2.4));
        var thresholds = List.of(buildQuantityThreshold("1", ThresholdType.ABNORMAL, null, 5.0));

        // Act
        var result = subject.determineTriagingCategory(answers, thresholds);

        // Assert
        assertEquals(TriagingCategory.YELLOW, result);
    }

    @Test
    public void determineTriagingCategory_handlesQuantityAnswer_closed() {
        // Arrange
        var answers = List.of(buildQuantityAnswer("1", 12.4));
        var thresholds = List.of(buildQuantityThreshold("1", ThresholdType.CRITICAL, 5.0, 17.0));

        // Act
        var result = subject.determineTriagingCategory(answers, thresholds);

        // Assert
        assertEquals(TriagingCategory.RED, result);
    }

    @Test
    public void determineTriagingCategory_handlesQuantityAnswer_halfOpenToTheRight() {
        // Arrange
        var answers = List.of(buildQuantityAnswer("1", 12.4));
        var thresholds = List.of(buildQuantityThreshold("1", ThresholdType.NORMAL, 5.0, null));

        // Act
        var result = subject.determineTriagingCategory(answers, thresholds);

        // Assert
        assertEquals(TriagingCategory.GREEN, result);
    }

    @Test
    public void determineTriagingCategory_handlesQuantityAnswer_unbounded() {
        // Arrange
        var answers = List.of(buildQuantityAnswer("1", -12.4));
        var thresholds = List.of(buildQuantityThreshold("1", ThresholdType.NORMAL, null, null));

        // Act
        var result = subject.determineTriagingCategory(answers, thresholds);

        // Assert
        assertEquals(TriagingCategory.GREEN, result);
    }

    @Test
    public void determineTriagingCategory_handlesQuantityAnswer_lowerBoundInclusive() {
        // Arrange
        var answers = List.of(buildQuantityAnswer("1", 6.0));
        var thresholds = List.of(
                buildQuantityThreshold("1", ThresholdType.ABNORMAL, null, 5.0),
                buildQuantityThreshold("1", ThresholdType.CRITICAL, 5.0, 6.0),
                buildQuantityThreshold("1", ThresholdType.NORMAL, 6.0, null)
        );

        // Act
        var result = subject.determineTriagingCategory(answers, thresholds);

        // Assert
        assertEquals(TriagingCategory.GREEN, result);
    }

    @Test
    public void determineTriagingCategory_handlesQuantityAnswer_upperBoundExclusive() {
        // Arrange
        var answers = List.of(buildQuantityAnswer("1", 5.0));
        var thresholds = List.of(
                buildQuantityThreshold("1", ThresholdType.ABNORMAL, null, 5.0),
                buildQuantityThreshold("1", ThresholdType.CRITICAL, 5.0, 6.0),
                buildQuantityThreshold("1", ThresholdType.NORMAL, 6.0, null)
        );

        // Act
        var result = subject.determineTriagingCategory(answers, thresholds);

        // Assert
        assertEquals(TriagingCategory.RED, result);
    }

    private AnswerModel buildBooleanAnswer(String linkId, boolean value) {
        AnswerModel answer = new AnswerModel();

        answer.setAnswerType(AnswerType.BOOLEAN);
        answer.setLinkId(linkId);
        answer.setValue(Boolean.toString(value));

        return answer;
    }

    private AnswerModel buildQuantityAnswer(String linkId, double value) {
        AnswerModel answer = new AnswerModel();

        answer.setAnswerType(AnswerType.QUANTITY);
        answer.setLinkId(linkId);
        answer.setValue(Double.toString(value));

        return answer;
    }

    private ThresholdModel buildBooleanThreshold(String linkId, ThresholdType thresholdType, boolean value) {
        ThresholdModel threshold = new ThresholdModel();

        threshold.setQuestionnaireItemLinkId(linkId);
        threshold.setType(thresholdType);
        threshold.setValueBoolean(value);

        return threshold;
    }

    private ThresholdModel buildQuantityThreshold(String linkId, ThresholdType thresholdType, Double low, Double high) {
        ThresholdModel threshold = new ThresholdModel();

        threshold.setQuestionnaireItemLinkId(linkId);
        threshold.setType(thresholdType);
        threshold.setValueQuantityLow(low);
        threshold.setValueQuantityHigh(high);

        return threshold;
    }
}