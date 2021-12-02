package dk.kvalitetsit.hjemmebehandling.fhir;

import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FhirLookupResultTest {
    private static final String QUESTIONNAIRE_ID_1 = "questionnaire-1";

    @Test
    public void fromBuild_unknownResourceType_throwsException() {
        // Arrange
        Bundle bundle = buildBundle(new AdverseEvent());

        // Act

        // Assert
        assertThrows(IllegalArgumentException.class, () -> FhirLookupResult.fromBundle(bundle));
    }

    @Test
    public void getQuestionnaires_questionnairePresent_success() {
        // Arrange
        Questionnaire questionnaire = buildQuestionnaire(QUESTIONNAIRE_ID_1);
        Bundle bundle = buildBundle(questionnaire);

        // Act
        FhirLookupResult result = FhirLookupResult.fromBundle(bundle);

        // Assert
        assertEquals(1, result.getQuestionnaires().size());
        assertEquals(questionnaire, result.getQuestionnaire(QUESTIONNAIRE_ID_1).get());
    }

    @Test
    public void getQuestionnaires_questionnaireMissing_throwsException() {
        // Arrange
        Bundle bundle = buildBundle();

        // Act
        FhirLookupResult result = FhirLookupResult.fromBundle(bundle);

        // Assert
        assertEquals(0, result.getQuestionnaires().size());
        assertFalse(result.getQuestionnaire(QUESTIONNAIRE_ID_1).isPresent());
    }

    private Questionnaire buildQuestionnaire(String questionnaireId) {
        Questionnaire questionnaire = new Questionnaire();

        questionnaire.setId(questionnaireId);

        return questionnaire;
    }

    private Bundle buildBundle(Resource... resources) {
        Bundle bundle = new Bundle();

        for(var resource: resources) {
            var entry = new Bundle.BundleEntryComponent();
            entry.setResource(resource);
            bundle.addEntry(entry);
        }

        return bundle;
    }
}