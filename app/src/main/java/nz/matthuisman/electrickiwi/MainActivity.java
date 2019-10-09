package nz.matthuisman.electrickiwi;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private View mProgressView;
    private View mPowerFormView;
    private View mLoginFormView;
    private View mScheduleFormView;
    private Spinner mHourSpinner;
    private ElectricKiwi electricKiwi;
    private ArrayAdapter<Hour> hoursAdapter;
    private ArrayAdapter<String> scheduleAdapter;

    private final String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

    private void showProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mPowerFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    protected void showSettings() {
        mProgressView.setVisibility(View.GONE);
        mPowerFormView.setVisibility(View.GONE);
        mLoginFormView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPowerFormView = findViewById(R.id.power_form);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.loading);
        mScheduleFormView = findViewById(R.id.schedule_form);

        mHourSpinner = findViewById(R.id.spinner);
        ListView mScheduleView = findViewById(R.id.schedule);

        electricKiwi = ((MyApplication) this.getApplication()).getElectricKiwi();

        EditText email_edit = findViewById(R.id.email);
        email_edit.setText(electricKiwi.getEmail());

        EditText password_edit = findViewById(R.id.password);
        password_edit.setText(electricKiwi.getPassword());

        hoursAdapter = new ArrayAdapter<> (this, android.R.layout.select_dialog_item);
        scheduleAdapter = new ArrayAdapter<> (this, android.R.layout.select_dialog_item);

        mHourSpinner.setAdapter(hoursAdapter);
        mScheduleView.setAdapter(scheduleAdapter);

        mScheduleView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int day_pos, long id) {
                new AlertDialog.Builder(view.getContext())
                        .setAdapter(hoursAdapter, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int hour_pos) {
                                addSchedule(day_pos, hoursAdapter.getItem(hour_pos).toString());
                            }
                        }).setTitle(days[day_pos]).show();
            }
        });

        mScheduleView.setLongClickable(true);
        mScheduleView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int day_pos, long id) {
                addSchedule(day_pos, "");
                return true;
            }
        });

        Util.setScheduleAlarm(this);
    }

    public void onScheduleDone(View view) {
        mScheduleFormView.setVisibility(View.GONE);
        mPowerFormView.setVisibility(View.VISIBLE);
    }

    public void addSchedule(int day, String hour) {
        JSONObject schedule;

        try {
            SharedPreferences preferences = getSharedPreferences("Credentials", MODE_PRIVATE);
            String json = preferences.getString("Schedule", "{}");

            schedule = new JSONObject(json);
            if (hour.isEmpty()) {
                schedule.remove(Integer.toString(day));
            }
            else {
                schedule.put(Integer.toString(day), hour);
            }

            preferences.edit()
                    .putString("Schedule", schedule.toString())
                    .apply();
        }
        catch (JSONException e) {
            return;
        }

        loadSchedule();
    }

    public void loadSchedule() {
        JSONObject schedule;

        try {
            SharedPreferences preferences = getSharedPreferences("Credentials", MODE_PRIVATE);
            String json = preferences.getString("Schedule", "{}");
            schedule = new JSONObject(json);
        }
        catch (JSONException e) {
            return;
        }

        scheduleAdapter.clear();
        for (int i = 0; i < days.length; i++) {
            String hour;
            try {
                hour = " - " + schedule.getString(Integer.toString(i));
            }
            catch (JSONException e) {
                hour = "";
            }

            scheduleAdapter.add(days[i] + hour);
        }
    }

    public void onSchedule(View view) {
        loadSchedule();
        mPowerFormView.setVisibility(View.GONE);
        mScheduleFormView.setVisibility(View.VISIBLE);
    }

    public void onSaveSettings(View view) {
        SharedPreferences preferences = getSharedPreferences("Credentials", MODE_PRIVATE);

        String _old_password = preferences.getString("Password", "");

        EditText email_edit = findViewById(R.id.email);
        EditText password_edit = findViewById(R.id.password);

        String email = email_edit.getText().toString();
        String password = password_edit.getText().toString();
        if (password.isEmpty()) {
            password = _old_password;
        }

        electricKiwi.logout();
        electricKiwi.setCredentials(email, password);

        preferences.edit()
                .putString("Email", email)
                .putString("Password", password)
                .apply();

        new GetHours().execute();
    }

    public void onUpdate(View view) {
        Hour hour = (Hour) mHourSpinner.getSelectedItem();
        new SetHour().execute(hour.get_id());
    }

    public void onSettings(View view) {
        showSettings();
    }

    @Override
    protected void onStart() {
        super.onStart();
        new GetHours().execute();
    }

    private class SetHour extends AsyncTask<String, Void, HoursResult> {
        @Override
        protected HoursResult doInBackground(String... params) {
            HoursResult result = new HoursResult();

            try {
                result.hours = electricKiwi.set_hour(params[0]);
                result.success = true;
            }
            catch (IOException e) {
                result.error = e.toString();
            }

            return result;
        }

        @Override
        protected void onPreExecute() {
            showProgress(true);
            hoursAdapter.clear();
        }

        @Override
        protected void onPostExecute(HoursResult result) {
            if (result.success){
                UpdateSpinner(result.hours);
            }
            else {
                showSettings();
            }
        }
    }

    private void UpdateSpinner(ArrayList<Hour> hours) {
        hoursAdapter.clear();

        for (int i = 0; i < hours.size(); i++) {
            Hour hour = hours.get(i);
            hoursAdapter.add(hour);
            if (hour.isSelected()) {
                Util.setAlarm(this, hour, false);
                mHourSpinner.setSelection(i);
            }
        }

        showProgress(false);
    }

    private class HoursResult
    {
        private boolean success = false;
        private String error = "Unknown Error";
        private ArrayList<Hour> hours;
    }

    private class GetHours extends AsyncTask<Void, Void, HoursResult> {
        @Override
        protected HoursResult doInBackground(Void... params) {
            HoursResult result = new HoursResult();

            try {
                result.hours = electricKiwi.get_hours();
                result.success = true;
            }
            catch (IOException e) {
                result.error = e.toString();
            }

            return result;
        }

        @Override
        protected void onPreExecute() {
            showProgress(true);
            hoursAdapter.clear();
        }

        @Override
        protected void onPostExecute(HoursResult result) {
            if (result.success){
                UpdateSpinner(result.hours);
            }
            else {
                showSettings();
            }
        }
    }

}
