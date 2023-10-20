package dk.kvalitetsit.hjemmebehandling.api;

public class PrimaryContactDto {

    private String name;
    private String affiliation;
    private ContactDetailsDto contactDetails;

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

    public ContactDetailsDto getContactDetails() {
        return contactDetails;
    }

    public void setContactDetails(ContactDetailsDto contactDetails) {
        this.contactDetails = contactDetails;
    }

}