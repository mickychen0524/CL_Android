package dev.countryfair.player.playlazlo.com.countryfair.model;

import com.google.gson.annotations.SerializedName;

public class Spatial{

	@SerializedName("altitude")
	private double altitude;

	@SerializedName("latitude")
	private double latitude;

	@SerializedName("longitude")
	private double longitude;

	public void setAltitude(double altitude){
		this.altitude = altitude;
	}

	public void setLatitude(double latitude){
		this.latitude = latitude;
	}

	public void setLongitude(double longitude){
		this.longitude = longitude;
	}
}