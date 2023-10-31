package dk.kvalitetsit.hjemmebehandling.service;

import dk.kvalitetsit.hjemmebehandling.fhir.FhirClient;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirLookupResult;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirMapper;
import dk.kvalitetsit.hjemmebehandling.model.BaseModel;
import dk.kvalitetsit.hjemmebehandling.model.OrganizationModel;
import dk.kvalitetsit.hjemmebehandling.model.QualifiedId;
import dk.kvalitetsit.hjemmebehandling.service.exception.ServiceException;
import org.hl7.fhir.r4.model.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OrganizationService {
    private static final Logger logger = LoggerFactory.getLogger(OrganizationService.class);

    private final FhirClient fhirClient;

    private final FhirMapper fhirMapper;

    public OrganizationService(FhirClient fhirClient, FhirMapper fhirMapper) {
        this.fhirClient = fhirClient;
        this.fhirMapper = fhirMapper;
    }

    /**
     * @param id the id of the organisation
     * @returns the organisation if it exist. Otherwise it returns none
     * @throws ServiceException
     */
    public Optional<OrganizationModel> getOrganizationById(QualifiedId id) throws ServiceException  {
        FhirLookupResult lookupResult = fhirClient.lookupOrganizationById(id.toString());

        Optional<Organization> organization = lookupResult.getOrganization(id.toString());
        return organization.map(fhirMapper::mapOrganization);

    }

    /**
     * @returns a list of associated organizations
     * @throws ServiceException
     */
    public List<OrganizationModel> getOrganizations(String cpr) throws ServiceException {
        FhirLookupResult lookupResult = fhirClient.lookupActiveCarePlans(cpr);

        var carePlans = lookupResult.getCarePlans().stream().map((carePlan) -> fhirMapper.mapCarePlan(carePlan, lookupResult)).map(BaseModel::getOrganizationId).collect(Collectors.toList());

        var organizations = fhirClient.lookupOrganizations(carePlans);

        return organizations
                .getOrganizations()
                .stream()
                .map(fhirMapper::mapOrganization)
                .collect(Collectors.toList());


    }
}
