package dk.kvalitetsit.hjemmebehandling.model;

public class ContactDetailsModel {

    private AddressModel address;
    private PhoneModel phone;


    public ContactDetailsModel(AddressModel address, PhoneModel phone) {
        this.address = address;
        this.phone = phone;
    }

    public ContactDetailsModel() {
    }

    public AddressModel getAddress() {
        return address;
    }

    public void setAddress(AddressModel address) {
        this.address = address;
    }

    public PhoneModel getPhone() {
        return phone;
    }

    public void setPhone(PhoneModel phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "ContactDetailsModel{" +
                "address=" + address +
                ", phone=" + phone +
                '}';
    }
}
