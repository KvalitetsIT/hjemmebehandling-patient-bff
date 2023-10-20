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
import java.util.stream.Collectors;

@Component
public class CarePlanService extends AccessValidatingService {
    private FhirClient fhirClient;

    private FhirMapper fhirMapper;

    public CarePlanService(FhirClient fhirClient, FhirMapper fhirMapper, AccessValidator accessValidator) {
        super(accessValidator);

        this.fhirClient = fhirClient;
        this.fhirMapper = fhirMapper;
    }

    public List<CarePlanModel> getActiveCarePlans(String cpr) throws ServiceException, AccessValidationException {
        FhirLookupResult lookupResult = fhirClient.lookupActiveCarePlans(cpr);
        List<CarePlan> carePlans = lookupResult.getCarePlans();

        validateCorrectSubject(lookupResult);
        return carePlans.stream().map((carePlan) -> fhirMapper.mapCarePlan(carePlan, lookupResult)).collect(Collectors.toList());
    }
}
