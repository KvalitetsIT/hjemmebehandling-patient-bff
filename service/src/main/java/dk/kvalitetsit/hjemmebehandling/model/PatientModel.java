package dk.kvalitetsit.hjemmebehandling.model;


import java.util.List;

public class PatientModel {
    private QualifiedId id;
    private String givenName;
    private String familyName;
    private String cpr;
    private ContactDetailsModel contactDetails;

    private List<PrimaryContactModel> contacts;
    
    public QualifiedId getId() {
        return id;
    }

    public void setId(QualifiedId id) {
        this.id = id;
    }

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

    public ContactDetailsModel getContactDetails() {
        return contactDetails;
    }

    public void setContactDetails(ContactDetailsModel contactDetails) {
        this.contactDetails = contactDetails;
    }

    public List<PrimaryContactModel> getContacts() {
        return contacts;
    }

    public void setContacts(List<PrimaryContactModel> contacts) {
        this.contacts = contacts;
    }
}
