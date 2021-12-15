package dk.kvalitetsit.hjemmebehandling.context;

public class UserContext {
	private String cpr;
	private String firstName;
	private String lastName;
	private String fullName;
	private String userId;
	private String[] entitlements;

    public UserContext() {}

    public String getCpr() {
        return cpr;
    }

    public void setCpr(String cpr) {
        this.cpr = cpr;
    }

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String[] getEntitlements() {
		return entitlements;
	}

	public void setEntitlements(String[] entitlements) {
		this.entitlements = entitlements;
	}
}
