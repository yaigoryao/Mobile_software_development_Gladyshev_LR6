package gladyshev.msdf.dz1;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class ChronometerService extends Service {

    private static final String CHANNEL_ID = "ChronometerServiceChannel";

    public ChronometerService() {}

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d("TIMER_TEST", "Service Start");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Chronometer Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
        startForegroundService();
        TimerManager.loadState(this, null);
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        Log.d("TIMER_TEST", "Service Stop");
        super.onDestroy();
        TimerManager.saveState(this);

        Intent broadcastIntent = new Intent("SERVICE_STOPPED");
        sendBroadcast(broadcastIntent);
    }

    private void startForegroundService()
    {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Хронометр")
                .setContentText("Таймеры запущены в фоновом режиме")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .build();
        startForeground(1, notification);
    }
}