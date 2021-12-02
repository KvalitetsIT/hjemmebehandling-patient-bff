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

    public FhirClient(FhirContext context, String endpoint) {
        this.context = context;
        this.endpoint = endpoint;
    }

    public FhirLookupResult lookupQuestionnaireResponses(String carePlanId) {
        var basedOnCriterion = QuestionnaireResponse.BASED_ON.hasId(carePlanId);

        return lookupQuestionnaireResponseByCriteria(List.of(basedOnCriterion));
    }

    private FhirLookupResult lookupQuestionnaireResponseByCriteria(List<ICriterion<?>> criteria) {
        return lookupByCriteria(QuestionnaireResponse.class, criteria, List.of(QuestionnaireResponse.INCLUDE_BASED_ON, QuestionnaireResponse.INCLUDE_QUESTIONNAIRE, QuestionnaireResponse.INCLUDE_SUBJECT));
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
}
