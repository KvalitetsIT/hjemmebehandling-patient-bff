package dk.kvalitetsit.hjemmebehandling.model;

import java.util.List;

public class OrganizationModel extends BaseModel {
    private String name;
    private String street;
    private String postalCode;
    private String city;
    private String country;
    private String phone;
    private List<PhoneHourModel> phoneHours;

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

    public List<PhoneHourModel> getPhoneHours() {
        return phoneHours;
    }

    public void setPhoneHours(List<PhoneHourModel> phoneHours) {
        this.phoneHours = phoneHours;
    }
}
