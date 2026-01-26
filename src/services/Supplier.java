package services;

import java.io.Serializable;

public class Supplier implements Serializable, Comparable<Supplier> {
    private static final long serialVersionUID = 1L;
    private int code;
    private String name;
    private String address;
    private String phone;

    public Supplier(int code, String name, String address, String phone) {
        this.code = code;
        this.name = name;
        this.address = address;
        this.phone = phone;
    }

    public boolean setName(String aName) {
        this.name = aName;
        return true;
    }

    public boolean setAddress(String anAddress) {
        this.address = anAddress;
        return true;
    }

    public boolean setPhone(String aPhone) {
        this.phone = aPhone;
        return true;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public int compareTo(Supplier other) {
        return Integer.compare(this.code, other.code);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("-----------------------------------------\n");
        sb.append("Codigo: ").append(code).append("\n");
        sb.append("Nome: ").append(name).append("\n");
        sb.append("Morada: ").append(address).append("\n");
        sb.append("Telefone: ").append(phone);
        return sb.toString();
    }
}
