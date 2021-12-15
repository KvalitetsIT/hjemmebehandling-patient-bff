package dk.kvalitetsit.hjemmebehandling.service.triage;

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
        if(answers == null || answers.isEmpty() || thresholds == null || thresholds.isEmpty()) {
            throw new IllegalArgumentException("Arguments must be non-null and non-empty!");
        }

        // Group the thresholds by linkId
        var thresholdsByLinkId = thresholds
                .stream()
                .collect(Collectors.groupingBy(t -> t.getQuestionnaireItemLinkId()));

        // Evaluate each answer against its thresholds.
        var result = TriagingCategory.GREEN;
        for(var answer : answers) {
            if(!thresholdsByLinkId.containsKey(answer.getLinkId())) {
                throw new IllegalStateException(String.format("Trying to evaluate thresholds for answer with linkId %s, but no thresholds were found!", answer.getLinkId()));
            }
            var thresholdsForAnswer = thresholdsByLinkId.get(answer.getLinkId());

            // We ASSUME that the thresholds are consistent in the sense that every possible answer can be mapped to exactly one ThresholdType,
            // and just throw exceptions in case the provided thresholds are malformed.
            // It is the responsibility of the PlanDefinition provider (ie. the administration module) to ensure that this condition holds.

            // Compute the category and determine whether it is more critical than the ones we've already computed.
            var categoryForAnswer = evaluateAnswer(answer, thresholdsForAnswer);
            result = mostCriticalCategory(result, categoryForAnswer);
        }
        return result;
    }

    private TriagingCategory mostCriticalCategory(TriagingCategory a, TriagingCategory b) {
        if(ranks.get(a) < ranks.get(b)) {
            return b;
        }
        else {
            return a;
        }
    }

    private TriagingCategory evaluateAnswer(AnswerModel answer, List<ThresholdModel> thresholdsForAnswer) {
        Optional<TriagingCategory> result = Optional.empty();
        for(ThresholdModel threshold : thresholdsForAnswer) {
            boolean answerCoveredByThreshold = false;
            switch(answer.getAnswerType()) {
                case BOOLEAN:
                    answerCoveredByThreshold = evaluateBooleanAnswer(answer, threshold);
                    break;
                case QUANTITY:
                    answerCoveredByThreshold = evaluateQuantityAnswer(answer, threshold);
                    break;
                default:
                    throw new IllegalArgumentException(String.format("Don't know how to handle AnswerType %s!", answer.getAnswerType().toString()));
            }
            if(answerCoveredByThreshold) {
                result = Optional.of(mapThresholdType(threshold.getType()));
            }
        }
        if(!result.isPresent()) {
            throw new IllegalStateException(String.format("Could not evaluate boolean answer for linkId %s: No Threshold found for value %s", answer.getLinkId(), answer.getValue()));
        }
        return result.get();
    }

    private boolean evaluateBooleanAnswer(AnswerModel answer, ThresholdModel threshold) {
        if(!List.of("true", "false").contains(answer.getValue())) {
            throw new IllegalArgumentException(String.format("Could not evaluate boolean answer for linkId %s: Value %s is not a boolean.", answer.getLinkId(), answer.getValue()));
        }
        boolean value = Boolean.valueOf(answer.getValue());

        if(threshold.getValueBoolean() == null) {
            throw new IllegalStateException(String.format("Could not evaluate boolean answer for linkId %s: Threshold did not contain a boolean value.", answer.getLinkId()));
        }

        return threshold.getValueBoolean() == value;
    }

    private boolean evaluateQuantityAnswer(AnswerModel answer, ThresholdModel threshold) {
        double value = 0.0;
        try {
            value = Double.valueOf(answer.getValue());
        }
        catch(NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Could not evaluate quantity answer for linkId %s: Value %s is not a double.", answer.getLinkId(), answer.getValue()));
        }

        // Check whether the value is contained in the provided interval (null-values correspond to infinity in wither direction).
        boolean contained = false;
        if(threshold.getValueQuantityLow() == null && threshold.getValueQuantityHigh() == null) {
            contained = true;
        }
        if(threshold.getValueQuantityLow() != null && threshold.getValueQuantityHigh() == null) {
            contained = threshold.getValueQuantityLow() <= value;
        }
        if(threshold.getValueQuantityLow() == null && threshold.getValueQuantityHigh() != null) {
            contained = threshold.getValueQuantityHigh() > value;
        }
        if(threshold.getValueQuantityLow() != null && threshold.getValueQuantityHigh() != null) {
            contained = threshold.getValueQuantityLow() <= value && value < threshold.getValueQuantityHigh();
        }
        return contained;
    }

    private TriagingCategory mapThresholdType(ThresholdType type) {
        switch(type) {
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
