package dk.kvalitetsit.hjemmebehandling.controller;

import dk.kvalitetsit.hjemmebehandling.api.CarePlanDto;
import dk.kvalitetsit.hjemmebehandling.api.DtoMapper;
import dk.kvalitetsit.hjemmebehandling.controller.http.LocationHeaderBuilder;
import dk.kvalitetsit.hjemmebehandling.service.CarePlanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@Tag(name = "CarePlan", description = "API for retrieving CarePlans.")
public class CarePlanController {
    private static final Logger logger = LoggerFactory.getLogger(CarePlanController.class);

    private CarePlanService carePlanService;
    private DtoMapper dtoMapper;

    public CarePlanController(CarePlanService carePlanService, DtoMapper dtoMapper) {
        this.carePlanService = carePlanService;
        this.dtoMapper = dtoMapper;
    }

    @GetMapping(value = "/v1/careplan")
    public ResponseEntity<CarePlanDto> getActiveCarePlan() {
        // Should get cpr from user context and use that.

        throw new UnsupportedOperationException();
    }
}
