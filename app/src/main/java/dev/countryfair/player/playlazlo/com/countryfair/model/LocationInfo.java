package dev.countryfair.player.playlazlo.com.countryfair.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LocationInfo{

	@SerializedName("playerLicenseCode")
	private String playerLicenseCode;

	@SerializedName("spatial")
	private Spatial spatial;

	@SerializedName("beaconEvents")
	private List<BeaconEvent> beaconEvents;

	@SerializedName("retailerRefId")
	private String retailerRefId;

	public void setPlayerLicenseCode(String playerLicenseCode){
		this.playerLicenseCode = playerLicenseCode;
	}

	public void setSpatial(Spatial spatial){
		this.spatial = spatial;
	}

	public void setBeaconEvents(List<BeaconEvent> beaconEvents){
		this.beaconEvents = beaconEvents;
	}

	public void setRetailerRefId(String retailerRefId) {
		this.retailerRefId = retailerRefId;
	}
}