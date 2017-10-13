package dev.countryfair.player.playlazlo.com.countryfair.model;

import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import dev.countryfair.player.playlazlo.com.countryfair.model.db.DiscoveredBeacons;

public class BeaconEvent{
	private static final DateFormat DF = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a", Locale.ENGLISH);
	@SerializedName("rssi")
	private Integer rssi;

	@SerializedName("major")
	private Integer major;

	@SerializedName("minor")
	private Integer minor;

	@SerializedName("proximity")
	private String proximity;

	@SerializedName("accuracy")
	private Double accuracy;

	@SerializedName("createdOn")
	private String createdOn;

	@SerializedName("beaconRefId")
	private String beaconRefId;

	public BeaconEvent(DiscoveredBeacons beacon) {
		rssi = beacon.getRssi();
		major = beacon.getMajor();
		minor = beacon.getMinor();
		proximity = beacon.getProximity();
		accuracy = beacon.getAccuracy();
		beaconRefId = beacon.getBeaconRefId();
		createdOn = DF.format(new Date(beacon.getDate()));
	}
}