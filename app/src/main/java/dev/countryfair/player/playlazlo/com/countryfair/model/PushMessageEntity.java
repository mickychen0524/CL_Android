package dev.countryfair.player.playlazlo.com.countryfair.model;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

public class PushMessageEntity{

	@SerializedName("templateType")
	private int templateType;

	@SerializedName("mediaSignature")
	private String mediaSignature;

	@SerializedName("mediaSize")
	private int mediaSize;

	@SerializedName("fileName")
	private String fileName;

	@SerializedName("generatedOn")
	private String generatedOn;

	@SerializedName("couponRefId")
	private String couponRefId;

	@SerializedName("claimExpireOn")
	private String claimExpireOn;

	@SerializedName("currency")
	private String currency;

	@SerializedName("EntityRefId")
	private String entityRefId;

	@SerializedName("licenseCode")
	private String licenseCode;

	@SerializedName("value")
	private double value;

	@SerializedName("sasUri")
	private String sasUri;

	public int getTemplateType(){
		return templateType;
	}

	public String getMediaSignature(){
		return mediaSignature;
	}

	public int getMediaSize(){
		return mediaSize;
	}

	public String getFileName(){
		return fileName;
	}

	public String getGeneratedOn(){
		return generatedOn;
	}

	public String getCouponRefId(){
		return couponRefId;
	}

	public String getClaimExpireOn(){
		return claimExpireOn;
	}

	public String getCurrency(){
		return currency;
	}

	public String getEntityRefId(){
		return entityRefId;
	}

	public String getLicenseCode(){
		return licenseCode;
	}

	public double getValue(){
		return value;
	}

	public String getSasUri(){
		return sasUri;
	}

	public static PushMessageEntity from(String entity) {
		if (entity==null)
			return null;
		try{
			return new Gson().fromJson(entity,PushMessageEntity.class);
		}
		catch (JsonSyntaxException e){
			e.printStackTrace();
			return null;
		}
	}
}