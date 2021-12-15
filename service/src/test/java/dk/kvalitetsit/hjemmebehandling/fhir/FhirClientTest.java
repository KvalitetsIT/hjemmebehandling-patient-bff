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
import org.junit.jupiter.api.Disabled;
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

    private static final String CAREPLAN_ID_1 = "CarePlan/careplan-1";
    private static final String ORGANIZATION_ID_1 = "Organization/organization-1";
    private static final String ORGANIZATION_ID_2 = "Organization/organization-2";
    private static final String PLANDEFINITION_ID_1 = "PlanDefinition/plandefinition-1";
    private static final String QUESTIONNAIRE_RESPONSE_ID_1 = "questionnaireresponse-1";
    private static final String QUESTIONNAIRE_RESPONSE_ID_2 = "questionnaireresponse-2";

    private static final String SOR_CODE_1 = "123456";
    private static final String SOR_CODE_2 = "654321";

    @BeforeEach
    public void setup() {
        Mockito.when(context.newRestfulGenericClient(endpoint)).thenReturn(client);
        subject = new FhirClient(context, endpoint);
    }

    @Test
    public void lookupCarePlanByCpr_carePlanPresent_success() {
        // Arrange
        String cpr = "0101010101";
        CarePlan carePlan = new CarePlan();
        carePlan.setId(CAREPLAN_ID_1);
        carePlan.addIdentifier().setSystem(Systems.CPR).setValue(cpr);

        setupSearchCarePlanByCprClient(carePlan);
        setupOrganization(SOR_CODE_1, ORGANIZATION_ID_1);

        // Act
        FhirLookupResult result = subject.lookupActiveCarePlan(cpr);

        // Assert
        assertTrue(result.getCarePlan(CAREPLAN_ID_1).isPresent());
        assertEquals(carePlan, result.getCarePlan(CAREPLAN_ID_1).get());
        assertEquals(Systems.CPR, result.getCarePlan(CAREPLAN_ID_1).get().getIdentifierFirstRep().getSystem());
        assertEquals(cpr, result.getCarePlan(CAREPLAN_ID_1).get().getIdentifierFirstRep().getValue());
    }

    @Test
    public void lookupCarePlanByCpr_carePlanMissing_empty() {
        // Arrange
        String cpr = "0101010101";

        setupSearchCarePlanByCprClient();
        setupOrganization(SOR_CODE_1, ORGANIZATION_ID_1);

        // Act
        FhirLookupResult result = subject.lookupActiveCarePlan(cpr);

        // Assert
        assertFalse(result.getCarePlan(CAREPLAN_ID_1).isPresent());
    }

    @Test
    public void lookupCarePlanByCpr_resultIncludesOrganization() {
        // Arrange
        String cpr = "0101010101";
        CarePlan carePlan = new CarePlan();
        carePlan.setId(CAREPLAN_ID_1);
        carePlan.addIdentifier().setSystem(Systems.CPR).setValue(cpr);

        setupSearchCarePlanByCprClient(carePlan);
        setupOrganization(SOR_CODE_1, ORGANIZATION_ID_1);

        // Act
        FhirLookupResult result = subject.lookupActiveCarePlan(cpr);

        // Assert
        assertTrue(result.getOrganization(ORGANIZATION_ID_1).isPresent());
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

        setupOrganization(SOR_CODE_1, ORGANIZATION_ID_1);

        // Act
        FhirLookupResult result = subject.lookupQuestionnaireResponses(carePlanId);

        // Assert
        assertEquals(2, result.getQuestionnaireResponses().size());
        assertTrue(result.getQuestionnaireResponses().contains(questionnaireResponse1));
        assertTrue(result.getQuestionnaireResponses().contains(questionnaireResponse2));
    }

    @Test
    public void lookupQuestionnaireResponses_includesPlanDefinition() {
        // Arrange
        String carePlanId = "careplan-1";

        QuestionnaireResponse questionnaireResponse1 = new QuestionnaireResponse();
        questionnaireResponse1.setId(QUESTIONNAIRE_RESPONSE_ID_1);
        QuestionnaireResponse questionnaireResponse2 = new QuestionnaireResponse();
        questionnaireResponse2.setId(QUESTIONNAIRE_RESPONSE_ID_2);
        setupSearchQuestionnaireResponseClient(1, questionnaireResponse1, questionnaireResponse2);

        setupOrganization(SOR_CODE_1, ORGANIZATION_ID_1);

        // Act
        FhirLookupResult result = subject.lookupQuestionnaireResponses(carePlanId);

        // Assert
        assertEquals(2, result.getQuestionnaireResponses().size());
        assertEquals(1, result.getPlanDefinitions().size());
        assertTrue(result.getPlanDefinition(PLANDEFINITION_ID_1).isPresent());
    }

    @Test
    public void saveQuestionnaireResponse_returnsQuestionnaireResponseId() {
        // Arrange
        QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
        CarePlan carePlan = new CarePlan();

        Bundle responseBundle = buildResponseBundle("201", "QuestionnaireResponse/2", "200", "CarePlan/3");
        setupTransactionClient(responseBundle);

        // Act
        String result = subject.saveQuestionnaireResponse(questionnaireResponse, carePlan);

        // Assert
        assertEquals("QuestionnaireResponse/2", result);
    }

    @Test
    public void saveQuestionnaireResponse_questionnaireResponseLocationMissing_throwsException() {
        // Arrange
        QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
        CarePlan carePlan = new CarePlan();

        Bundle responseBundle = buildResponseBundle("201", "Questionnaire/4", "200", "CarePlan/3");
        setupTransactionClient(responseBundle);

        // Act

        // Assert
        assertThrows(IllegalStateException.class, () -> subject.saveQuestionnaireResponse(questionnaireResponse, carePlan));
    }

    @Test
    public void saveQuestionnaireResponse_unwantedHttpStatus_throwsException() {
        // Arrange
        QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
        CarePlan carePlan = new CarePlan();

        Bundle responseBundle = buildResponseBundle("400", null, "400", null);
        setupTransactionClient(responseBundle);

        // Act

        // Assert
        assertThrows(IllegalStateException.class, () -> subject.saveQuestionnaireResponse(questionnaireResponse, carePlan));
    }

    @Test
    @Disabled("TODO - figure out how to handle tagging")
    public void saveQuestionnaireResponse_addsOrganizationTag() {
        // Arrange
        QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
        CarePlan carePlan = new CarePlan();

        Bundle responseBundle = buildResponseBundle("201", "QuestionnaireResponse/2", "201", "CarePlan/3");
        setupTransactionClient(responseBundle, SOR_CODE_2, ORGANIZATION_ID_2);

        // Act
        String result = subject.saveQuestionnaireResponse(questionnaireResponse, carePlan);

        // Assert
        assertTrue(isTaggedWithId(questionnaireResponse, ORGANIZATION_ID_2));
    }

    private Bundle buildResponseBundle(String questionnaireResponseStatus, String questionnaireResponseLocation, String carePlanStatus, String carePlanLocation) {
        Bundle responseBundle = new Bundle();

        var questionnaireResponseEntry = responseBundle.addEntry();
        questionnaireResponseEntry.setResponse(new Bundle.BundleEntryResponseComponent());
        questionnaireResponseEntry.getResponse().setStatus(questionnaireResponseStatus);
        questionnaireResponseEntry.getResponse().setLocation(questionnaireResponseLocation);

        var carePlanEntry = responseBundle.addEntry();
        carePlanEntry.setResponse(new Bundle.BundleEntryResponseComponent());
        carePlanEntry.getResponse().setStatus(carePlanStatus);
        carePlanEntry.getResponse().setLocation(carePlanLocation);

        return responseBundle;
    }

    private void setupSearchCarePlanByCprClient(CarePlan... carePlans) {
        setupSearchClient(2, 2, CarePlan.class, carePlans);

        if(carePlans.length > 0) {
            setupSearchQuestionnaireClient();
        }
    }

    private void setupSearchQuestionnaireClient(Questionnaire... questionnaires) {
        setupSearchClient(1, 0, Questionnaire.class, questionnaires);
    }

    private void setupSearchQuestionnaireResponseClient(int criteriaCount, QuestionnaireResponse... questionnaireResponses) {
        setupSearchClient(criteriaCount, 3, QuestionnaireResponse.class, questionnaireResponses);

        if(questionnaireResponses.length > 0) {
            PlanDefinition planDefinition = new PlanDefinition();
            planDefinition.setId(PLANDEFINITION_ID_1);
            setupSearchPlanDefinitionClient(planDefinition);
        }
    }

    private void setupSearchPlanDefinitionClient(PlanDefinition... planDefinitions) {
        setupSearchClient(1, 1, PlanDefinition.class, planDefinitions);
    }

    private void setupSearchClient(Class<? extends Resource> resourceClass, Resource... resources) {
        setupSearchClient(1, 0, resourceClass, resources);
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

    private void setupOrganization(String sorCode, String organizationId) {
        var organization = new Organization();
        organization.setId(organizationId);
        organization.addIdentifier().setSystem(Systems.SOR).setValue(sorCode);

        setupSearchOrganizationClient(organization);
    }

    private void setupSearchOrganizationClient(Organization... organizations) {
        setupSearchClient(Organization.class, organizations);
    }

    private void setupTransactionClient(Bundle responseBundle) {
        setupTransactionClient(responseBundle, SOR_CODE_1, ORGANIZATION_ID_1);
    }

    private void setupTransactionClient(Bundle responseBundle, String sorCode, String organizationId) {
        // TODO - decide on how to handle tagging, and comment this in!
        // setupUserContext(sorCode);
        // setupOrganization(sorCode, organizationId);

        Mockito.when(client.transaction().withBundle(Mockito.any(Bundle.class)).execute()).thenReturn(responseBundle);
    }

    private boolean isTagged(DomainResource resource) {
        return resource.getExtension().stream().anyMatch(e -> isOrganizationTag(e));
    }

    private boolean isTaggedWithId(DomainResource resource, String organizationId) {
        return resource.getExtension().stream().anyMatch(e -> isOrganizationTag(e) && isTagForOrganization(e, organizationId));
    }

    private boolean isOrganizationTag(Extension e) {
        return e.getUrl().equals(Systems.ORGANIZATION);
    }

    private boolean isTagForOrganization(Extension e, String organizationId) {
        return e.getValue() instanceof Reference && ((Reference) e.getValue()).getReference().equals(organizationId);
    }
}