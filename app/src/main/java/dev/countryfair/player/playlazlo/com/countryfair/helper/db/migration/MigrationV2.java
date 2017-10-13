package dev.countryfair.player.playlazlo.com.countryfair.helper.db.migration;

import android.database.sqlite.SQLiteDatabase;

import dev.countryfair.player.playlazlo.com.countryfair.helper.db.DatabaseUpgradeHelper;


/**
 * Migration to scheme v2

 */

public class MigrationV2 implements DatabaseUpgradeHelper.Migration {
    @Override
    public Integer getVersion() {
        return 2;
    }

    @Override
    public void runMigration(SQLiteDatabase db) {
        //noinspection unchecked
        //MigrationHelper.migrate(db, SampleDao.class, SampleRecordDao.class);
    }
}
