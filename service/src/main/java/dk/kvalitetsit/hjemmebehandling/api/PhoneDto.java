package dk.kvalitetsit.hjemmebehandling.api;

import java.util.Optional;

public class PhoneDto {
    private String primary;

    private String secondary;

    public PhoneDto(String primary, String secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    public PhoneDto() {
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
