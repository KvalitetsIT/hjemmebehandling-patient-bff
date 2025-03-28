package dk.kvalitetsit.hjemmebehandling.model;

public class PhoneModel {

    private String primary;

    private String secondary;

    public PhoneModel(String primary, String secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    public PhoneModel() {
    }

    public String getPrimary() {
        return primary;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }

    public String getSecondary() {
        return secondary;
    }

    public void setSecondary(String secondary) {
        this.secondary = secondary;
    }

    @Override
    public String toString() {
        return "PhoneDto{" +
                "primary='" + primary + '\'' +
                ", secondary=" + secondary +
                '}';
    }

}
