package dev.countryfair.player.playlazlo.com.countryfair.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by Dev01 on 9/27/2017.
 */

@Entity(tableName = "pushmessage")
public class PushMessage {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "data")
    private String data;


    public PushMessage(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}