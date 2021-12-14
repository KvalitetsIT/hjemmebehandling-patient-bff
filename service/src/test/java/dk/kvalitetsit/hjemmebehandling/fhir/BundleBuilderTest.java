package dk.kvalitetsit.hjemmebehandling.fhir;

import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BundleBuilderTest {
    private BundleBuilder subject = new BundleBuilder();

    private static final String CAREPLAN_ID = "careplan-1";
    private static final String QUESTIONNAIRERESPONSE_ID = "questionnaireresponse-1";

    @Test
    public void buildQuestionnaireResponseBundle_mapsArgumentsToEntries() {
        // Arrange
        QuestionnaireResponse questionnaireResponse = buildQuestionnaireResponse(QUESTIONNAIRERESPONSE_ID);
        CarePlan carePlan = buildCarePlan(CAREPLAN_ID, QUESTIONNAIRERESPONSE_ID);

        // Act
        Bundle result = subject.buildQuestionnaireResponseBundle(questionnaireResponse, carePlan);

        // Assert
        assertEquals(2, result.getEntry().size());
        assertEquals(Bundle.HTTPVerb.POST, result.getEntry().get(0).getRequest().getMethod());
        assertEquals(Bundle.HTTPVerb.PUT, result.getEntry().get(1).getRequest().getMethod());
    }

    private QuestionnaireResponse buildQuestionnaireResponse(String questionnaireResponseId) {
        QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();

        questionnaireResponse.setId(questionnaireResponseId);

        return questionnaireResponse;
    }

    private CarePlan buildCarePlan(String carePlanId, String patientId) {
        CarePlan carePlan = new CarePlan();

        carePlan.setId(carePlanId);
        carePlan.setSubject(new Reference(patientId));

        return carePlan;
    }
}