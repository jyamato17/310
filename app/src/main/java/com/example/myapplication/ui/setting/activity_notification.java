package com.example.myapplication.ui.setting;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.myapplication.R;

public class activity_notification extends AppCompatActivity {
    public static final String Channel_1_ID = "channel1";
    private NotificationManagerCompat notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        notificationManager = NotificationManagerCompat.from(this);
        sendOnChannel1();

    }
    private void sendOnChannel1() {
        Notification notification = new NotificationCompat.Builder(this, "channel1")
                .setSmallIcon(R.drawable.ic_one)
                .setContentTitle("Map Covid Title")
                .setContentText("Map Covid Text")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();

        notificationManager.notify(1, notification);
    }




}
