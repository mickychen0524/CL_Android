package dev.countryfair.player.playlazlo.com.countryfair.model;

public class Receipt {

    private double latitude;
    private double longitude;
    private String uuid;
    private String createdOn;
    private String correlationRefId;
    private ReceiptData data;

    public Receipt() {
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getUuid() {
        return uuid;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public String getCorrelationRefId() {
        return correlationRefId;
    }

    public ReceiptData getData() {
        return data;
    }
}
