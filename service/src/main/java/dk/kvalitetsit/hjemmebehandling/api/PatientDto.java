package dk.kvalitetsit.hjemmebehandling.api;

import java.util.List;

public class PatientDto {
    private String givenName;
    private String familyName;
    private String cpr;
    private String customUserName;
    private ContactDetailsDto contactsDetails;
    private List<PrimaryContactDto> primaryContacts;


    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getCpr() {
        return cpr;
    }

    public void setCpr(String cpr) {
        this.cpr = cpr;
    }

    public String getCustomUserName() {
        return customUserName;
    }

    public void setCustomUserName(String customUserName) {
        this.customUserName = customUserName;
    }

    public ContactDetailsDto getContactsDetails() {
        return contactsDetails;
    }

    public void setContactsDetails(ContactDetailsDto contactsDetails) {
        this.contactsDetails = contactsDetails;
    }

    public List<PrimaryContactDto> getPrimaryContacts() {
        return primaryContacts;
    }

    public void setPrimaryContacts(List<PrimaryContactDto> primaryContacts) {
        this.primaryContacts = primaryContacts;
    }
}
