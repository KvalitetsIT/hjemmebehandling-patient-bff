package dk.kvalitetsit.hjemmebehandling.api;

import java.util.List;

public class MeasurementTypeDto {
    private String system;
    private String code;
    private String display;

    private ThresholdDto threshold;

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public ThresholdDto getThreshold() {
        return threshold;
    }

    public void setThreshold(ThresholdDto threshold) {
        this.threshold = threshold;
    }
}
