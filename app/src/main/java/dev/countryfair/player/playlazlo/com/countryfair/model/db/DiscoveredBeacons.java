package dev.countryfair.player.playlazlo.com.countryfair.model.db;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class DiscoveredBeacons  {
    @Id
    private Long id;

    private String beaconRefId;

    private Integer major;

    private Integer minor;

    private Integer rssi;

    private Double accuracy;

    private String proximity;

    private long date;

    @Generated(hash = 151102819)
    public DiscoveredBeacons(Long id, String beaconRefId, Integer major, Integer minor, Integer rssi,
            Double accuracy, String proximity, long date) {
        this.id = id;
        this.beaconRefId = beaconRefId;
        this.major = major;
        this.minor = minor;
        this.rssi = rssi;
        this.accuracy = accuracy;
        this.proximity = proximity;
        this.date = date;
    }

    public DiscoveredBeacons(String beaconRefId, Integer major,
                             Integer minor, Integer rssi, Double accuracy, String proximity,long date) {
        this.beaconRefId = beaconRefId;
        this.major = major;
        this.minor = minor;
        this.rssi = rssi;
        this.accuracy = accuracy;
        this.proximity = proximity;
        this.date = date;
    }

    @Generated(hash = 673165960)
    public DiscoveredBeacons() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBeaconRefId() {
        return this.beaconRefId;
    }

    public void setBeaconRefId(String beaconRefId) {
        this.beaconRefId = beaconRefId;
    }

    public Integer getMajor() {
        return this.major;
    }

    public void setMajor(Integer major) {
        this.major = major;
    }

    public Integer getMinor() {
        return this.minor;
    }

    public void setMinor(Integer minor) {
        this.minor = minor;
    }

    public Integer getRssi() {
        return this.rssi;
    }

    public void setRssi(Integer rssi) {
        this.rssi = rssi;
    }

    public Double getAccuracy() {
        return this.accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public String getProximity() {
        return this.proximity;
    }

    public void setProximity(String proximity) {
        this.proximity = proximity;
    }

    public long getDate() {
        return this.date;
    }

    public void setDate(long date) {
        this.date = date;
    }

}
