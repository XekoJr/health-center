package services;

import java.io.Serializable;

public class Certification implements Serializable {
    private String nivel;
    private String grau;

    public Certification(String nivel, String grau) {
        this.nivel = nivel;
        this.grau = grau;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public String getGrau() {
        return grau;
    }

    public void setGrau(String grau) {
        this.grau = grau;
    }

    public String toString() {
        return "Nivel: " + nivel + ", Grau: " + grau;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Certification other = (Certification) obj;
        return nivel.equals(other.nivel) && grau.equals(other.grau);
    }
}
