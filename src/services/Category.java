package services;

import java.io.Serializable;

public class Category implements Serializable {
    private static final long serialVersionUID = 1L;
    private int code;
    private String name;

    public Category(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public boolean setName(String aName) {
        this.name = aName;
        return true;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
