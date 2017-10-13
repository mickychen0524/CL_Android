package dev.countryfair.player.playlazlo.com.countryfair.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class BeaconsItem implements Parcelable{

	@SerializedName("installedOn")
	private String installedOn;

	@SerializedName("major")
	private int major;

	@SerializedName("minor")
	private int minor;

	@SerializedName("beaconId")
	private String beaconId;

	@SerializedName("placementDescription")
	private String placementDescription;

	@SerializedName("beaconRefId")
	private String beaconRefId;

	protected BeaconsItem(Parcel in) {
		installedOn = in.readString();
		major = in.readInt();
		minor = in.readInt();
		beaconId = in.readString();
		placementDescription = in.readString();
		beaconRefId = in.readString();
	}

	public static final Creator<BeaconsItem> CREATOR = new Creator<BeaconsItem>() {
		@Override
		public BeaconsItem createFromParcel(Parcel in) {
			return new BeaconsItem(in);
		}

		@Override
		public BeaconsItem[] newArray(int size) {
			return new BeaconsItem[size];
		}
	};

	public String getInstalledOn(){
		return installedOn;
	}

	public int getMajor(){
		return major;
	}

	public int getMinor(){
		return minor;
	}

	public String getBeaconId(){
		return beaconId;
	}

	public String getPlacementDescription(){
		return placementDescription;
	}

	public String getBeaconRefId(){
		return beaconRefId;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeString(installedOn);
		parcel.writeInt(major);
		parcel.writeInt(minor);
		parcel.writeString(beaconId);
		parcel.writeString(placementDescription);
		parcel.writeString(beaconRefId);
	}
}