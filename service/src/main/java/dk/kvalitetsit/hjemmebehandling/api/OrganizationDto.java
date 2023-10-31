package dk.kvalitetsit.hjemmebehandling.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class OrganizationDto {
    private String id;
    private String name;
    private ContactDetailsDto contactDetails;
    private List<PhoneHourDto> phoneHours;


    public OrganizationDto(String id, String name, ContactDetailsDto contactDetails, List<PhoneHourDto> phoneHours) {
        this.id = id;
        this.name = name;
        this.contactDetails = contactDetails;
        this.phoneHours = phoneHours;
    }

    public OrganizationDto() {
    }

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


    public List<PhoneHourDto> getPhoneHours() {
        return phoneHours;
    }

    public void setPhoneHours(List<PhoneHourDto> phoneHours) {
        this.phoneHours = phoneHours;
    }

    public ContactDetailsDto getContactDetails() {
        return contactDetails;
    }

    public void setContactDetails(ContactDetailsDto contactDetails) {
        this.contactDetails = contactDetails;
    }


    @Override
    public String toString() {
        return "OrganizationDto{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", contactDetails=" + contactDetails +
                ", phoneHours=" + phoneHours +
                '}';
    }
}
