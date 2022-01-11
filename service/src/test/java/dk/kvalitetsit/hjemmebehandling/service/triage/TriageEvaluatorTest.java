package dk.kvalitetsit.hjemmebehandling.service.triage;

import dk.kvalitetsit.hjemmebehandling.constants.AnswerType;
import dk.kvalitetsit.hjemmebehandling.constants.TriagingCategory;
import dk.kvalitetsit.hjemmebehandling.model.AnswerModel;
import dk.kvalitetsit.hjemmebehandling.model.ThresholdModel;
import dk.kvalitetsit.hjemmebehandling.types.ThresholdType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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
        assertEquals(TriagingCategory.RED, result);
    }

    @Test
    public void determineTriagingCategory_redTriagingCategory_WhenNullThresholds() {
        // Arrange
        var answers = List.of(buildQuantityAnswer("1", 6.0));
        ArrayList<ThresholdModel> thresholds = null;

        // Act
        assertThrows(IllegalArgumentException.class,() -> subject.determineTriagingCategory(answers, thresholds));
    }

    @Test
    public void determineTriagingCategory_redTriagingCategory_WhenNoThresholds() {
        // Arrange
        var answers = List.of(buildQuantityAnswer("1", 6.0));
        var thresholds = new ArrayList<ThresholdModel>();

        // Act
        var result = subject.determineTriagingCategory(answers, thresholds);

        // Assert
        assertEquals(TriagingCategory.GREEN, result);
    }

    @Test
    public void determineTriagingCategory_redTriagingCategory_WhenNoThresholdMatchesAnswer_lowerEnd() {
        // Arrange
        var answers = List.of(buildQuantityAnswer("1", 1.0));
        var thresholds = List.of(
                buildQuantityThreshold("1", ThresholdType.ABNORMAL, 2.0, 5.0),
                buildQuantityThreshold("1", ThresholdType.CRITICAL, 5.0, 6.0),
                buildQuantityThreshold("1", ThresholdType.NORMAL, 6.0, null)
        );

        // Act
        var result = subject.determineTriagingCategory(answers, thresholds);

        // Assert
        assertEquals(TriagingCategory.RED, result);
    }


    @Test
    public void determineTriagingCategory_redTriagingCategory_WhenNoThresholdMatchesAnswer_higherEnd() {
        // Arrange
        var answers = List.of(buildQuantityAnswer("1", 8.0));
        var thresholds = List.of(
                buildQuantityThreshold("1", ThresholdType.ABNORMAL, null, 5.0),
                buildQuantityThreshold("1", ThresholdType.CRITICAL, 5.0, 6.0),
                buildQuantityThreshold("1", ThresholdType.NORMAL, 6.0, 7.0)
        );

        // Act
        var result = subject.determineTriagingCategory(answers, thresholds);

        // Assert
        assertEquals(TriagingCategory.RED, result);
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

    @Test
    public void determineTriagingCategory_OneIsGreenWithNoThresholds_OneIsYellowWithThresholds_ReturnsYellow() {
        // Arrange
        var answers = List.of(
                buildQuantityAnswer("1", 40),
                buildQuantityAnswer("2", 40)

        );
        var thresholds = List.of(
                buildQuantityThreshold("2", ThresholdType.NORMAL, null, 24.9),
                buildQuantityThreshold("2", ThresholdType.ABNORMAL, 25.0, 49.9),
                buildQuantityThreshold("2", ThresholdType.CRITICAL, 50.0, null)
        );

        // Act
        var result = subject.determineTriagingCategory(answers, thresholds);

        // Assert
        assertEquals(TriagingCategory.YELLOW, result);
    }

    private AnswerModel buildBooleanAnswer(String linkId, boolean value) {
        return buildAnswer(linkId, AnswerType.BOOLEAN, Boolean.toString(value));
    }

    private AnswerModel buildQuantityAnswer(String linkId, double value) {
        return buildAnswer(linkId, AnswerType.QUANTITY, Double.toString(value));
    }

    private AnswerModel buildAnswer(String linkId, AnswerType answerType, String value) {
        AnswerModel answer = new AnswerModel();

        answer.setLinkId(linkId);
        answer.setAnswerType(answerType);
        answer.setValue(value);

        return answer;
    }

    private ThresholdModel buildBooleanThreshold(String linkId, ThresholdType thresholdType, boolean valueBoolean) {
        return buildThreshold(linkId, thresholdType, valueBoolean, null, null);
    }

    private ThresholdModel buildQuantityThreshold(String linkId, ThresholdType thresholdType, Double low, Double high) {
        return buildThreshold(linkId, thresholdType, null, low, high);
    }

    private ThresholdModel buildThreshold(String linkId, ThresholdType thresholdType, Boolean valueBoolean, Double low, Double high) {
        ThresholdModel threshold = new ThresholdModel();

        threshold.setQuestionnaireItemLinkId(linkId);
        threshold.setType(thresholdType);
        threshold.setValueBoolean(valueBoolean);
        threshold.setValueQuantityLow(low);
        threshold.setValueQuantityHigh(high);

        return threshold;
    }
}