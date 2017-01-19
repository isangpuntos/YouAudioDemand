package com.isang.puntos.youaudiodemand;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;
import java.io.FileInputStream;

public class GUINotificationConstants
{
    public interface ACTION
    {
        public static String MAIN_ACTION = "com.marothiatechs.customnotification.action.main";
        public static String INIT_ACTION = "com.marothiatechs.customnotification.action.init";
        public static String PREV_ACTION = "com.marothiatechs.customnotification.action.prev";
        public static String PLAY_ACTION = "com.marothiatechs.customnotification.action.play";
        public static String NEXT_ACTION = "com.marothiatechs.customnotification.action.next";
        public static String STARTFOREGROUND_ACTION = "com.marothiatechs.customnotification.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.marothiatechs.customnotification.action.stopforeground";

    }

    public interface NOTIFICATION_ID
    {
        public static int FOREGROUND_SERVICE = 101;
    }

    public static Bitmap getDefaultAlbumArt(Context context)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bm = null;
        try
        {
            File filePath = context.getFileStreamPath(HandlerGlobal.songTitle + ".png");
            FileInputStream fi = new FileInputStream(filePath);
            bm = BitmapFactory.decodeStream(fi);
        }
        catch (Error ee)
        {
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher, options);
        }
        catch (Exception e)
        {
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher, options);
        }
        return bm;
    }
}

