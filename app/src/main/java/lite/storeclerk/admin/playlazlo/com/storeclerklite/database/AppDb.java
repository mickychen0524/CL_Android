package lite.storeclerk.admin.playlazlo.com.storeclerklite.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import lite.storeclerk.admin.playlazlo.com.storeclerklite.model.PushMessage;

@Database(entities = {PushMessage.class}, version = 1)
public abstract class AppDb extends RoomDatabase{
    private static AppDb INSTANCE;

    public abstract DaoPushMessage daoPushMessage();

    public static AppDb getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), AppDb.class, "cl-database")
                            .allowMainThreadQueries()
                            .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

}
