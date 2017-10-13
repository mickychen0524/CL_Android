package dev.countryfair.player.playlazlo.com.countryfair.model;

import mobi.windfall.receipt.ScanResults;

public class ReceiptRequestBody {

    private ReceiptOCRBody data;
    private String correlationRefId;
    private String latitude;
    private String longitude;

    public ReceiptRequestBody(ReceiptOCRBody data, String correlationRefId, String latitude, String longitude) {

        this.data = data;
        this.correlationRefId = correlationRefId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public ReceiptRequestBody() {
    }

    public ReceiptOCRBody getData() {
        return data;
    }

    public String getCorrelationRefId() {
        return correlationRefId;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }


    public void setData(ReceiptOCRBody data) {
        this.data = data;
    }

    public void setCorrelationRefId(String correlationRefId) {
        this.correlationRefId = correlationRefId;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
