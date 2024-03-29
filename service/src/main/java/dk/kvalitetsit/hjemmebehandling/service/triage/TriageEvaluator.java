package dk.kvalitetsit.hjemmebehandling.service.triage;

import dk.kvalitetsit.hjemmebehandling.constants.AnswerType;
import dk.kvalitetsit.hjemmebehandling.constants.TriagingCategory;
import dk.kvalitetsit.hjemmebehandling.model.AnswerModel;
import dk.kvalitetsit.hjemmebehandling.model.ThresholdModel;
import dk.kvalitetsit.hjemmebehandling.types.ThresholdType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TriageEvaluator {
    private static Map<TriagingCategory, Integer> ranks = Map.of(
            TriagingCategory.RED, 3,
            TriagingCategory.YELLOW, 2,
            TriagingCategory.GREEN, 1
    );

    public TriagingCategory determineTriagingCategory(List<AnswerModel> answers, List<ThresholdModel> thresholds) {
        if (answers == null || answers.isEmpty() || thresholds == null) {
            throw new IllegalArgumentException("Arguments must be non-null!");
        }

        // Group the thresholds by linkId
        var thresholdsByLinkId = thresholds
                .stream()
                .collect(Collectors.groupingBy(t -> t.getQuestionnaireItemLinkId()));

        // Evaluate each answer against its thresholds.
        var result = TriagingCategory.GREEN;
        for (var answer : answers) {

            var thresholdsForAnswer = thresholdsByLinkId.get(answer.getLinkId());

            // We ASSUME that the thresholds are consistent in the sense that every possible answer can be mapped to exactly one ThresholdType,
            // and just throw exceptions in case the provided thresholds are malformed.
            // It is the responsibility of the PlanDefinition provider (ie. the administration module) to ensure that this condition holds.

            // Compute the category and determine whether it is more critical than the ones we've already computed.

            var categoryForAnswer = TriagingCategory.GREEN;
            boolean thresholdsWereFound = thresholdsByLinkId.containsKey(answer.getLinkId());
            if (thresholdsWereFound)
                categoryForAnswer = evaluateAnswer(answer, thresholdsForAnswer);
            result = mostCriticalCategory(result, categoryForAnswer);

            if (answer.getAnswerType() == AnswerType.GROUP && answer.getSubAnswers() != null) {
                var subAnswersResult = this.determineTriagingCategory(answer.getSubAnswers(), thresholds);
                result = mostCriticalCategory(result, subAnswersResult);
            }
        }
        return result;
    }

    private TriagingCategory mostCriticalCategory(TriagingCategory a, TriagingCategory b) {
        if (ranks.get(a) < ranks.get(b)) {
            return b;
        } else {
            return a;
        }
    }

    private TriagingCategory evaluateAnswer(AnswerModel answer, List<ThresholdModel> thresholdsForAnswer) {
        thresholdsForAnswer.sort((a, b) -> b.getType().compareTo(a.getType())); //We sort so that the first threshold is the most critical - And then the first threshold that contains the answer will be returned

        Optional<TriagingCategory> result = Optional.empty();
        for (ThresholdModel threshold : thresholdsForAnswer) {
            boolean answerCoveredByThreshold = false;
            switch (answer.getAnswerType()) {
                case BOOLEAN:
                    answerCoveredByThreshold = evaluateBooleanAnswer(answer, threshold);
                    break;
                case QUANTITY:
                    answerCoveredByThreshold = evaluateQuantityAnswer(answer, threshold);
                    break;
                case STRING:
                    answerCoveredByThreshold = evaluateStringAnswer(answer, threshold);
                    break;
                default:
                    throw new IllegalArgumentException(String.format("Don't know how to handle AnswerType %s!", answer.getAnswerType().toString()));
            }

            if (answerCoveredByThreshold) {
                return mapThresholdType(threshold.getType()); //Since we sorted the threshold by category, we can just return the first one that matches
            }
        }
        return TriagingCategory.RED;
    }

    private boolean evaluateStringAnswer(AnswerModel answer, ThresholdModel threshold) {
        if (answer.getValue() == null || "".equals(answer.getValue())) {
            throw new IllegalArgumentException(String.format("Could not evaluate string answer for linkId %s: Value is empty.", answer.getLinkId(), answer.getValue()));
        }
        if (threshold.getValueOption() == null) {
            throw new IllegalStateException(String.format("Could not evaluate string answer for linkId %s: Threshold did not contain a string value.", answer.getLinkId()));
        }

        return threshold.getValueOption().equals(answer.getValue());
    }

    private boolean evaluateBooleanAnswer(AnswerModel answer, ThresholdModel threshold) {
        if (!List.of("true", "false").contains(answer.getValue())) {
            throw new IllegalArgumentException(String.format("Could not evaluate boolean answer for linkId %s: Value %s is not a boolean.", answer.getLinkId(), answer.getValue()));
        }
        boolean value = Boolean.valueOf(answer.getValue());

        if (threshold.getValueBoolean() == null) {
            throw new IllegalStateException(String.format("Could not evaluate boolean answer for linkId %s: Threshold did not contain a boolean value.", answer.getLinkId()));
        }

        return threshold.getValueBoolean() == value;
    }

    private boolean evaluateQuantityAnswer(AnswerModel answer, ThresholdModel threshold) {
        double value = 0.0;
        try {
            value = Double.valueOf(answer.getValue());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Could not evaluate quantity answer for linkId %s: Value %s is not a double.", answer.getLinkId(), answer.getValue()));
        }

        // Check whether the value is contained in the provided interval (null-values correspond to infinity in wither direction).
        boolean contained = false;
        if (threshold.getValueQuantityLow() == null && threshold.getValueQuantityHigh() == null) {
            contained = true;
        }
        if (threshold.getValueQuantityLow() != null && threshold.getValueQuantityHigh() == null) {
            contained = threshold.getValueQuantityLow() <= value;
        }
        if (threshold.getValueQuantityLow() == null && threshold.getValueQuantityHigh() != null) {
            contained = threshold.getValueQuantityHigh() >= value;
        }
        if (threshold.getValueQuantityLow() != null && threshold.getValueQuantityHigh() != null) {
            contained = threshold.getValueQuantityLow() <= value && value <= threshold.getValueQuantityHigh();
        }
        return contained;
    }

    private TriagingCategory mapThresholdType(ThresholdType type) {
        switch (type) {
            case NORMAL:
                return TriagingCategory.GREEN;
            case ABNORMAL:
                return TriagingCategory.YELLOW;
            case CRITICAL:
                return TriagingCategory.RED;
            default:
                throw new IllegalArgumentException(String.format("Don't know how to handle ThresholdType %s!", type.toString()));
        }
    }
}
