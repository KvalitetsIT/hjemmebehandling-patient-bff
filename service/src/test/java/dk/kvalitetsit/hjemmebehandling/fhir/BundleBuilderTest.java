package dk.kvalitetsit.hjemmebehandling.fhir;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BundleBuilderTest {
    private static final String CAREPLAN_ID = "careplan-1";
    private static final String QUESTIONNAIRERESPONSE_ID = "questionnaireresponse-1";
    private final BundleBuilder subject = new BundleBuilder();

    @Test
    public void buildQuestionnaireResponseBundle_mapsArgumentsToEntries() {
        QuestionnaireResponse questionnaireResponse = buildQuestionnaireResponse();
        CarePlan carePlan = buildCarePlan();

        Bundle result = subject.buildQuestionnaireResponseBundle(questionnaireResponse, carePlan);

        assertEquals(2, result.getEntry().size());
        assertEquals(Bundle.HTTPVerb.POST, result.getEntry().getFirst().getRequest().getMethod());
        assertEquals(Bundle.HTTPVerb.PUT, result.getEntry().get(1).getRequest().getMethod());
    }

    private QuestionnaireResponse buildQuestionnaireResponse() {
        QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
        questionnaireResponse.setId(BundleBuilderTest.QUESTIONNAIRERESPONSE_ID);
        return questionnaireResponse;
    }

    private CarePlan buildCarePlan() {
        CarePlan carePlan = new CarePlan();
        carePlan.setId(BundleBuilderTest.CAREPLAN_ID);
        carePlan.setSubject(new Reference(BundleBuilderTest.QUESTIONNAIRERESPONSE_ID));
        return carePlan;
    }
}