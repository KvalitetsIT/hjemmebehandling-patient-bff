package dk.kvalitetsit.hjemmebehandling.service;

import dk.kvalitetsit.hjemmebehandling.fhir.FhirClient;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirLookupResult;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirMapper;
import dk.kvalitetsit.hjemmebehandling.model.CarePlanModel;
import dk.kvalitetsit.hjemmebehandling.service.access.AccessValidator;
import dk.kvalitetsit.hjemmebehandling.service.exception.AccessValidationException;
import dk.kvalitetsit.hjemmebehandling.service.exception.ServiceException;
import org.hl7.fhir.r4.model.CarePlan;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CarePlanService extends AccessValidatingService {
    private FhirClient fhirClient;

    private FhirMapper fhirMapper;

    public CarePlanService(FhirClient fhirClient, FhirMapper fhirMapper, AccessValidator accessValidator) {
        super(accessValidator);

        this.fhirClient = fhirClient;
        this.fhirMapper = fhirMapper;
    }

    public Optional<CarePlanModel> getActiveCarePlan(String cpr) throws ServiceException, AccessValidationException {
        FhirLookupResult lookupResult = fhirClient.lookupActiveCarePlan(cpr);
        List<CarePlan> carePlans = lookupResult.getCarePlans();
        if(carePlans.size() > 1) {
            throw new IllegalStateException(String.format("Expected to look up zero or one active careplan for cpr %s, got %s!", cpr, carePlans.size()));
        }
        if(carePlans.isEmpty()) {
            return Optional.empty();
        }

        validateCorrectSubject(lookupResult);
        return Optional.of(fhirMapper.mapCarePlan(carePlans.get(0), lookupResult));
    }
}
