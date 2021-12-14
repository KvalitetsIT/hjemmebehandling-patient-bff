package dk.kvalitetsit.hjemmebehandling.fhir;

import org.hl7.fhir.r4.model.*;

public class BundleBuilder {
    public Bundle buildQuestionnaireResponseBundle(QuestionnaireResponse questionnaireResponse, CarePlan carePlan) {
        // Build the QuestionnaireResponse entry.
        var questionnaireResponseEntry = buildQuestionnaireResponseEntry(questionnaireResponse);

        // Build the CarePlan entry.
        var carePlanEntry = buildCarePlanEntry(carePlan);

        // Build a transaction bundle.
        var bundle = new Bundle();

        bundle.setType(Bundle.BundleType.TRANSACTION);
        bundle.addEntry(questionnaireResponseEntry);
        bundle.addEntry(carePlanEntry);

        return bundle;
    }

    private Bundle.BundleEntryComponent buildQuestionnaireResponseEntry(QuestionnaireResponse questionnaireResponse) {
        return buildEntry(questionnaireResponse, "QuestionnaireResponse/questionnaireresponse-entry", Bundle.HTTPVerb.POST);
    }

    private Bundle.BundleEntryComponent buildCarePlanEntry(CarePlan carePlan) {
        return buildEntry(carePlan, "CarePlan/careplan-entry", Bundle.HTTPVerb.PUT);
    }

    private Bundle.BundleEntryComponent buildEntry(Resource resource, String fullUrl, Bundle.HTTPVerb method) {
        var entry = new Bundle.BundleEntryComponent();

        entry.setFullUrl(fullUrl);
        entry.setRequest(new Bundle.BundleEntryRequestComponent());
        entry.getRequest().setMethod(method);
        entry.getRequest().setUrl(resource.getId());
        entry.setResource(resource);

        return entry;
    }
}
