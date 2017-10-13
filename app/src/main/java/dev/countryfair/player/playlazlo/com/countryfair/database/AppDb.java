package dev.countryfair.player.playlazlo.com.countryfair.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import dev.countryfair.player.playlazlo.com.countryfair.model.PushMessage;

/**
 * Created by Dev01 on 9/27/2017.
 */


@Database(entities = {PushMessage.class}, version = 1)
public abstract class AppDb extends RoomDatabase{
    private static AppDb INSTANCE;

    public abstract DaoPushMessage daoPushMessage();

    public static AppDb getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), AppDb.class, "cf-database")
                            .allowMainThreadQueries()
                            .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

}
