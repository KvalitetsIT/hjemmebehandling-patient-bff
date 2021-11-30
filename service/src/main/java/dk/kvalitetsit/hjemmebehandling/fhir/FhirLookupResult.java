package dk.kvalitetsit.hjemmebehandling.fhir;

import org.hl7.fhir.r4.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class FhirLookupResult {
    private Map<String, Questionnaire> questionnairesById;
    private Map<String, QuestionnaireResponse> questionnaireResponsesById;

    private FhirLookupResult() {
        // Using LinkedHashMap preserves the insertion order (necessary for eg. returning sorted results).
        questionnairesById = new LinkedHashMap<>();
        questionnaireResponsesById = new LinkedHashMap<>();
    }

    public static FhirLookupResult fromBundle(Bundle bundle) {
        FhirLookupResult result = new FhirLookupResult();

        bundle.getEntry().forEach(e -> result.addResource(e.getResource()));

        return result;
    }

    public static FhirLookupResult fromResource(Resource resource) {
        return fromResources(resource);
    }

    public static FhirLookupResult fromResources(Resource... resources) {
        FhirLookupResult result = new FhirLookupResult();

        for(Resource resource : resources) {
            result.addResource(resource);
        }

        return result;
    }

    public Optional<Questionnaire> getQuestionnaire(String questionnaireId) {
        return getResource(questionnaireId, questionnairesById);
    }

    public List<Questionnaire> getQuestionnaires() {
        return getResources(questionnairesById);
    }

    public Optional<QuestionnaireResponse> getQuestionnaireResponse(String questionnaireResponseId) {
        return getResource(questionnaireResponseId, questionnaireResponsesById);
    }

    public List<QuestionnaireResponse> getQuestionnaireResponses() {
        return getResources(questionnaireResponsesById);
    }

    public FhirLookupResult merge(FhirLookupResult result) {
        for(Questionnaire questionnaire: result.questionnairesById.values()) {
            addResource(questionnaire);
        }
        for(QuestionnaireResponse questionnaireResponse: result.questionnaireResponsesById.values()) {
            addResource(questionnaireResponse);
        }

        return this;
    }

    private <T extends Resource> Optional<T> getResource(String resourceId, Map<String, T> resourcesById) {
        if(!resourcesById.containsKey(resourceId)) {
            return Optional.empty();
        }
        return Optional.of(resourcesById.get(resourceId));
    }

    private <T extends Resource> List<T> getResources(Map<String, T> resourcesById) {
        return resourcesById.values().stream().collect(Collectors.toList());
    }

    private void addResource(Resource resource) {
        String resourceId = resource.getIdElement().toUnqualifiedVersionless().getValue();
        switch(resource.getResourceType()) {
            case Questionnaire:
                questionnairesById.put(resourceId, (Questionnaire) resource);
                break;
            case QuestionnaireResponse:
                questionnaireResponsesById.put(resourceId, (QuestionnaireResponse) resource);
                break;
            default:
                throw new IllegalArgumentException(String.format("Unknown resource type: %s", resource.getResourceType().toString()));
        }
    }
}
