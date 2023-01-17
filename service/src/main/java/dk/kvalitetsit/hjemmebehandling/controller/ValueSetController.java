package dk.kvalitetsit.hjemmebehandling.controller;

import dk.kvalitetsit.hjemmebehandling.api.DtoMapper;
import dk.kvalitetsit.hjemmebehandling.api.MeasurementTypeDto;
import dk.kvalitetsit.hjemmebehandling.api.ThresholdDto;
import dk.kvalitetsit.hjemmebehandling.model.MeasurementTypeModel;
import dk.kvalitetsit.hjemmebehandling.service.ValueSetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Tag(name = "ValueSet", description = "API for retrieving various valuesets, such as measurement types, supported by the system.")
public class ValueSetController extends BaseController {
  private static final Logger logger = LoggerFactory.getLogger(ValueSetController.class);

  private ValueSetService valueSetService;
  private DtoMapper dtoMapper;

  public ValueSetController(ValueSetService valueSetService, DtoMapper dtoMapper) {
    this.valueSetService = valueSetService;
    this.dtoMapper = dtoMapper;
  }

  @Operation(summary = "Get measurement types.", description = "Retrieves list of measurement types supported by the requestors organization.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successful operation.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = MeasurementTypeDto.class))))
  })
  @GetMapping(value = "/v1/{organizationId}/measurementtype")
  public ResponseEntity<List<MeasurementTypeDto>> getMeasurementTypes(@PathVariable("organizationId") String organizationId) {
    List<MeasurementTypeModel> measurementTypes = valueSetService.getMeasurementTypes(organizationId);

    return ResponseEntity.ok(measurementTypes.stream().map(mt -> dtoMapper.mapMeasurementTypeModel(mt)).collect(Collectors.toList()));
  }

  @Operation(summary = "Get thresholds for measurement types.", description = "Retrieves list of thresholds for measurement types supported by the requestors organization.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Successful operation.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ThresholdDto.class))))
  })
  @GetMapping(value = "/v1/{organizationId}/measurementtype/threshold")
  public ResponseEntity<List<ThresholdDto>> getMeasurementTypeThresholds(@PathVariable("organizationId") String organizationId) {
    List<MeasurementTypeModel> measurementTypes = valueSetService.getMeasurementTypes(organizationId);

    return ResponseEntity.ok(measurementTypes.stream().map(mt -> dtoMapper.mapThresholdModel(mt.getThreshold())).collect(Collectors.toList()));
  }
}
