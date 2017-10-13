package dev.countryfair.player.playlazlo.com.countryfair.model;

/**
 * Created by Android Developer on 16/08/17.
 */

public class Loyalty {

    private String skuName;
    private float lifetimeCount;
    private float pendingCount;

    public Loyalty() {
    }

    public Loyalty(String skuName, float lifetimeCount, float pendingCount) {
        this.skuName = skuName;
        this.lifetimeCount = lifetimeCount;
        this.pendingCount = pendingCount;
    }

    public String getSkuName() {
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }

    public float getLifetimeCount() {
        return lifetimeCount;
    }

    public void setLifetimeCount(float lifetimeCount) {
        this.lifetimeCount = lifetimeCount;
    }

    public float getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(float pendingCount) {
        this.pendingCount = pendingCount;
    }

    @Override
    public String toString() {
        return "Loyalty{" +
                "skuName='" + skuName + '\'' +
                ", lifetimeCount=" + lifetimeCount +
                ", pendingCount=" + pendingCount +
                '}';
    }
}
