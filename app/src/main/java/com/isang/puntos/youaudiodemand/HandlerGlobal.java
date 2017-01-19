package com.isang.puntos.youaudiodemand;

import android.content.Intent;
import android.media.MediaPlayer;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by ADMIN on 16/07/2016.
 */
public class HandlerGlobal
{
    public static MediaPlayer mediaPlayer;
    public static String songTitle = "";
    public static String id = "";
    public static String duration = "";
    public static String thumbNail = "";
    public static String currentPlaylist = "";
    public static String mediaPlayerState = "stop";
    public static ArrayList<HandlerSongInformation> songInfoGlobal = null;
    public static int currentIndex = 0;
    public static ImageButton gBtnNext = null;
    public static ImageButton gBtnPrevious = null;
    public static ListView gPlayListViewHandler = null;

}
