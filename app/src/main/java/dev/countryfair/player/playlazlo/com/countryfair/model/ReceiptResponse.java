package dev.countryfair.player.playlazlo.com.countryfair.model;

public class ReceiptResponse {

    private String correlationRefId;
    private String createdOn;
    private String error;
    private ReceiptData data;

    public ReceiptResponse() {
    }

    public String getCorrelationRefId() {
        return correlationRefId;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public String getError() {
        return error;
    }

    public ReceiptData getData() {
        return data;
    }
}
