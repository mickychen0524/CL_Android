package dev.countryfair.player.playlazlo.com.countryfair.model;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class Retailer{
	public static final Type LIST_TYPE = new TypeToken<ArrayList<Retailer>>() {}.getType();

	@SerializedName("addressZipPostalCode")
	private String addressZipPostalCode;

	@SerializedName("brandRefId")
	private String brandRefId;

	@SerializedName("addressStateProvince")
	private String addressStateProvince;

	@SerializedName("beacons")
	private ArrayList<BeaconsItem> beacons;

	@SerializedName("logoUrl")
	private String logoUrl;

	@SerializedName("addressCounty")
	private String addressCounty;

	@SerializedName("addressCountryCode")
	private String addressCountryCode;

	@SerializedName("retailerName")
	private String retailerName;

	@SerializedName("addressLine1")
	private String addressLine1;

	@SerializedName("addressLocation")
	private AddressLocation addressLocation;

	@SerializedName("addressLine2")
	private String addressLine2;

	@SerializedName("logoHorizontalUrl")
	private String logoHorizontalUrl;

	@SerializedName("retailerRefId")
	private String retailerRefId;

	@SerializedName("brand")
	private String brand;

	@SerializedName("logoVerticalUrl")
	private String logoVerticalUrl;

	@SerializedName("addressCity")
	private String addressCity;

	public String getAddressZipPostalCode(){
		return addressZipPostalCode;
	}

	public String getBrandRefId(){
		return brandRefId;
	}

	public String getAddressStateProvince(){
		return addressStateProvince;
	}

	public ArrayList<BeaconsItem> getBeacons(){
		return beacons;
	}

	public String getLogoUrl(){
		return logoUrl;
	}

	public String getAddressCounty(){
		return addressCounty;
	}

	public String getAddressCountryCode(){
		return addressCountryCode;
	}

	public String getRetailerName(){
		return retailerName;
	}

	public String getAddressLine1(){
		return addressLine1;
	}

	public AddressLocation getAddressLocation(){
		return addressLocation;
	}

	public String getAddressLine2(){
		return addressLine2;
	}

	public String getLogoHorizontalUrl(){
		return logoHorizontalUrl;
	}

	public String getRetailerRefId(){
		return retailerRefId;
	}

	public String getBrand(){
		return brand;
	}

	public String getLogoVerticalUrl(){
		return logoVerticalUrl;
	}

	public String getAddressCity(){
		return addressCity;
	}
}