package dk.kvalitetsit.hjemmebehandling.model;

import java.util.List;

public class OrganizationModel {
    private QualifiedId id;
    private String name;
    private ContactDetailsModel contactDetails;
    private List<PhoneHourModel> phoneHours;

    /**
     * Some blob of html specifying the phone hours and other details associated to the contact of the organisation
     */
    private String blob;

    public OrganizationModel(QualifiedId id, String name, ContactDetailsModel contactDetails, List<PhoneHourModel> phoneHours, String blob) {
        this.id = id;
        this.name = name;
        this.contactDetails = contactDetails;
        this.phoneHours = phoneHours;
        this.blob = blob;
    }

    public OrganizationModel() {
    }

    public QualifiedId getId() {
        return id;
    }

    public void setId(QualifiedId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ContactDetailsModel getContactDetails() {
        return contactDetails;
    }

    public void setContactDetails(ContactDetailsModel contactDetails) {
        this.contactDetails = contactDetails;
    }

    public List<PhoneHourModel> getPhoneHours() {
        return phoneHours;
    }

    public void setPhoneHours(List<PhoneHourModel> phoneHours) {
        this.phoneHours = phoneHours;
    }


    @Override
    public String toString() {
        return "OrganizationModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", contactDetails=" + contactDetails +
                ", phoneHours=" + phoneHours +
                '}';
    }

    public String getBlob() {
        return blob;
    }

    public void setBlob(String blob) {
        this.blob = blob;
    }
}
