package dk.kvalitetsit.hjemmebehandling.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import dk.kvalitetsit.hjemmebehandling.constants.SearchParameters;
import dk.kvalitetsit.hjemmebehandling.constants.Systems;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FhirClient {
    private static final Logger logger = LoggerFactory.getLogger(FhirClient.class);

    private FhirContext context;
    private String endpoint;

    public FhirClient(FhirContext context, String endpoint) {
        this.context = context;
        this.endpoint = endpoint;
    }

    public FhirLookupResult lookupActiveCarePlans(String cpr) {
        var statusCriterion = CarePlan.STATUS.exactly().code(CarePlan.CarePlanStatus.ACTIVE.toCode());
        var cprCriterion = CarePlan.SUBJECT.hasChainedProperty("Patient", Patient.IDENTIFIER.exactly().systemAndValues(Systems.CPR, cpr));

        return lookupCarePlansByCriteria(List.of(statusCriterion, cprCriterion));
    }

    public FhirLookupResult lookupOrganizationById(String organizationId) {
        var idCriterion = Organization.RES_ID.exactly().code(organizationId);

        return lookupOrganizationsByCriteria(List.of(idCriterion));
    }

    public FhirLookupResult lookupQuestionnaireResponses(String carePlanId, List<String> questionnaireIds) {
        var questionnaireCriterion = QuestionnaireResponse.QUESTIONNAIRE.hasAnyOfIds(questionnaireIds);
        var basedOnCriterion = QuestionnaireResponse.BASED_ON.hasId(carePlanId);

        return lookupQuestionnaireResponsesByCriteria(List.of(questionnaireCriterion, basedOnCriterion));
    }

    public FhirLookupResult lookupQuestionnaireResponseById(String questionnaireResponseId) {
        var idCriterion = QuestionnaireResponse.RES_ID.exactly().code(questionnaireResponseId);

        return lookupQuestionnaireResponsesByCriteria(List.of(idCriterion));
    }

    public String saveQuestionnaireResponse(QuestionnaireResponse questionnaireResponse, CarePlan carePlan) {
        // Transfer organization tag from careplan
        addOrganizationTag(questionnaireResponse, ExtensionMapper.extractOrganizationId(carePlan.getExtension()));

        // Use a transaction to save the new response, along with the updated careplan.
        Bundle bundle = new BundleBuilder().buildQuestionnaireResponseBundle(questionnaireResponse, carePlan);
        return saveInTransaction(bundle, ResourceType.QuestionnaireResponse);
    }

    private FhirLookupResult lookupCarePlansByCriteria(List<ICriterion<?>> criteria) {
        var carePlanResult = lookupByCriteria(CarePlan.class, criteria, List.of(CarePlan.INCLUDE_SUBJECT, CarePlan.INCLUDE_INSTANTIATES_CANONICAL));

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

    public FhirLookupResult lookupQuestionnaires(Collection<String> questionnaireIds) {
        var idCriterion = Questionnaire.RES_ID.exactly().codes(questionnaireIds);

        return lookupByCriteria(Questionnaire.class, List.of(idCriterion));
    }

    private FhirLookupResult lookupPlanDefinitions(Collection<String> planDefinitionIds) {
        var idCriterion = PlanDefinition.RES_ID.exactly().codes(planDefinitionIds);

        return lookupByCriteria(PlanDefinition.class, List.of(idCriterion), List.of(PlanDefinition.INCLUDE_DEFINITION));
    }

    private FhirLookupResult lookupOrganizationsByCriteria(List<ICriterion<?>> criteria) {
        // Don't try to include Organization-resources when we are looking up organizations ...
        boolean withOrganizations = false;
        return lookupByCriteria(Organization.class, criteria, List.of(), withOrganizations, Optional.empty(), Optional.empty(), Optional.empty());
    }

    private FhirLookupResult lookupQuestionnaireResponsesByCriteria(List<ICriterion<?>> criteria) {
        var questionnaireResponseResult = lookupByCriteria(QuestionnaireResponse.class, criteria, List.of(QuestionnaireResponse.INCLUDE_BASED_ON, QuestionnaireResponse.INCLUDE_QUESTIONNAIRE, QuestionnaireResponse.INCLUDE_SUBJECT));

        // We also need the planDefinitions, which are found by following the chain QuestionnaireResponse.based-on -> CarePlan.instantiates-canonical.
        // This requires a separate lookup.
        if(questionnaireResponseResult.getQuestionnaireResponses().isEmpty()) {
            return questionnaireResponseResult;
        }

        // Get the related planDefinitions
        List<String> planDefinitionIds = getPlanDefinitionIds(questionnaireResponseResult.getCarePlans());
        FhirLookupResult planDefinitionResult = lookupPlanDefinitions(planDefinitionIds);

        // Merge the results
        questionnaireResponseResult.merge(planDefinitionResult);

        return questionnaireResponseResult;
    }


    private List<String> getQuestionnaireIds(List<CarePlan> carePlans) {
        return carePlans
                .stream()
                .flatMap(cp -> cp.getActivity().stream().map(a -> getQuestionnaireId(a.getDetail())))
                .collect(Collectors.toList());
    }

    private List<String> getPlanDefinitionIds(List<CarePlan> carePlans) {
        return carePlans
                .stream()
                .flatMap(cp -> cp.getInstantiatesCanonical().stream().map(ic -> ic.getValue()))
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
        boolean withOrganizations = true;
        return lookupByCriteria(resourceClass, criteria, includes, withOrganizations, Optional.empty(), Optional.empty(), Optional.empty());
    }

    private <T extends Resource> FhirLookupResult lookupByCriteria(Class<T> resourceClass, List<ICriterion<?>> criteria, List<Include> includes, boolean withOrganizations, Optional<SortSpec> sortSpec, Optional<Integer> offset, Optional<Integer> count) {
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
        FhirLookupResult lookupResult = FhirLookupResult.fromBundle(bundle);
        if(withOrganizations) {
            List<String> organizationIds = lookupResult.values()
                    .stream()
                    .map(r -> ExtensionMapper.tryExtractOrganizationId(r.getExtension()))
                    .filter(id -> id.isPresent())
                    .map(id -> id.get())
                    .distinct()
                    .collect(Collectors.toList());

            lookupResult = lookupResult.merge(lookupOrganizations(organizationIds));
        }
        return lookupResult;
    }

    public FhirLookupResult lookupOrganizations(List<String> organizationIds) {
        var idCriterion = Organization.RES_ID.exactly().codes(organizationIds);

        return lookupOrganizationsByCriteria(List.of(idCriterion));
    }

    public String saveInTransaction(Bundle transactionBundle, ResourceType resourceType) {
        IGenericClient client = context.newRestfulGenericClient(endpoint);

        // Execute the transaction
        var responseBundle = client.transaction().withBundle(transactionBundle).execute();

        // Locate the 'primary' entry in the response
        var id = "";
        for(var responseEntry : responseBundle.getEntry()) {
            var status = responseEntry.getResponse().getStatus();
            var location = responseEntry.getResponse().getLocation();
            if(status.startsWith("201") && location.startsWith(resourceType.toString())) {
                id = location.replaceFirst("/_history.*$", "");
            }
        }

        if(id.isEmpty()) {
            throw new IllegalStateException("Could not locate location-header in response when executing transaction.");
        }
        return id;
    }

    private void addOrganizationTag(DomainResource extendable, String organizationId) {
        if(extendable.getExtension().stream().anyMatch(e -> e.getUrl().equals(Systems.ORGANIZATION))) {
            throw new IllegalArgumentException(String.format("Trying to add organization tag to resource, but the tag was already present!", extendable.getId()));
        }

        extendable.addExtension(Systems.ORGANIZATION, new Reference(organizationId));
    }

    public List<Questionnaire> lookupVersionsOfQuestionnaireById(List<String> questionnaireIds) {

        IGenericClient client = context.newRestfulGenericClient(endpoint);

        List<Questionnaire> resources = new LinkedList<>();

        questionnaireIds.forEach( id -> {
            Bundle bundle = client.history().onInstance(new IdType("Questionnaire", id)).returnBundle(Bundle.class).execute();
            bundle.getEntry().forEach(x -> resources.add((Questionnaire) x.getResource()));
        });

        return resources;
    }

    public FhirLookupResult lookupValueSet(String organizationId) {
        var organizationCriterion = buildOrganizationCriterion(organizationId);

        return lookupByCriteria(ValueSet.class, List.of(organizationCriterion));
    }

    private ICriterion<?> buildOrganizationCriterion(String organizationId) {
        return new ReferenceClientParam(SearchParameters.ORGANIZATION).hasId(organizationId);
    }

    public FhirLookupResult lookupQuestionnaireResponsesForMultipleCarePlans(List<String> carePlanIds, List<String> questionnaireIds) {
        var questionnaireCriterion = QuestionnaireResponse.QUESTIONNAIRE.hasAnyOfIds(questionnaireIds);
        var basedOnCriterion = QuestionnaireResponse.BASED_ON.hasAnyOfIds(carePlanIds);

        return lookupQuestionnaireResponsesByCriteria(List.of(questionnaireCriterion, basedOnCriterion));
    }

    public Optional<Patient> lookupPatientByCPR(String cpr) {
        var cprCriterion = Patient.IDENTIFIER.exactly().systemAndValues(Systems.CPR, cpr);
        return lookupPatient(List.of(cprCriterion));
    }


    private Optional<Patient> lookupPatient(List<ICriterion<?>> criterion) {
        var lookupResult = lookupByCriteria(Patient.class, criterion);

        if(lookupResult.getPatients().isEmpty()) {
            return Optional.empty();
        }
        if(lookupResult.getPatients().size() > 1) {
            throw new IllegalStateException(String.format("Could not lookup single resource of class %s!", Patient.class));
        }
        return Optional.of(lookupResult.getPatients().get(0));
    }

}
