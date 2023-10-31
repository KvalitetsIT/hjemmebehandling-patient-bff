package dk.kvalitetsit.hjemmebehandling.controller;

import dk.kvalitetsit.hjemmebehandling.api.DtoMapper;
import dk.kvalitetsit.hjemmebehandling.api.OrganizationDto;
import dk.kvalitetsit.hjemmebehandling.constants.errors.ErrorDetails;
import dk.kvalitetsit.hjemmebehandling.context.UserContextProvider;
import dk.kvalitetsit.hjemmebehandling.controller.exception.BadRequestException;
import dk.kvalitetsit.hjemmebehandling.controller.exception.ResourceNotFoundException;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirUtils;
import dk.kvalitetsit.hjemmebehandling.model.OrganizationModel;
import dk.kvalitetsit.hjemmebehandling.model.QualifiedId;
import dk.kvalitetsit.hjemmebehandling.service.OrganizationService;
import dk.kvalitetsit.hjemmebehandling.service.exception.ServiceException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hl7.fhir.r4.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@Tag(name = "Organization", description = "API for retrieving Organizations.")
public class OrganizationController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(OrganizationController.class);

    private final OrganizationService organizationService;
    private final DtoMapper dtoMapper;
    private final UserContextProvider userContextProvider;

    public OrganizationController(OrganizationService organizationService, DtoMapper dtoMapper, UserContextProvider userContextProvider) {
        this.organizationService = organizationService;
        this.dtoMapper = dtoMapper;
        this.userContextProvider = userContextProvider;
    }

    @GetMapping(value = "/v1/organizations/{id}")
    public ResponseEntity<OrganizationDto> getOrganization(@PathVariable("id") String id) {
        // Look up the Organization
        Optional<OrganizationModel> organization = Optional.empty();

        try {
            organization = organizationService.getOrganizationById(new QualifiedId(FhirUtils.qualifyId(id, ResourceType.Organization)));
        }
        catch(ServiceException e) {
            logger.error("Could not retrieve QuestionnaireResponse", e);
            throw toStatusCodeException(e);
        }
        if(organization.isEmpty()) {
            throw new ResourceNotFoundException(String.format("Organization with id %s not found.", id), ErrorDetails.ORGANIZATION_DOES_NOT_EXIST);
        }
        return ResponseEntity.ok(dtoMapper.mapOrganizationModel(organization.get()));
    }

    @GetMapping(value = "/v1/organizations")
    public ResponseEntity<List<OrganizationDto>> getOrganizations() {
        String cpr = this.userContextProvider.getUserContext().getCpr();

        if(cpr == null || cpr.isEmpty()) {
            throw new BadRequestException(ErrorDetails.MISSING_CONTEXT);
        }

        try {
            return ResponseEntity.ok(organizationService.getOrganizations(cpr)
                    .stream()
                    .map(dtoMapper::mapOrganizationModel)
                    .collect(Collectors.toList())
            );
        } catch(ServiceException e) {
            logger.error("Could not retrieve QuestionnaireResponse", e);
            throw toStatusCodeException(e);
        }

    }

}
