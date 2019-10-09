package nz.matthuisman.electrickiwi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Matt on 31/05/2017.
 */

public class ScheduleReceiver extends BroadcastReceiver {
    private ElectricKiwi electricKiwi;
    private Context context;

    @Override
    public void onReceive(final Context context, Intent intent) {
        electricKiwi = ((MyApplication) context.getApplicationContext()).getElectricKiwi();
        this.context = context;

        JSONObject schedule;
        String hour;
        try {
            SharedPreferences preferences = context.getSharedPreferences("Credentials", MODE_PRIVATE);
            String json = preferences.getString("Schedule", "{}");
            schedule = new JSONObject(json);
            Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("Pacific/Auckland"));
            hour = schedule.getString(Integer.toString(calendar.get(Calendar.DAY_OF_WEEK) - 1));
            new SetHour().execute(hour);
        }
        catch (JSONException e) {
        }

        Util.setScheduleAlarm(context);
    }

    public class HourResult
    {
        public boolean success = false;
        public String error = "Unknown Error";
        public Hour hour;
    }

    private class SetHour extends AsyncTask<String, Void, HourResult> {
        @Override
        protected HourResult doInBackground(String... params) {
            HourResult result = new HourResult();

            try {
                ArrayList<Hour> hours = electricKiwi.get_hours();
                for (int i = 0; i < hours.size(); i++) {
                    Hour hour = hours.get(i);
                    if (hour.toString().equals(params[0])) {
                        if (!hour.isSelected()) {
                            electricKiwi.set_hour(hour.get_id());
                        }

                        result.hour = hour;
                        result.success = true;
                        return result;
                    }
                }

            }
            catch (java.io.IOException e) {
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

            }
        }
    }
}