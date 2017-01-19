package com.isang.puntos.youaudiodemand;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.view.View;
import android.widget.RemoteViews;


/**
 * Created by ADMIN on 17/07/2016.
 */
public class GUINotificationService extends Service
{
    Notification status;
    private final String LOG_TAG = "NotificationService";
    private int playBtn =  R.drawable.apollo_holo_dark_pause;

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (intent.getAction().equals(GUINotificationConstants.ACTION.STARTFOREGROUND_ACTION))
        {
            showNotification();
            //Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();

        }
        else if (intent.getAction().equals(GUINotificationConstants.ACTION.PREV_ACTION))
        {
            HandlerGlobal.gBtnPrevious.performClick();
            showNotification();
        }
        else if (intent.getAction().equals(GUINotificationConstants.ACTION.PLAY_ACTION))
        {
            RemoteViews views = new RemoteViews(getPackageName(),R.layout.status_bar);
            RemoteViews bigViews = new RemoteViews(getPackageName(),R.layout.status_bar_expanded);
            if(HandlerGlobal.mediaPlayer.isPlaying())
            {
                HandlerGlobal.mediaPlayer.pause();
                HandlerGlobal.mediaPlayerState = "pause";
                playBtn = R.drawable.apollo_holo_dark_play;
                showNotification();
            }
            else
            {
                HandlerGlobal.mediaPlayer.seekTo(HandlerGlobal.mediaPlayer.getCurrentPosition());
                HandlerGlobal.mediaPlayer.start();
                HandlerGlobal.mediaPlayerState = "play";
                playBtn = R.drawable.apollo_holo_dark_pause;
                showNotification();
            }

            status.contentView = views;
            status.bigContentView = bigViews;
            status.flags = Notification.FLAG_ONGOING_EVENT;
        }
        else if (intent.getAction().equals(GUINotificationConstants.ACTION.NEXT_ACTION))
        {
            HandlerGlobal.gBtnNext.performClick();
            showNotification();
        }
        else if (intent.getAction().equals(GUINotificationConstants.ACTION.STOPFOREGROUND_ACTION))
        {
            HandlerGlobal.mediaPlayerState = "stop";
            if (HandlerGlobal.mediaPlayer != null && HandlerGlobal.mediaPlayer.isPlaying())
            {
                HandlerGlobal.mediaPlayer.stop();
            }
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void showNotification()
    {
        RemoteViews views = new RemoteViews(getPackageName(),R.layout.status_bar);
        RemoteViews bigViews = new RemoteViews(getPackageName(),R.layout.status_bar_expanded);

        views.setViewVisibility(R.id.status_bar_icon, View.VISIBLE);
        views.setViewVisibility(R.id.status_bar_album_art, View.GONE);
        bigViews.setImageViewBitmap(R.id.status_bar_album_art, GUINotificationConstants.getDefaultAlbumArt(this));

        Intent notificationIntent = new Intent(this, PageMainActivity.class);
        notificationIntent.setAction(GUINotificationConstants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent previousIntent = new Intent(this, GUINotificationService.class);
        previousIntent.setAction(GUINotificationConstants.ACTION.PREV_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0, previousIntent, 0);

        Intent playIntent = new Intent(this, GUINotificationService.class);
        playIntent.setAction(GUINotificationConstants.ACTION.PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0, playIntent, 0);

        Intent nextIntent = new Intent(this, GUINotificationService.class);
        nextIntent.setAction(GUINotificationConstants.ACTION.NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0, nextIntent, 0);

        Intent closeIntent = new Intent(this, GUINotificationService.class);
        closeIntent.setAction(GUINotificationConstants.ACTION.STOPFOREGROUND_ACTION);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,closeIntent, 0);

        views.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);

        views.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);

        views.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent);

        views.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);

        views.setImageViewResource(R.id.status_bar_play, playBtn);
        bigViews.setImageViewResource(R.id.status_bar_play, playBtn);

        views.setTextViewText(R.id.status_bar_track_name, HandlerGlobal.songTitle);
        bigViews.setTextViewText(R.id.status_bar_track_name, HandlerGlobal.songTitle);

        views.setTextViewText(R.id.status_bar_artist_name, HandlerGlobal.currentPlaylist);
        bigViews.setTextViewText(R.id.status_bar_artist_name, HandlerGlobal.currentPlaylist);

        bigViews.setTextViewText(R.id.status_bar_album_name, HandlerGlobal.duration);

        status = new Notification.Builder(this).build();
        status.contentView = views;
        status.bigContentView = bigViews;
        status.flags = Notification.FLAG_ONGOING_EVENT;
        status.icon = R.drawable.ic_launcher;
        status.contentIntent = pendingIntent;
        startForeground(GUINotificationConstants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
    }


}

