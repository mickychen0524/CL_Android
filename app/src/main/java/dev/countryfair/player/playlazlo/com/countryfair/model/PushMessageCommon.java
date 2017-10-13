package dev.countryfair.player.playlazlo.com.countryfair.model;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

public class PushMessageCommon{

	@SerializedName("entityId")
	private String entityId;

	@SerializedName("operationType")
	private int operationType;

	@SerializedName("createdOn")
	private String createdOn;

	public void setEntityId(String entityId){
		this.entityId = entityId;
	}

	public String getEntityId(){
		return entityId;
	}

	public void setOperationType(int operationType){
		this.operationType = operationType;
	}

	public int getOperationType(){
		return operationType;
	}

	public void setCreatedOn(String createdOn){
		this.createdOn = createdOn;
	}

	public String getCreatedOn(){
		return createdOn;
	}

	public static PushMessageCommon from(String common) {
		if (common==null)
			return null;
		try{
			return new Gson().fromJson(common,PushMessageCommon.class);
		}
		catch (JsonSyntaxException e){
			e.printStackTrace();
			return null;
		}
	}
}