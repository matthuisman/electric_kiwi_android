package nz.matthuisman.electrickiwi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.io.IOException;

/**
 * Created by Matt on 31/05/2017.
 */

public class AlarmReceiver extends BroadcastReceiver {
    private ElectricKiwi electricKiwi;
    private Context context;

    @Override
    public void onReceive(final Context context, Intent intent) {
        this.context = context;
        electricKiwi = ((MyApplication) this.context.getApplicationContext()).getElectricKiwi();
        new GetHour().execute();
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
                Util.setAlarm(context, result.hour, true);
            }
            else {
                System.out.println(result.error);
            }
        }
    }
}