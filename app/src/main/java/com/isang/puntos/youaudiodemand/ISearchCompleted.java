package com.isang.puntos.youaudiodemand;

import java.util.ArrayList;

/**
 * Created by ADMIN on 16/07/2016.
 */
public interface ISearchCompleted
{
    public void onSearchCompleted(ArrayList<HandlerSongInformation> handlerSongInformations, String keyword);
}
