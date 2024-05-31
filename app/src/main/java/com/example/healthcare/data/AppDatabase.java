package com.example.healthcare.data;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {UserInfo.class, Alert.class}, version = 4)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;
    public abstract UserDao userDao();
    public abstract AlertDao alertDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "app_database")
                    .fallbackToDestructiveMigration()
                    .addMigrations(MIGRATION_2_3)
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(roomCallback)
                    .build();
        }
        return instance;
    }

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE user_info ADD COLUMN weight TEXT");
            database.execSQL("ALTER TABLE user_info ADD COLUMN height TEXT");
            database.execSQL("ALTER TABLE user_info ADD COLUMN allergyMedications TEXT");
            database.execSQL("ALTER TABLE user_info ADD COLUMN emergencyContact TEXT");
        }
    };

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE user_info ADD COLUMN steps TEXT");
            database.execSQL("ALTER TABLE user_info ADD COLUMN bodyTemperature TEXT");
        }
    };

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsyncTask(instance).execute();
        }
    };

    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void> {
        private UserDao userDao;


        private PopulateDbAsyncTask(AppDatabase db) {

            userDao = db.userDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            UserInfo admin = new UserInfo();
            admin.firstName = "Admin";
            admin.lastName = "User";
            admin.dob = "01/01/1970";
            admin.phone = "1234567890";
            admin.email = "admin@local";
            admin.password = "123456";
            admin.sex = "Other";
            admin.isAdmin = true;
            userDao.insert(admin);
            return null;
        }
    }


}
