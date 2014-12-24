package com.highlights.service;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.highlights.EntryActivity;
import com.highlights.R;

import org.joda.time.DateTime;

/**
 * Created by austin on 12/22/14.
 */
public class EntryNotificationAlarmReceiver extends BroadcastReceiver{

    int notificationId = 001;

    @Override
    public void onReceive(Context context, Intent intent) {
        // create pending intent for setter activity
        Intent entryIntent = new Intent(context, EntryActivity.class);
        // toString is needed to put datetime into extras
        entryIntent.putExtra("entryDate", new DateTime().toString());
        entryIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent entryPendingIntent =
                PendingIntent.getActivity(context, 0, entryIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        // create notification that will go to pending setter activity
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("What'd you do today?")
                        .setContentText("Write it so you remember.")
                        .setContentIntent(entryPendingIntent)
                        .setAutoCancel(true);

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context);

        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}
