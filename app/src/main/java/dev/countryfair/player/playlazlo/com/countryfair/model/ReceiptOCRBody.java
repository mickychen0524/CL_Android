package dev.countryfair.player.playlazlo.com.countryfair.model;

import java.util.List;

public class ReceiptOCRBody {
    private String retailerId;
    private String receiptRefId;
    private String brandId;
    private String brandName;
    private String createdOn;
    private List<ReceiptOCRProduct> lineItems;
    private String ocrRaw;

    public ReceiptOCRBody() {
    }

    public String getRetailerId() {return retailerId;}

    public String getReceiptRefId() {
        return receiptRefId;
    }

    public String getBrandId() {
        return brandId;
    }

    public String getBrandName() {
        return brandName;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public List<ReceiptOCRProduct> getLineItems() {
        return lineItems;
    }

    public void setRetailerId(String retailerId) {
        this.retailerId = retailerId;
    }

    public void setReceiptRefId(String receiptRefId) {
        this.receiptRefId = receiptRefId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public void setLineItems(List<ReceiptOCRProduct> lineItems) {
        this.lineItems = lineItems;
    }

    public String getOcrRaw() {
        return ocrRaw;
    }

    public void setOcrRaw(String ocrRaw) {
        this.ocrRaw = ocrRaw;
    }
}
