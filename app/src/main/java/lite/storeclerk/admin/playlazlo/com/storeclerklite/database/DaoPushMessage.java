package lite.storeclerk.admin.playlazlo.com.storeclerklite.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import lite.storeclerk.admin.playlazlo.com.storeclerklite.model.PushMessage;

@Dao
public interface DaoPushMessage {

    @Query("SELECT * FROM pushmessage")
    List<PushMessage> getAll();

    @Insert
    void insert(PushMessage pushMessage);

    @Insert
    void insertAll(List<PushMessage> pushMessages);
}
