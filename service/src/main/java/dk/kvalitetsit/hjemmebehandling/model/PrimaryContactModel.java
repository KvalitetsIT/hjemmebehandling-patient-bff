package dk.kvalitetsit.hjemmebehandling.model;

import dk.kvalitetsit.hjemmebehandling.api.ContactDetailsDto;

public class PrimaryContactModel {

    private String name;
    private String affiliation;
    private ContactDetailsModel contactDetails;



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
}
