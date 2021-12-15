package dk.kvalitetsit.hjemmebehandling.controller;

import dk.kvalitetsit.hjemmebehandling.api.CarePlanDto;
import dk.kvalitetsit.hjemmebehandling.api.DtoMapper;
import dk.kvalitetsit.hjemmebehandling.constants.errors.ErrorDetails;
import dk.kvalitetsit.hjemmebehandling.context.UserContextProvider;
import dk.kvalitetsit.hjemmebehandling.controller.exception.ResourceNotFoundException;
import dk.kvalitetsit.hjemmebehandling.model.CarePlanModel;
import dk.kvalitetsit.hjemmebehandling.service.CarePlanService;
import dk.kvalitetsit.hjemmebehandling.service.exception.ServiceException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@Tag(name = "CarePlan", description = "API for retrieving CarePlans.")
public class CarePlanController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(CarePlanController.class);

    private CarePlanService carePlanService;
    private DtoMapper dtoMapper;
    private UserContextProvider userContextProvider;

    public CarePlanController(CarePlanService carePlanService, DtoMapper dtoMapper, UserContextProvider userContextProvider) {
        this.carePlanService = carePlanService;
        this.dtoMapper = dtoMapper;
        this.userContextProvider = userContextProvider;
    }

    @GetMapping(value = "/v1/careplan/active")
    public ResponseEntity<CarePlanDto> getActiveCarePlan() {
        String cpr = userContextProvider.getUserContext().getCpr();

        Optional<CarePlanModel> carePlan = Optional.empty();

        try {
            carePlan = carePlanService.getActiveCarePlan(cpr);
        }
        catch(ServiceException e) {
            logger.error("Could not update questionnaire response", e);
            throw toStatusCodeException(e);
        }

        if(!carePlan.isPresent()) {
            throw new ResourceNotFoundException("No active careplan exists for the current user.", ErrorDetails.NO_ACTIVE_CAREPLAN_EXISTS);
        }
        return ResponseEntity.ok(dtoMapper.mapCarePlanModel(carePlan.get()));
    }
}
