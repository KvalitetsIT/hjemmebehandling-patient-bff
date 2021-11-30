package dk.kvalitetsit.hjemmebehandling.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.DateClientParam;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import dk.kvalitetsit.hjemmebehandling.constants.ExaminationStatus;
import dk.kvalitetsit.hjemmebehandling.constants.SearchParameters;
import dk.kvalitetsit.hjemmebehandling.constants.Systems;
import dk.kvalitetsit.hjemmebehandling.context.UserContextProvider;
import org.checkerframework.checker.nullness.Opt;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.html.Option;
import java.sql.Date;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FhirClient {
    private static final Logger logger = LoggerFactory.getLogger(FhirClient.class);

    private FhirContext context;
    private String endpoint;
    private UserContextProvider userContextProvider;

    private static final List<ResourceType> UNTAGGED_RESOURCE_TYPES = List.of(ResourceType.Patient);

    public FhirClient(FhirContext context, String endpoint, UserContextProvider userContextProvider) {
        this.context = context;
        this.endpoint = endpoint;
        this.userContextProvider = userContextProvider;
    }

    public FhirLookupResult lookupCarePlansByPatientId(String patientId) {
        var patientCriterion = CarePlan.PATIENT.hasId(patientId);
        var organizationCriterion = buildOrganizationCriterion();

        return lookupCarePlansByCriteria(List.of(patientCriterion, organizationCriterion));
    }

    public FhirLookupResult lookupCarePlansUnsatisfiedAt(Instant pointInTime, int offset, int count) {
        // The criterion expresses that the careplan must no longer be satisfied at the given point in time.
        var satisfiedUntilCriterion = new DateClientParam(SearchParameters.CAREPLAN_SATISFIED_UNTIL).before().millis(Date.from(pointInTime));
        var organizationCriterion = buildOrganizationCriterion();

        var sortSpec = new SortSpec(SearchParameters.CAREPLAN_SATISFIED_UNTIL, SortOrderEnum.ASC);

        return lookupCarePlansByCriteria(List.of(satisfiedUntilCriterion, organizationCriterion), Optional.of(sortSpec), Optional.of(offset), Optional.of(count));
    }

    public FhirLookupResult lookupCarePlanById(String carePlanId) {
        var idCriterion = CarePlan.RES_ID.exactly().code(carePlanId);

        return lookupCarePlansByCriteria(List.of(idCriterion));
    }

    public Optional<Organization> lookupOrganizationBySorCode(String sorCode) {
        var sorCodeCriterion = Organization.IDENTIFIER.exactly().systemAndValues(Systems.SOR, sorCode);

        var lookupResult = lookupByCriteria(Organization.class, List.of(sorCodeCriterion));
        if(lookupResult.getOrganizations().isEmpty()) {
            return Optional.empty();
        }
        if(lookupResult.getOrganizations().size() > 1) {
            throw new IllegalStateException(String.format("Could not lookup single resource of class %s!", Organization.class));
        }
        return Optional.of(lookupResult.getOrganizations().get(0));
    }

    public Optional<Patient> lookupPatientById(String patientId) {
        var idCriterion = Patient.RES_ID.exactly().code(patientId);

        return lookupPatient(idCriterion);
    }

    public Optional<Patient> lookupPatientByCpr(String cpr) {
        var cprCriterion = Patient.IDENTIFIER.exactly().systemAndValues(Systems.CPR, cpr);

        return lookupPatient(cprCriterion);
    }

    public FhirLookupResult lookupQuestionnaireResponseById(String questionnaireResponseId) {
        var idCriterion = QuestionnaireResponse.RES_ID.exactly().code(questionnaireResponseId);
        return lookupQuestionnaireResponseByCriteria(List.of(idCriterion));
    }

    public FhirLookupResult lookupQuestionnaireResponses(String carePlanId, List<String> questionnaireIds) {
        var questionnaireCriterion = QuestionnaireResponse.QUESTIONNAIRE.hasAnyOfIds(questionnaireIds);
        var basedOnCriterion = QuestionnaireResponse.BASED_ON.hasId(carePlanId);

        return lookupQuestionnaireResponseByCriteria(List.of(questionnaireCriterion, basedOnCriterion));
    }

    public FhirLookupResult lookupQuestionnaireResponsesByStatus(List<ExaminationStatus> statuses) {
        var codes = statuses.stream().map(s -> s.toString()).collect(Collectors.toList());
        var statusCriterion = new TokenClientParam(SearchParameters.EXAMINATION_STATUS).exactly().codes(codes.toArray(new String[codes.size()]));
        var organizationCriterion = buildOrganizationCriterion();

        return lookupQuestionnaireResponseByCriteria(List.of(statusCriterion, organizationCriterion));
    }

    public FhirLookupResult lookupQuestionnaireResponsesByStatus(ExaminationStatus status) {
        return lookupQuestionnaireResponsesByStatus(List.of(status));
    }

    public String saveCarePlan(CarePlan carePlan) {
        return save(carePlan);
    }

    public String saveCarePlan(CarePlan carePlan, Patient patient) {
        // Build a transaction bundle.
        var bundle = new BundleBuilder().buildCarePlanBundle(carePlan, patient);

        return saveInTransaction(bundle, ResourceType.CarePlan);
    }

    public String savePatient(Patient patient) {
        return save(patient);
    }

    public String saveQuestionnaireResponse(QuestionnaireResponse questionnaireResponse) {
        return save(questionnaireResponse);
    }

    public FhirLookupResult lookupPlanDefinition(String planDefinitionId) {
        var idCriterion = PlanDefinition.RES_ID.exactly().code(planDefinitionId);

        return lookupPlanDefinitionsByCriteria(List.of(idCriterion));
    }

    public FhirLookupResult lookupPlanDefinitions() {
        var organizationCriterion = buildOrganizationCriterion();

        return lookupPlanDefinitionsByCriteria(List.of(organizationCriterion));
    }

    public FhirLookupResult lookupPlanDefinitions(Collection<String> planDefinitionIds) {
        var idCriterion = PlanDefinition.RES_ID.exactly().codes(planDefinitionIds);

        return lookupByCriteria(PlanDefinition.class, List.of(idCriterion));
    }

    public FhirLookupResult lookupQuestionnaires(Collection<String> questionnaireIds) {
        var idCriterion = Questionnaire.RES_ID.exactly().codes(questionnaireIds);
        var organizationCriterion = buildOrganizationCriterion();

        return lookupByCriteria(Questionnaire.class, List.of(idCriterion, organizationCriterion));
    }

    public void updateCarePlan(CarePlan carePlan) {
        update(carePlan);
    }

    public void updateQuestionnaireResponse(QuestionnaireResponse questionnaireResponse) {
        update(questionnaireResponse);
    }

    private FhirLookupResult lookupCarePlansByCriteria(List<ICriterion<?>> criteria) {
        return lookupCarePlansByCriteria(criteria, Optional.empty(), Optional.empty(), Optional.empty());
    }

    private FhirLookupResult lookupCarePlansByCriteria(List<ICriterion<?>> criteria, Optional<SortSpec> sortSpec, Optional<Integer> offset, Optional<Integer> count) {
        var carePlanResult = lookupByCriteria(CarePlan.class, criteria, List.of(CarePlan.INCLUDE_SUBJECT, CarePlan.INCLUDE_INSTANTIATES_CANONICAL), sortSpec, offset, count);

        // The FhirLookupResult includes the patient- and plandefinition-resources that we need,
        // but due to limitations of the FHIR server, not the questionnaire-resources. Se wo look up those in a separate call.
        if(carePlanResult.getCarePlans().isEmpty()) {
            return carePlanResult;
        }

        // Get the related questionnaire-resources
        List<String> questionnaireIds = getQuestionnaireIds(carePlanResult.getCarePlans());
        FhirLookupResult questionnaireResult = lookupQuestionnaires(questionnaireIds);

        // Merge the results
        return carePlanResult.merge(questionnaireResult);
    }

    private Optional<Patient> lookupPatient(ICriterion<?> criterion) {
        var lookupResult = lookupByCriteria(Patient.class, List.of(criterion));

        if(lookupResult.getPatients().isEmpty()) {
            return Optional.empty();
        }
        if(lookupResult.getPatients().size() > 1) {
            throw new IllegalStateException(String.format("Could not lookup single resource of class %s!", Patient.class));
        }
        return Optional.of(lookupResult.getPatients().get(0));
    }

    private FhirLookupResult lookupPlanDefinitionsByCriteria(List<ICriterion<?>> criteria) {
        // Includes the Questionnaire resources.
        return lookupByCriteria(PlanDefinition.class, criteria, List.of(PlanDefinition.INCLUDE_DEFINITION));
    }

    private FhirLookupResult lookupQuestionnaireResponseByCriteria(List<ICriterion<?>> criteria) {
        return lookupByCriteria(QuestionnaireResponse.class, criteria, List.of(QuestionnaireResponse.INCLUDE_BASED_ON, QuestionnaireResponse.INCLUDE_QUESTIONNAIRE, QuestionnaireResponse.INCLUDE_SUBJECT));
    }

    private List<String> getQuestionnaireIds(List<CarePlan> carePlans) {
        return carePlans
                .stream()
                .flatMap(cp -> cp.getActivity().stream().map(a -> getQuestionnaireId(a.getDetail())))
                .collect(Collectors.toList());
    }

    private String getQuestionnaireId(CarePlan.CarePlanActivityDetailComponent detail) {
        if(detail.getInstantiatesCanonical() == null || detail.getInstantiatesCanonical().size() != 1) {
            throw new IllegalStateException("Expected InstantiatesCanonical to be present, and to contain exactly one value!");
        }
        return detail.getInstantiatesCanonical().get(0).getValue();
    }

    private <T extends Resource> FhirLookupResult lookupByCriteria(Class<T> resourceClass, List<ICriterion<?>> criteria) {
        return lookupByCriteria(resourceClass, criteria, null);
    }

    private <T extends Resource> FhirLookupResult lookupByCriteria(Class<T> resourceClass, List<ICriterion<?>> criteria, List<Include> includes) {
        return lookupByCriteria(resourceClass, criteria, includes, Optional.empty(), Optional.empty(), Optional.empty());
    }

    private <T extends Resource> FhirLookupResult lookupByCriteria(Class<T> resourceClass, List<ICriterion<?>> criteria, List<Include> includes, Optional<SortSpec> sortSpec, Optional<Integer> offset, Optional<Integer> count) {
        IGenericClient client = context.newRestfulGenericClient(endpoint);

        var query = client
                .search()
                .forResource(resourceClass);
        if(criteria != null && criteria.size() > 0) {
            query = query.where(criteria.get(0));
            for(int i = 1; i < criteria.size(); i++) {
                query = query.and(criteria.get(i));
            }
        }
        if(includes != null) {
            for(var include : includes) {
                query = query.include(include);
            }
        }
        if(sortSpec.isPresent()) {
            query = query.sort(sortSpec.get());
        }
        if(offset.isPresent()) {
            query = query.offset(offset.get());
        }
        if(count.isPresent()) {
            query = query.count(count.get());
        }

        Bundle bundle = (Bundle) query.execute();
        return FhirLookupResult.fromBundle(bundle);
    }

    private <T extends Resource> String save(Resource resource) {
        addOrganizationTag(resource);

        IGenericClient client = context.newRestfulGenericClient(endpoint);

        MethodOutcome outcome = client.create().resource(resource).execute();
        if(!outcome.getCreated()) {
            throw new IllegalStateException(String.format("Tried to create resource of type %s, but it was not created!", resource.getResourceType().name()));
        }
        return outcome.getId().toUnqualifiedVersionless().getIdPart();
    }

    private String saveInTransaction(Bundle transactionBundle, ResourceType resourceType) {
        addOrganizationTag(transactionBundle);

        IGenericClient client = context.newRestfulGenericClient(endpoint);

        // Execute the transaction
        var responseBundle = client.transaction().withBundle(transactionBundle).execute();

        // Locate the 'primary' entry in the response
        var id = "";
        for(var responseEntry : responseBundle.getEntry()) {
            var status = responseEntry.getResponse().getStatus();
            var location = responseEntry.getResponse().getLocation();
            if(!status.startsWith("201")) {
                throw new IllegalStateException(String.format("Creating %s failed. Received unwanted http statuscode: %s", resourceType, status));
            }
            if(location.startsWith(resourceType.toString())) {
                id = location.replaceFirst("/_history.*$", "");
            }
        }

        if(id.isEmpty()) {
            throw new IllegalStateException("Could not locate location-header in response when executing transaction.");
        }
        return id;
    }

    private <T extends Resource> void update(Resource resource) {
        IGenericClient client = context.newRestfulGenericClient(endpoint);
        client.update().resource(resource).execute();
    }

    private void addOrganizationTag(Resource resource) {
        if(resource.getResourceType() == ResourceType.Bundle) {
            addOrganizationTag((Bundle) resource);
        }
        if(resource instanceof DomainResource) {
            addOrganizationTag((DomainResource) resource);
        }
        else {
            throw new IllegalArgumentException(String.format("Trying to add organization tag to resource %s, but the resource was of incorrect type %s!", resource.getId(), resource.getResourceType()));
        }
    }

    private void addOrganizationTag(Bundle bundle) {
        for(var entry : bundle.getEntry()) {
            addOrganizationTag(entry.getResource());
        }
    }

    private void addOrganizationTag(DomainResource extendable) {
        if(excludeFromOrganizationTagging(extendable)) {
            return;
        }
        if(extendable.getExtension().stream().anyMatch(e -> e.getUrl().equals(Systems.ORGANIZATION))) {
            throw new IllegalArgumentException(String.format("Trying to add organization tag to resource, but the tag was already present!", extendable.getId()));
        }

        extendable.addExtension(Systems.ORGANIZATION, new Reference(getOrganizationId()));
    }

    private boolean excludeFromOrganizationTagging(DomainResource extendable) {
        return UNTAGGED_RESOURCE_TYPES.contains(extendable.getResourceType());
    }

    private ICriterion<?> buildOrganizationCriterion() {
        String organizationId = getOrganizationId();
        return new ReferenceClientParam(SearchParameters.ORGANIZATION).hasId(organizationId);
    }

    private String getOrganizationId() {
        var context = userContextProvider.getUserContext();
        if(context == null) {
            throw new IllegalStateException("UserContext was not initialized!");
        }

        var organization = lookupOrganizationBySorCode(context.getOrgId())
                .orElseThrow(() -> new IllegalStateException(String.format("No Organization was present for sorCode %s!", context.getOrgId())));

        var organizationId = organization.getIdElement().toUnqualifiedVersionless().getValue();
        return FhirUtils.qualifyId(organizationId, ResourceType.Organization);
    }
}
