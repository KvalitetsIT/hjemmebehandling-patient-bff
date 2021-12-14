package dk.kvalitetsit.hjemmebehandling.service.triage;

import dk.kvalitetsit.hjemmebehandling.constants.TriagingCategory;
import dk.kvalitetsit.hjemmebehandling.model.AnswerModel;
import dk.kvalitetsit.hjemmebehandling.model.ThresholdModel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TriageEvaluator {
    public TriagingCategory determineTriagingCategory(List<AnswerModel> answers, List<ThresholdModel> thresholds) {
        return TriagingCategory.RED;
    }
}
