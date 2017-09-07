package com.example.taha.signoutreminder;


import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    public final static int REQUEST_CODE = 10101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button addReminder = (Button) findViewById(R.id.addreminder);
        final TextView results = (TextView) findViewById(R.id.results);
        addReminder.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Calendar endTime = addEvent();
                if (endTime != null) {
                    results.setText("Reminder added successfully\n\nSign out time:" + endTime.getTime());
                    //Toast.makeText(getApplicationContext(), "Reminder added successfully\n Sign out time:" + endTime.getTime(), Toast.LENGTH_LONG).show();
                } else {
                    results.setText("Failed to add reminder!");
                    //Toast.makeText(getApplicationContext(), "Failed to add reminder!", Toast.LENGTH_LONG).show();
                }
            }

        });

    }

    public Calendar addEvent() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CALENDAR}, REQUEST_CODE);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR}, REQUEST_CODE);
        }
        String title = "Sign out Reminder";
        Uri eventsUri = Uri.parse("content://com.android.calendar/events");
        Calendar beginTime = Calendar.getInstance();

        beginTime.add(Calendar.MINUTE, 480);
        Calendar endTime = Calendar.getInstance();

        endTime.add(Calendar.MINUTE, 510);
        try {
            //delete old events
            Calendar beginTimeFilter = Calendar.getInstance();
            beginTimeFilter.set(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, 0, 0);


            // Uri evuri = CalendarContract.Events.CONTENT_URI;
            Cursor result = getContentResolver().query(eventsUri, new String[]{CalendarContract.Events._ID, CalendarContract.Events.ACCOUNT_NAME, CalendarContract.Instances.TITLE},
                    CalendarContract.Instances.DTSTART + " >= " + beginTimeFilter.getTimeInMillis(), null, null);
            while (result.moveToNext()) {
                if (result.getString(2).equals(title)) {
                    long calid = result.getLong(0);
                    Uri deleteUri = ContentUris.withAppendedId(eventsUri, calid);
                    getContentResolver().delete(deleteUri, null, null);
                }
            }
            result.close();
            String[] projection = new String[]{"_id", "name"};
            Uri calendars = Uri.parse("content://com.android.calendar/calendars");
            Cursor managedCursor = getContentResolver().query(calendars, projection, null, null, null);
            ContentValues event = new ContentValues();

            long StartTime = beginTime.getTimeInMillis();
            long EndTime = endTime.getTimeInMillis();

            int idColumn = managedCursor.getColumnIndex("_id");

            if (managedCursor.moveToFirst()) {
                SimpleDateFormat format =
                        new SimpleDateFormat("yyyyMMdd");


                String calId = managedCursor.getString(idColumn);

                event.put("calendar_id", calId);

                event.put("title", title);
                event.put("description", title);
                event.put("dtstart", StartTime);
                event.put("dtend", EndTime);
                event.put("hasAlarm", 1);
                event.put(CalendarContract.Events.EVENT_TIMEZONE, "Africa/Cairo");
                event.put(CalendarContract.Events.EVENT_LOCATION, "Cairo");
                //event.put(CalendarContract.Events._ID, format.format(beginTime.getTime()));


                Uri calUri = getContentResolver().insert(eventsUri, event);

                Uri remindersUri = Uri.parse("content://com.android.calendar/reminders");
                event = new ContentValues();
                event.put("event_id", Long.parseLong(calUri.getLastPathSegment()));
                //event.put("event_id", Long.parseLong(format.format(beginTime.getTime())));
                event.put("method", 1);
                event.put("minutes", 0);

                getContentResolver().insert(remindersUri, event);
            }

            managedCursor.close();
            return endTime;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
