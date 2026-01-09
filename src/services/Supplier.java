package services;

import java.io.Serializable;

public class Supplier implements Serializable, Comparable<Supplier> {
    private static final long serialVersionUID = 1L;
    private int code;
    private String name;
    private String email;
    private String phone;

    public Supplier(int code, String name, String email, String phone) {
        this.code = code;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public boolean setName(String aName) {
        this.name = aName;
        return true;
    }

    public boolean setEmail(String anEmail) {
        this.email = anEmail;
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

    public String getEmail() {
        return email;
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
        sb.append("Email: ").append(email).append("\n");
        sb.append("Telefone: ").append(phone);
        return sb.toString();
    }
}
