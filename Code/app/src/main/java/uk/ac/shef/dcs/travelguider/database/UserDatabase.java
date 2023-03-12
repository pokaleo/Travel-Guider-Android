package uk.ac.shef.dcs.travelguider.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = { VisitBean.class, PhotoBean.class }, version = 1,exportSchema = false)
public abstract class UserDatabase extends RoomDatabase {

    private static final String DB_NAME = "UserDatabase.db";
    private static volatile UserDatabase instance;

    public static synchronized UserDatabase getInstance(Context context) {
        if (instance == null) {
            instance = create(context);
        }
        return instance;
    }

    private static UserDatabase create(final Context context) {
        return Room.databaseBuilder(
                context,
                UserDatabase.class,
                DB_NAME).allowMainThreadQueries().
                build();
    }

    public abstract PhotoDAO getPhotoDAO();

    public abstract VisitDAO getVisitDAO();

}
