package nz.matthuisman.electrickiwi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by Matt on 31/05/2017.
 */

public class BootReceiver extends BroadcastReceiver {
    private ElectricKiwi electricKiwi;
    private Context context;

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            this.context = context;
            electricKiwi = ((MyApplication) this.context.getApplicationContext()).getElectricKiwi();
            new GetHour().execute();
        }
    }

    public class HourResult
    {
        public boolean success = false;
        public String error = "Unknown Error";
        public Hour hour;
    }

    private class GetHour extends AsyncTask<Void, Void, HourResult> {
        @Override
        protected HourResult doInBackground(Void... params) {
            HourResult result = new HourResult();

            try {
                result.hour = electricKiwi.get_hour();
                result.success = true;
            }
            catch (IOException e) {
                result.error = e.toString();
            }

            return result;
        }

        @Override
        protected void onPostExecute(HourResult result) {
            if (result.success){
                Util.setAlarm(context, result.hour, false);
            }
            else {
                System.out.println(result.error);
            }
        }
    }
}