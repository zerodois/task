package com.example.felipe.app;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import components.Waiting;
import models.CalendarProvider;
import models.Graph;
import models.Setting;
import models.Task;
import models.Work;

public class CreateEvent extends AppCompatActivity implements View.OnClickListener {

    private Button date_input;
    private Button event_hour;
    private EditText estimate_picker;
    private EditText name_input;
    private Calendar calendar;
    private Spinner period_spinner;
    private Setting setting;
    private Waiting w;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String format(Calendar calendar) {
        Date t = calendar.getTime();
        String f = String.format(Locale.getDefault(), "%ta, %td de %tB de %tY", t, t, t, t);
        String[] w = f.split("\\s+");
        f = w[0].toUpperCase().charAt(0) + w[0].substring(1, w[0].length());
        for (int i = 1; i < w.length; i++)
            f += " " + (w[i].equals("de") ? w[i] : w[i].toUpperCase().charAt(0) + w[i].substring(1, w[i].length()));
        return f;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String formatHour(Calendar calendar) {
        //return calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
        return String.format(Locale.getDefault(), "%tH:%tM", calendar.getTime(), calendar.getTime());
    }

    public DatePickerDialog.OnDateSetListener getListener(final View v) {
        return new DatePickerDialog.OnDateSetListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            private void updateLabel(Button b) {
                b.setText(format(calendar));
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel((Button) v);
            }
        };
    }

    public TimePickerDialog.OnTimeSetListener getListenerHour(final View view) {
        return new TimePickerDialog.OnTimeSetListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), i, i1);
                ((Button) view).setText(formatHour(calendar));
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        Utils.setSystemBarLight(this, ContextCompat.getColor(this, R.color.colorPrimaryDarker));
        calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
        setting = Setting.init(this).get(1);


        //pager = (ViewPager) findViewById(R.id.diff);
        //pager.setAdapter(new DifficultyAdapter(this));
        name_input = (EditText) findViewById(R.id.event_name);
        date_input = (Button) findViewById(R.id.event_date);
        event_hour = (Button) findViewById(R.id.event_hour);
        event_hour.setText(formatHour(calendar));
        event_hour.setOnClickListener(listenerHour());
        estimate_picker = (EditText) findViewById(R.id.estimate_picker);
        //estimate_picker.setTypeface(null, Typeface.NORMAL);
        date_input.setTypeface(null, Typeface.NORMAL);
        event_hour.setTypeface(null, Typeface.NORMAL);
        date_input.setText(format(calendar));
        date_input.setOnClickListener(this);
        period_spinner = (Spinner) findViewById(R.id.period_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.period, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        period_spinner.setAdapter(adapter);
        period_spinner.setSelection(0);
    }

    public void close(View v) {
        this.finish();
    }

    public View.OnClickListener listenerHour() {
        final CreateEvent act = this;
        return new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                TimePickerDialog picker = new TimePickerDialog(act, getListenerHour(view), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                picker.show();
            }
        };
    }

    /*  */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void createEvent(View view) throws InterruptedException {
        w = new Waiting(this, "Configurando seu tempo");

        List<CalendarProvider> calendars = CalendarProvider.calendars(this);
        if (calendars.size() == 0)
            return;

        Graph graph = new Graph(Calendar.getInstance(), calendar, setting, this);
        Task task = new Task(name_input.getText().toString(), calendar.getTimeInMillis(), calendar.getTimeInMillis() + 3600000);
        int estimative = Integer.parseInt(estimate_picker.getText().toString());
        List<Task> l = graph.organize(task, estimative);
        long _id = task.record(this, calendars.get(1).getId());
        if (_id == -1) {
            Log.d("INFO::", "Erro ao salvar no calendar");
            return;
        }
        Log.d("INFO::", "GRRRR " + _id);
        Work work = new Work(this);
        work.setPayload(estimative);
        work.setTask(task);
        work.setReference(null);
        if (!work.save()) {
            Log.d("INFO::", "Erro ao salvar do DB :((");
            return;
        }


        work.setReference(task);
        for (Task t : l) {
            _id = t.record(this,calendars.get(0).getId());
            if (_id == -1) {
                Log.d("INFO::", "Erro ao salvar o fragmento no calendar :((");
                return;
            }
            work.setTask(t);
            if (!work.save()) {
                Log.d("INFO::", "Erro ao salvar o fragmento no DB :((");
                return;
            }
            Log.d("INFO::", "\"Salvo\": " + t.getTitle());
        }

        TabActivity.prototype.reload();
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View view) {
        DatePickerDialog picker = new DatePickerDialog(this, getListener(view), calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        picker.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        w.close();
    }
}
