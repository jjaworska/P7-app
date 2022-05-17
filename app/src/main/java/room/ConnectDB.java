package room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Result.class}, version  = 1)
@TypeConverters({DateConverter.class})
public abstract class ConnectDB extends RoomDatabase {

    public abstract ResultDao resultDao();

    private static ConnectDB INSTANCE;

    public static ConnectDB getDbInstance(Context context) {

        if(INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), ConnectDB.class, "results")
                    .allowMainThreadQueries()
                    .build();

        }
        return INSTANCE;
    }

}
