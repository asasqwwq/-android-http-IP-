package com.xulei.g4nproxy;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import com.xulei.g4nproxy_client.ProxyClient;
import com.xulei.g4nproxy_protocol.protocol.Constants;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

/**
 * Created by virjar on 2019/2/22.
 */

public class HttpProxyService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        startService();
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startService();
        return START_STICKY;
    }

    private AtomicBoolean serviceStarted = new AtomicBoolean(false);

    private void startService() {
        if (serviceStarted.compareAndSet(false, true)) {
            Thread thread = new Thread("g4ProxyThread") {
                @Override
                public void run() {
                    try {
                        startServiceInternal();
                    } catch (InterruptedException e) {
                        Log.e("HttpProxyService",e.getMessage());
                    }
                }
            };
            thread.setDaemon(true);
            thread.start();

        }
    }

    private void setNotifyChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel notificationChannel = new NotificationChannel(BuildConfig.APPLICATION_ID,
                "channel", NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.YELLOW);
        notificationChannel.setShowBadge(true);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }
        manager.createNotificationChannel(notificationChannel);
    }

    private void startServiceInternal() throws InterruptedException {

        setNotifyChannel();

        Notification.Builder builder = new Notification.Builder(this.getApplicationContext()); //????????????Notification?????????
        // ??????PendingIntent
        Intent nfIntent = new Intent(this, MainActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, FLAG_UPDATE_CURRENT))
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher)) // ??????????????????????????????(?????????)
                .setContentTitle("G4Proxy") // ??????????????????????????????
                .setSmallIcon(R.mipmap.ic_launcher) // ??????????????????????????????
                .setContentText("????????????agent") // ?????????????????????
                .setWhen(System.currentTimeMillis()); // ??????????????????????????????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(BuildConfig.APPLICATION_ID);
        }

        Notification notification = builder.build(); // ??????????????????Notification
        notification.defaults = Notification.DEFAULT_SOUND; //????????????????????????
        startForeground(110, notification);// ??????????????????

        ALOG.setUpLogComponent(new ALOG.LogImpl() {
            @Override
            public void i(String tag, String msg) {
                Log.i(tag, msg);
            }

            @Override
            public void w(String tag, String msg) {
                Log.w(tag, msg);
            }

            @Override
            public void w(String tag, String msg, Throwable throwable) {
                Log.w(tag, msg, throwable);
            }

            @Override
            public void e(String tag, String msg) {
                Log.e(tag, msg);
            }

            @Override
            public void e(String tag, String msg, Throwable throwable) {
                Log.e(tag, msg, throwable);
            }
        });

//        Log.i("weijia", "start G4Proxy front service");
//        Launcher.startHttpProxyService(3128);  // ?????????????????????

        Log.i("weijia", "start private network forward task");
        String clientID = Settings.System.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
//        ProxyClient.start(Constants.g4ProxyServerHost_1, Constants.g4ProxyServerPort, clientID);
        ProxyClient.start(Constants.g4nproxyServerHost,Constants.g4nproxyServerPort,clientID);

//        ProxyClient.start(Constants.g4ProxyServerHost_2, Constants.g4ProxyServerPort, clientID);
    }

}
