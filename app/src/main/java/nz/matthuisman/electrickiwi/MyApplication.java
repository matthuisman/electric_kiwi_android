package nz.matthuisman.electrickiwi;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;

public class MyApplication extends Application {
    private ElectricKiwi electricKiwi;

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("power_hour", "Power Hour Reminder", importance);
            channel.setDescription("Notify 10 minutes before free hour of power begins");
            channel.enableLights(true);
            channel.setLightColor(Color.YELLOW);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        electricKiwi = new ElectricKiwi(getApplicationContext());

        SharedPreferences preferences = getSharedPreferences("Credentials", MODE_PRIVATE);
        String email = preferences.getString("Email", "");
        String password = preferences.getString("Password", "");

        electricKiwi.setCredentials(email, password);
    }

    public ElectricKiwi getElectricKiwi() {
        return electricKiwi;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}