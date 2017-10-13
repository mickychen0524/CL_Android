package dev.countryfair.player.playlazlo.com.countryfair.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import dev.countryfair.player.playlazlo.com.countryfair.model.PushMessage;

/**
 * Created by Dev01 on 9/27/2017.
 */

@Dao
public interface DaoPushMessage {

    @Query("SELECT * FROM pushmessage")
    List<PushMessage> getAll();

    @Insert
    void insert(PushMessage pushMessage);

    @Insert
    void insertAll(List<PushMessage> pushMessages);
}
