package dk.kvalitetsit.hjemmebehandling.controller;

import dk.kvalitetsit.hjemmebehandling.api.DtoMapper;
import dk.kvalitetsit.hjemmebehandling.model.MeasurementTypeModel;
import dk.kvalitetsit.hjemmebehandling.service.ValueSetService;
import org.openapitools.api.ValueSetApi;
import org.openapitools.model.MeasurementTypeDto;
import org.openapitools.model.ThresholdDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ValueSetController extends BaseController implements ValueSetApi {
    private static final Logger logger = LoggerFactory.getLogger(ValueSetController.class);

    private final ValueSetService valueSetService;
    private final DtoMapper dtoMapper;

    public ValueSetController(ValueSetService valueSetService, DtoMapper dtoMapper) {
        this.valueSetService = valueSetService;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public ResponseEntity<List<ThresholdDto>> getMeasurementTypeThresholds(String organizationId) {
        List<MeasurementTypeModel> measurementTypes = valueSetService.getMeasurementTypes(organizationId);
        return ResponseEntity.ok(measurementTypes.stream().map(mt -> dtoMapper.mapThresholdModel(mt.getThreshold())).toList());
    }

    @Override
    public ResponseEntity<List<MeasurementTypeDto>> getMeasurementTypes(String organizationId) {
        List<MeasurementTypeModel> measurementTypes = valueSetService.getMeasurementTypes(organizationId);
        return ResponseEntity.ok(measurementTypes.stream().map(dtoMapper::mapMeasurementTypeModel).toList());
    }

}
