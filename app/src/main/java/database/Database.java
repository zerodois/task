package database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by felipe on 10/10/17.
 */

public class Database extends SQLiteOpenHelper {

    public static final String NAME = "database.db";
    private static final int VERSAO = 6;
    private static Database instance;

    public Database(Context context) {
        super(context, NAME, null, VERSAO);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String setting = "CREATE TABLE "+ SettingSchema.TABLE+" ("
                + SettingSchema.ID + " integer primary key autoincrement,"
                + SettingSchema.DAYS + " text,"
                + SettingSchema.LAUNCH[0] + " text,"
                + SettingSchema.LAUNCH[1] + " text,"
                + SettingSchema.DINNER[0] + " text,"
                + SettingSchema.DINNER[1] + " text,"
                + SettingSchema.WAKE[0] + " text,"
                + SettingSchema.WAKE[1] + " text,"
                + SettingSchema.SLEEP[0] + " text,"
                + SettingSchema.SLEEP[1] + " text"
                +")";

        String work = "CREATE TABLE "+WorkSchema.TABLE+" ("
                + WorkSchema.ID + " integer primary key autoincrement,"
                + WorkSchema.PAYLOAD + " integer,"
                + WorkSchema.PAYLOADTYPE + " integer NULL,"
                + WorkSchema.DONE + " integer NULL,"
                + WorkSchema.REFERENCES + " integer NULL,"
                + WorkSchema.EVENT+ " integer"
                +")";

        String network = "CREATE TABLE "+NetworkSchema.TABLE+" ("
                + NetworkSchema.ID + " integer primary key autoincrement,"
                + NetworkSchema.LAYER + " integer,"
                + NetworkSchema.ROW + " integer,"
                + NetworkSchema.COLUMN + " integer,"
                + NetworkSchema.VALUE + " real,"
                + NetworkSchema.DIMENSION + " text"
                +")";

        String input = "CREATE TABLE "+InputSchema.TABLE+" ("
                + InputSchema.ID + " integer primary key autoincrement,"
                + InputSchema.DAY + " integer,"
                + InputSchema.HOUR + " integer,"
                + InputSchema.VALUE + " integer"
                +")";

        db.execSQL(setting);
        db.execSQL(work);
        db.execSQL(network);
        db.execSQL(input);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + SettingSchema.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + WorkSchema.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + NetworkSchema.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + InputSchema.TABLE);
        onCreate(db);
    }

    public static Database getInstance (Context context) {
        if (instance == null)
            instance = new Database(context);
        return instance;
    }
}
