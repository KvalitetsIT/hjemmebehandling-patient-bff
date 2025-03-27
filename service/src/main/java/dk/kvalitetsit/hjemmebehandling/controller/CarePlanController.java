package dk.kvalitetsit.hjemmebehandling.controller;

import dk.kvalitetsit.hjemmebehandling.api.CarePlanDto;
import dk.kvalitetsit.hjemmebehandling.api.DtoMapper;
import dk.kvalitetsit.hjemmebehandling.constants.errors.ErrorDetails;
import dk.kvalitetsit.hjemmebehandling.context.UserContextProvider;
import dk.kvalitetsit.hjemmebehandling.controller.exception.BadRequestException;
import dk.kvalitetsit.hjemmebehandling.controller.exception.ResourceNotFoundException;
import dk.kvalitetsit.hjemmebehandling.model.CarePlanModel;
import dk.kvalitetsit.hjemmebehandling.service.CarePlanService;
import dk.kvalitetsit.hjemmebehandling.service.exception.AccessValidationException;
import dk.kvalitetsit.hjemmebehandling.service.exception.ServiceException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Tag(name = "CarePlan", description = "API for retrieving CarePlans.")
public class CarePlanController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(CarePlanController.class);

    private final CarePlanService carePlanService;
    private final DtoMapper dtoMapper;
    private final UserContextProvider userContextProvider;

    public CarePlanController(CarePlanService carePlanService, DtoMapper dtoMapper, UserContextProvider userContextProvider) {
        this.carePlanService = carePlanService;
        this.dtoMapper = dtoMapper;
        this.userContextProvider = userContextProvider;
    }

    @GetMapping(value = "/v1/careplans/active")
    public ResponseEntity<List<CarePlanDto>> getActiveCarePlans() {

        // TODO: handle 'Optional.get()' without 'isPresent()' check
        String cpr = userContextProvider.getUserContext().getCpr().get();

        if(cpr == null || cpr.isEmpty()) {
            throw new BadRequestException(ErrorDetails.MISSING_CONTEXT);
        }

        try {
            List<CarePlanModel> carePlans = carePlanService.getActiveCarePlans(cpr);

            if(carePlans.isEmpty()) {
                throw new ResourceNotFoundException("No active careplans exists for the current user.", ErrorDetails.NO_ACTIVE_CAREPLAN_EXISTS);
            }

            return ResponseEntity.ok(carePlans
                    .stream()
                    .map(dtoMapper::mapCarePlanModel)
                    .collect(Collectors.toList()));
        }
        catch(AccessValidationException | ServiceException e) {
            logger.error("Could not update questionnaire response", e);
            throw toStatusCodeException(e);
        }

    }
}
