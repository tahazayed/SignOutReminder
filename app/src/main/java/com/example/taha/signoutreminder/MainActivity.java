package com.example.taha.signoutreminder;


import android.Manifest;
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
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button addReminder = (Button) findViewById(R.id.addreminder);
        addReminder.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Calendar endTime = addEvent();
                if (endTime != null) {
                    Toast.makeText(getApplicationContext(), "Reminder added successfully\n Sign out time:" + endTime.getTime(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to add reminder!", Toast.LENGTH_LONG).show();
                }
            }

        });

    }

    public Calendar addEvent() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            int MY_CAL_WRITE_REQ = 0;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CALENDAR}, MY_CAL_WRITE_REQ);
        }
        Calendar beginTime = Calendar.getInstance();

        beginTime.add(Calendar.MINUTE, 480);
        Calendar endTime = Calendar.getInstance();

        endTime.add(Calendar.MINUTE, 510);
        try {

            String[] projection = new String[]{"_id", "name"};
            Uri calendars = Uri.parse("content://com.android.calendar/calendars");
            Cursor managedCursor = getContentResolver().query(calendars, projection, null, null, null);
            ContentValues event = new ContentValues();

            long StartTime = beginTime.getTimeInMillis();
            long EndTime = endTime.getTimeInMillis();

            int idColumn = managedCursor.getColumnIndex("_id");

            if (managedCursor.moveToFirst()) {


                String calId = managedCursor.getString(idColumn);

                event.put("calendar_id", calId);
                event.put("title", "Sign out Reminder");
                event.put("description", "Sign out Reminder");
                event.put("dtstart", StartTime);
                event.put("dtend", EndTime);
                event.put("hasAlarm", 1);
                event.put(CalendarContract.Events.EVENT_TIMEZONE, "Africa/Cairo");
                event.put(CalendarContract.Events.EVENT_LOCATION, "Cairo");

                Uri eventsUri = Uri.parse("content://com.android.calendar/events");
                Uri calUri = getContentResolver().insert(eventsUri, event);

                Uri remindersUri = Uri.parse("content://com.android.calendar/reminders");
                event = new ContentValues();
                event.put("event_id", Long.parseLong(calUri.getLastPathSegment()));

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
