package com.ampy.openboot;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

/** BootReceiver does not start an activity from the background: Android restricts that. */
public final class BootReceiver extends BroadcastReceiver {
    private static final String CHANNEL = "openboot_sessions";

    @Override public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(action)) {
            BootState.onBoot(context);
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            // When LOCKED_BOOT_COMPLETED was skipped on a device, consume pending here.
            if (BootState.pending(context)) BootState.onBoot(context);
            if (BootState.active(context)) notifySession(context);
        }
    }

    private void notifySession(Context context) {
        if (Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) return;
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) return;
        manager.createNotificationChannel(new NotificationChannel(
                CHANNEL, "Sessione OpenBoot", NotificationManager.IMPORTANCE_HIGH));
        Intent launch = new Intent(context, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, 0, launch,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new Notification.Builder(context, CHANNEL)
                .setSmallIcon(android.R.drawable.ic_lock_power_off)
                .setContentTitle("OpenBoot: sessione pronta")
                .setContentText("Tocca per aprire WELCOME TO MINIMAL BOOT!")
                .setContentIntent(pi)
                .setAutoCancel(true).build();
        manager.notify(101, notification);
    }
}
