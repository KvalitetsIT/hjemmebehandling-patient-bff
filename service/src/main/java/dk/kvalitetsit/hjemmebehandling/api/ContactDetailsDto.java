package dk.kvalitetsit.hjemmebehandling.api;

public class ContactDetailsDto {

    private AddressDto address;
    private PhoneDto phone;


    public ContactDetailsDto(AddressDto addressDto, PhoneDto phone) {
        this.address = addressDto;
        this.phone = phone;
    }

    public ContactDetailsDto() {
    }

    public AddressDto getAddress() {
        return address;
    }

    public void setAddress(AddressDto addressDto) {
        this.address = addressDto;
    }

    public PhoneDto getPhone() {
        return phone;
    }

    public void setPhone(PhoneDto phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "ContactDetailsDto{" +
                "addressDto=" + address +
                ", phone=" + phone +
                '}';
    }
}
