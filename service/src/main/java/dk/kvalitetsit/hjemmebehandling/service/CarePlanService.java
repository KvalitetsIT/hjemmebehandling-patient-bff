package dk.kvalitetsit.hjemmebehandling.service;

import dk.kvalitetsit.hjemmebehandling.fhir.FhirClient;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirMapper;
import dk.kvalitetsit.hjemmebehandling.model.CarePlanModel;
import dk.kvalitetsit.hjemmebehandling.service.access.AccessValidator;
import dk.kvalitetsit.hjemmebehandling.service.exception.ServiceException;
import org.springframework.stereotype.Component;

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

    public Optional<CarePlanModel> getActiveCarePlan(String cpr) throws ServiceException {
        throw new UnsupportedOperationException();
    }
}
