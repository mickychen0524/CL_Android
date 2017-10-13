package dev.countryfair.player.playlazlo.com.countryfair.model;

public class ReceiptOCRProduct {

    private String lineItemText;
    private int quantity;
    private double amount;

    public ReceiptOCRProduct(String lineItemText, int quantity, double amount) {
        this.lineItemText = lineItemText;
        this.quantity = quantity;
        this.amount = amount;
    }

    public ReceiptOCRProduct() {
    }

    public String getLineItemText() {
        return lineItemText;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getAmount() {
        return amount;
    }
}
