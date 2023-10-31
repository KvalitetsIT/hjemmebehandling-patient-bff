package dk.kvalitetsit.hjemmebehandling.model;

import dk.kvalitetsit.hjemmebehandling.api.ContactDetailsDto;

public class PrimaryContactModel {

    private String name;
    private String affiliation;
    private ContactDetailsModel contactDetails;

    /**
     * The id of the organisation of which this contact is associated
     */
    private String organisation;

    public PrimaryContactModel(String name, String affiliation, ContactDetailsModel contactDetails, String organisation) {
        this.name = name;
        this.affiliation = affiliation;
        this.contactDetails = contactDetails;
        this.organisation = organisation;
    }

    public PrimaryContactModel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public ContactDetailsModel getContactDetails() {
        return contactDetails;
    }

    public void setContactDetails(ContactDetailsModel contactDetails) {
        this.contactDetails = contactDetails;
    }

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }


    @Override
    public String toString() {
        return "PrimaryContactModel{" +
                "name='" + name + '\'' +
                ", affiliation='" + affiliation + '\'' +
                ", contactDetails=" + contactDetails +
                ", organisation='" + organisation + '\'' +
                '}';
    }
}
