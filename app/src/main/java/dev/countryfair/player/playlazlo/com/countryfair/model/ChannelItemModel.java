package dev.countryfair.player.playlazlo.com.countryfair.model;

import org.json.JSONObject;

/**
 * Created by nyam on 2/17/17.
 */
public class ChannelItemModel {

    private JSONObject channelData;

    public ChannelItemModel() {

    }

    public ChannelItemModel(JSONObject obj) {
        this.channelData = obj;
    }
    public JSONObject getChannelData() {
        return channelData;
    }

    public void setChannelData(JSONObject obj) {
        this.channelData = obj;
    }
}
