package dev.countryfair.player.playlazlo.com.countryfair.model;

import com.google.gson.annotations.SerializedName;

public class SendBeaconData{

	@SerializedName("locationInfo")
	private LocationInfo locationInfo;

	@SerializedName("brandLicenseCode")
	private String brandLicenseCode;

	public void setLocationInfo(LocationInfo locationInfo){
		this.locationInfo = locationInfo;
	}

	public void setBrandLicenseCode(String brandLicenseCode){
		this.brandLicenseCode = brandLicenseCode;
	}
}