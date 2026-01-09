package services;

import java.io.Serializable;

public class ChemicalComponent implements Serializable, Comparable<ChemicalComponent> {
    private static final long serialVersionUID = 1L;
    private int code;
    private String name;
    private String alphaValue;
    private String betaValue;
    private int stockQty;

    public ChemicalComponent(int code, String name, String alphaValue, String betaValue, int stockQty) {
        this.code = code;
        this.name = name;
        this.alphaValue = alphaValue;
        this.betaValue = betaValue;
        this.stockQty = stockQty;
    }

    public boolean setStockQty(int qty) {
        this.stockQty = qty;
        return true;
    }

    public boolean changeStockQty(int increment) {
        this.stockQty += increment;
        return true;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getAlphaValue() {
        return alphaValue;
    }

    public String getBetaValue() {
        return betaValue;
    }

    public int getStockQty() {
        return stockQty;
    }

    public int compareTo(ChemicalComponent other) {
        return Integer.compare(this.code, other.code);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("-----------------------------------------\n");
        sb.append("Codigo: ").append(code).append("\n");
        sb.append("Nome: ").append(name).append("\n");
        sb.append("Alfa: ").append(alphaValue).append("\n");
        sb.append("Beta: ").append(betaValue).append("\n");
        sb.append("Stock: ").append(stockQty);
        return sb.toString();
    }
}
