package nz.matthuisman.electrickiwi;

import android.app.AlarmManager;
import android.app.Notification;
import android.graphics.Color;
import androidx.core.app.NotificationCompat;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.NOTIFICATION_SERVICE;

public class Util {
    static public void setScheduleAlarm(Context context) {
        Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("Pacific/Auckland"));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 15);
        calendar.set(Calendar.SECOND, 0);

        Calendar current = GregorianCalendar.getInstance(TimeZone.getTimeZone("Pacific/Auckland"));
        current.add(Calendar.SECOND, 10);

        if(calendar.before(current)) {
            calendar.add(Calendar.DATE, 1);
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(context, ScheduleReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    static public void setAlarm(Context context, Hour hour, Boolean notify) {
        DateFormat sdf = new SimpleDateFormat("hh:mm a");
        Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("Pacific/Auckland"));
        Date date;

        Calendar current = GregorianCalendar.getInstance(TimeZone.getTimeZone("Pacific/Auckland"));
        current.add(Calendar.SECOND, 10);

        try {
            date = sdf.parse(hour.toString());
        }
        catch (java.text.ParseException e) {
            return;
        }

        calendar.set(Calendar.HOUR_OF_DAY, date.getHours());
        calendar.set(Calendar.MINUTE, date.getMinutes());
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.MINUTE, -10);

        // Above minus 10mins on midnight, puts to yesterday.
        // So fix with below
        if (calendar.get(Calendar.DAY_OF_MONTH) != current.get(Calendar.DAY_OF_MONTH)) {
            calendar.add(Calendar.DATE, 1);
        }

        if(calendar.before(current)) {
            if (notify) {
                Intent notificationIntent = new Intent(context, MainActivity.class);
                PendingIntent contentIntent = PendingIntent.getActivity(context,
                        0, notificationIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT);

                Notification n = new NotificationCompat.Builder(context, "power_hour")
                        .setContentTitle(String.format(context.getString(R.string.notification), hour))
                        .setContentText(context.getString(R.string.notification_2))
                        .setContentIntent(contentIntent)
                        .setDefaults(NotificationCompat.DEFAULT_SOUND)
                        .setColor(Color.parseColor("#ff7800"))
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .build();

                n.flags |= Notification.FLAG_AUTO_CANCEL;

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(0, n);
            }
            calendar.add(Calendar.DATE, 1);
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }
}
