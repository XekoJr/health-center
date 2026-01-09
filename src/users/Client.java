package users;

public class Client extends User {
    protected String nif;
    protected String address;
    protected String phone;

    public Client(String username, String email, String password, String name, String status, String type, boolean reviewed, String nif, String address, String phone) {
        super(username, email, password, name, status, type, reviewed);
        this.nif = nif;
        this.address = address;
        this.phone = phone;
    }

    public boolean setAddress(String aAddress) {
        this.address = aAddress;
        return true;
    }

    public boolean setPhone(String aPhone) {
        this.phone = aPhone;
        return true;
    }

    public boolean setNif(String aNif) {
        this.nif = aNif;
        return true;
    }

    public String getNif() {
        return nif;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("\n");
        sb.append("NIF: ").append(nif).append("\n");
        sb.append("Telefone: ").append(phone).append("\n");
        sb.append("Morada: ").append(address);
        return sb.toString();
    }
}
