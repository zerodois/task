package models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static java.lang.Math.ceil;

/**
 * Created by felipe on 11/09/17.
 */

public class Day implements Parcelable {
    private int number;
    private String week;
    private int month;
    private List<Task> events;
    private float all = 16;
    private int[] freeHour;
    private int year;
    private float free;
    private Setting setting;

    public static String [] months;

    static {
        DateFormatSymbols dfs = new DateFormatSymbols();
        months = dfs.getMonths();
    }

    public Day(Calendar calendar) {
        this(calendar, new ArrayList<Task>());
    }

    public Day(Calendar calendar, List<Task> events) {
        this(calendar, events, null);
    }
    public Day(Calendar calendar, List<Task> events, Setting setting) {
        this.number = calendar.get(Calendar.DAY_OF_MONTH);
        this.week = getWeek(calendar);
        this.month = calendar.get(Calendar.MONTH);
        this.year = calendar.get(Calendar.YEAR);
        this.setting = setting;
        this.setEvents(events);
    }

    protected Day(Parcel in) {
        number = in.readInt();
        week = in.readString();
        month = in.readInt();
        events = in.createTypedArrayList(Task.CREATOR);
        year = in.readInt();
        free = in.readFloat();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(number);
        dest.writeString(week);
        dest.writeInt(month);
        dest.writeTypedList(events);
        dest.writeInt(year);
        dest.writeFloat(free);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Day> CREATOR = new Creator<Day>() {
        @Override
        public Day createFromParcel(Parcel in) {
            return new Day(in);
        }

        @Override
        public Day[] newArray(int size) {
            return new Day[size];
        }
    };

    public int getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }
    public String getWeek() { return week; }
    public void setWeek(String week) { this.week = week; }
    public int getMonth() { return month; }
    public void setMonth(int mounth) { this.month = mounth; }
    public List<Task> getEvents() { return events; }

    public void setEvents(List<Task> events) {

        int lunchTime = 2;
        int lunch = 12;
        int dinner = 19;
        int wake = 6;

        if (setting != null) {
            wake = Integer.parseInt( setting.getWake()[0].split(":")[0] );
            dinner = Integer.parseInt( setting.getDinner()[0].split(":")[0] );
            lunch = Integer.parseInt( setting.getLaunch()[0].split(":")[0] );
            int sleep = Integer.parseInt( setting.getSleep()[0].split(":")[0] );
            all = (sleep == 0 ? 24 : sleep) - wake - lunchTime;
        }

        free = all;
        freeHour = new int[(int) all+lunchTime];
        for (int i = 0; i < all+lunchTime; i++)
            freeHour[i] = 0;
        freeHour[ lunch - wake ] = 1;
        freeHour[ dinner - wake ] = 1;

        this.events = events;
        for (Task t : events) {
            if (t.isAllDay())
                continue;
            int diff = (int) ceil( (t.getEnd() - t.getDate()) / 3600000 );
            if (diff < 0)
                Log.d("INFO::", "NEGATIVE FDP: " + t.getTitle() + " " + t.getEnd() + " " + t.getDate());
            Calendar c = Calendar.getInstance(TimeZone.getDefault());
            c.setTimeInMillis(t.getDate());
            int h = c.get(Calendar.HOUR_OF_DAY);
            if (h - wake < 0) continue;
            freeHour[ h - wake ] = diff;
            free -= diff;
        }
    }

    public List<int[]> getFreeVector () {

        int lunchTime = 2;
        int wake = setting != null ? Integer.parseInt( setting.getWake()[0].split(":")[0] ) : 6;

        int index = 0;
        int start = 0;
        List<int[]> ret = new ArrayList<>();

        while (index < all + lunchTime) {
            if (freeHour[index] != 0 && start == 0)
                index += freeHour[index];
            else if (freeHour[index] == 0 && start == 0)
                start = index + wake;
            else if (freeHour[index] != 0 && start != 0){
                ret.add(new int[]{ start, index+wake });
                start = 0;
                index += freeHour[index];
            } else
                index += 1;
        }

        if (start != 0)
            ret.add(new int[]{ start, 0 });

        return ret;
    }

    public String getMonth(boolean name) {
        return months[ month ];
    }

    public String getWeek(Calendar calendar) {
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        return dayFormat.format(calendar.getTime());
    }

    public String toString () {
        return getWeek().toUpperCase() + ", " + number + " de " + getMonth(true).toUpperCase().substring(0, 3);
    }

    public static List<Task> tasksByDay (List<Task> tasks, int day) {
        ArrayList<Task> arr = new ArrayList<>();
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());

        for(Task t : tasks) {
            if (t.isAllDay())
                calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
            else
                calendar.setTimeZone(TimeZone.getDefault());
            calendar.setTimeInMillis(t.getDate());
            if (calendar.get(Calendar.DAY_OF_MONTH) == day)
                arr.add(t);
        }
        return arr;
    }

    public float getFree() {
        return free;
    }

    public void setFree(float free) {
        this.free = free;
    }

    public Calendar getCalendar(int hour) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, number);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR_OF_DAY, hour);
        return c;
    }

    public static List<Day> create(Calendar init, int end, List<Task> tasks) {
        Calendar e = Calendar.getInstance();
        e.setTimeInMillis(init.getTimeInMillis());
        e.set(Calendar.DAY_OF_MONTH, end);
        return create(init, e, tasks);
    }

    private static boolean compare(Calendar date1, Calendar date2) {
        return date1.get(Calendar.DAY_OF_MONTH) == date2.get(Calendar.DAY_OF_MONTH)
        && date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH)
        && date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR);
    }

    public static List<Day> create(Calendar init, Calendar end, List<Task> tasks) {
        return create(init, end, tasks, false);
    }

    public static List<Day> create(Calendar init, Calendar end, List<Task> tasks, Boolean allDays) {
        return create(init, end, tasks, allDays, null);
    }

    public static List<Day> create(Calendar init, Calendar end, List<Task> tasks, Boolean allDays, Setting setting) {
        List<Day> arr = new ArrayList<>();
        for(; !compare(init, end); init.add(Calendar.DAY_OF_MONTH, 1)) {
            ArrayList<Task> events = new ArrayList<>(tasksByDay(tasks, init.get(Calendar.DAY_OF_MONTH)));
            if (events.size() == 0 && !allDays) continue;
            Day d = new Day(init, events, setting);
            arr.add(d);
        }
        return arr;
    }

    public String getYear() {
        return "" + year;
    }
}
