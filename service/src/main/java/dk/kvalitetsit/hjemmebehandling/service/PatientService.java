package dk.kvalitetsit.hjemmebehandling.service;

import dk.kvalitetsit.hjemmebehandling.constants.errors.ErrorDetails;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirClient;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirLookupResult;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirMapper;
import dk.kvalitetsit.hjemmebehandling.model.PatientModel;
import dk.kvalitetsit.hjemmebehandling.service.exception.ErrorKind;
import dk.kvalitetsit.hjemmebehandling.service.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PatientService {
    private static final Logger logger = LoggerFactory.getLogger(OrganizationService.class);

    private final FhirClient fhirClient;

    private final FhirMapper fhirMapper;

    public PatientService(FhirClient fhirClient, FhirMapper fhirMapper) {
        this.fhirClient = fhirClient;
        this.fhirMapper = fhirMapper;
    }

    public PatientModel getPatient(String cpr) throws ServiceException {

        var patient = fhirClient.lookupPatientByCPR(cpr);
        if (patient.isEmpty()) {
            throw new ServiceException("Could not retrieve patient with the specified cpr", ErrorKind.NOT_FOUND, ErrorDetails.PATIENT_NOT_FOUND);
        }
        return fhirMapper.mapPatient(patient.get());

    }
}
