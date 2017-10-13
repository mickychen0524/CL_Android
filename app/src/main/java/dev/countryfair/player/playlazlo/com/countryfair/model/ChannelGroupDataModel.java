package dev.countryfair.player.playlazlo.com.countryfair.model;

import java.util.ArrayList;

/**
 * Created by nyam on 2/17/17.
 */
public class ChannelGroupDataModel {

    private String headerTitle;

    private ArrayList<ChannelItemModel> allItemsInSection;


    public ChannelGroupDataModel() {

    }
    public ChannelGroupDataModel(String headerTitle, ArrayList<ChannelItemModel> allItemsInSection) {
        this.headerTitle = headerTitle;
        this.allItemsInSection = allItemsInSection;
    }

    public ArrayList<ChannelItemModel> getAllItemsInSection() {
        return allItemsInSection;
    }

    public void setAllItemsInSection(ArrayList<ChannelItemModel> allItemsInSection) {
        this.allItemsInSection = allItemsInSection;
    }

}
