package dk.kvalitetsit.hjemmebehandling.model;

public class MeasurementTypeModel {
    private String system;
    private String code;
    private String display;

    private ThresholdModel threshold;

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

    public ThresholdModel getThreshold() {
        return threshold;
    }

    public void setThreshold(ThresholdModel threshold) {
        this.threshold = threshold;
    }
}
