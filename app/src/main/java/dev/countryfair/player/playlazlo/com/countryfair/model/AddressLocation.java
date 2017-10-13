package dev.countryfair.player.playlazlo.com.countryfair.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AddressLocation{

	@SerializedName("coordinates")
	private List<Double> coordinates;

	@SerializedName("type")
	private String type;

	public List<Double> getCoordinates(){
		return coordinates;
	}

	public String getType(){
		return type;
	}
}