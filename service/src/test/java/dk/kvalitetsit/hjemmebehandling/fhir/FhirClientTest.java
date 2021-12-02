package dk.kvalitetsit.hjemmebehandling.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import dk.kvalitetsit.hjemmebehandling.constants.ExaminationStatus;
import dk.kvalitetsit.hjemmebehandling.constants.Systems;
import dk.kvalitetsit.hjemmebehandling.context.UserContext;
import dk.kvalitetsit.hjemmebehandling.context.UserContextProvider;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class FhirClientTest {
    private FhirClient subject;

    @Mock
    private FhirContext context;

    private String endpoint = "http://foo";

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private IGenericClient client;


    private static final String QUESTIONNAIRE_RESPONSE_ID_1 = "questionnaireresponse-1";
    private static final String QUESTIONNAIRE_RESPONSE_ID_2 = "questionnaireresponse-2";

    @BeforeEach
    public void setup() {
        Mockito.when(context.newRestfulGenericClient(endpoint)).thenReturn(client);
        subject = new FhirClient(context, endpoint);
    }

    @Test
    public void lookupQuestionnaireResponses_carePlanAndQuestionnairesPresent_success() {
        // Arrange
        String carePlanId = "careplan-1";

        QuestionnaireResponse questionnaireResponse1 = new QuestionnaireResponse();
        questionnaireResponse1.setId(QUESTIONNAIRE_RESPONSE_ID_1);
        QuestionnaireResponse questionnaireResponse2 = new QuestionnaireResponse();
        questionnaireResponse2.setId(QUESTIONNAIRE_RESPONSE_ID_2);
        setupSearchQuestionnaireResponseClient(1, questionnaireResponse1, questionnaireResponse2);

        // Act
        FhirLookupResult result = subject.lookupQuestionnaireResponses(carePlanId);

        // Assert
        assertEquals(2, result.getQuestionnaireResponses().size());
        assertTrue(result.getQuestionnaireResponses().contains(questionnaireResponse1));
        assertTrue(result.getQuestionnaireResponses().contains(questionnaireResponse2));
    }

    private void setupSearchQuestionnaireResponseClient(int criteriaCount, QuestionnaireResponse... questionnaireResponses) {
        setupSearchClient(criteriaCount, 3, QuestionnaireResponse.class, questionnaireResponses);
    }

    private void setupSearchClient(int criteriaCount, int includeCount, Class<? extends Resource> resourceClass, Resource... resources) {
        setupSearchClient(criteriaCount, includeCount, false, false, false, resourceClass, resources);
    }

    private void setupSearchClient(int criteriaCount, int includeCount, boolean withSort, boolean withOffset, boolean withCount, Class<? extends Resource> resourceClass, Resource... resources) {
        Bundle bundle = new Bundle();

        for(Resource resource : resources) {
            Bundle.BundleEntryComponent component = new Bundle.BundleEntryComponent();
            component.setResource(resource);
            bundle.addEntry(component);
        }
        bundle.setTotal(resources.length);

        var query = client.search().forResource(resourceClass);
        if(criteriaCount > 0) {
            query = query.where(Mockito.any(ICriterion.class));
        }
        for(var i = 1; i < criteriaCount; i++) {
            query = query.and(Mockito.any(ICriterion.class));
        }
        for(var i = 0; i < includeCount; i++) {
            query = query.include(Mockito.any(Include.class));
        }
        if(withSort) {
            query = query.sort(Mockito.any(SortSpec.class));
        }
        if(withOffset) {
            query = query.offset(Mockito.anyInt());
        }
        if(withCount) {
            query = query.count(Mockito.anyInt());
        }

        Mockito.when(query
                .execute())
                .thenReturn(bundle);
    }
}