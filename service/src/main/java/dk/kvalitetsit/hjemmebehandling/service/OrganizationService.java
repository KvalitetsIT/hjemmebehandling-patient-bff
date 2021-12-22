package dk.kvalitetsit.hjemmebehandling.service;

import dk.kvalitetsit.hjemmebehandling.fhir.FhirClient;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirLookupResult;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirMapper;
import dk.kvalitetsit.hjemmebehandling.model.OrganizationModel;
import dk.kvalitetsit.hjemmebehandling.model.QualifiedId;
import dk.kvalitetsit.hjemmebehandling.service.exception.ServiceException;
import org.hl7.fhir.r4.model.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrganizationService {
    private static final Logger logger = LoggerFactory.getLogger(OrganizationService.class);

    private FhirClient fhirClient;

    private FhirMapper fhirMapper;

    public OrganizationService(FhirClient fhirClient, FhirMapper fhirMapper) {
        this.fhirClient = fhirClient;
        this.fhirMapper = fhirMapper;
    }

    public Optional<OrganizationModel> getOrganizationById(QualifiedId id) throws ServiceException  {
        FhirLookupResult lookupResult = fhirClient.lookupOrganizationById(id.toString());

        Optional<Organization> organization = lookupResult.getOrganization(id.toString());
        if(!organization.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(fhirMapper.mapOrganization(organization.get()));
    }
}
