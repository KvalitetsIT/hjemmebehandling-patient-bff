package dk.kvalitetsit.hjemmebehandling.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class OrganizationDto {
    private String id;
    private String name;
    private String street;
    private String postalCode;
    private String city;
    private String country;
    private String phone;
    private List<PhoneHourDto> phoneHours;

    @Schema(required = true, description = "Id of the resource", example = "CarePlan/10")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<PhoneHourDto> getPhoneHours() {
        return phoneHours;
    }

    public void setPhoneHours(List<PhoneHourDto> phoneHours) {
        this.phoneHours = phoneHours;
    }
}
