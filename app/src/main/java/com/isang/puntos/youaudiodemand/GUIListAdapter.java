package com.isang.puntos.youaudiodemand;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class GUIListAdapter extends BaseAdapter
{
    private ArrayList<HandlerSongInformation> mainList;
    private ArrayList<String> mListOfPlayList;
    private Context appContext;
    private int listMode = 1;
    private TextView tv1 = null;
    private TextView tv3 = null;
    private ImageView imageIcon = null;
    private ImageView imageClick = null;
    private ImageButton btnPlay;
    private int imageIconVisibility = View.VISIBLE;
    private int optionIconVisibility = View.VISIBLE;

    public GUIListAdapter(Context applicationContext, ArrayList<HandlerSongInformation> playList, ArrayList<String> listOfPlayList)
    {
        super();
        this.mainList = playList;
        this.mListOfPlayList = listOfPlayList;
        appContext = applicationContext;
    }

    @Override
    public int getCount()
    {
        return !(mainList == null)? mainList.size() : mListOfPlayList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return !(mainList == null)? mainList.get(position) : mListOfPlayList.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {

            LayoutInflater inflater = (LayoutInflater) getApplicationContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.custom_row_stack, null);
        }

        tv1 = (TextView) convertView.findViewById(R.id.row_textView1);
        tv3 = (TextView) convertView.findViewById(R.id.row_textView2);
        imageIcon = (ImageView) convertView.findViewById(R.id.row_imageView1);
        imageClick = (ImageView) convertView.findViewById(R.id.row_click_imageView1);
        imageIcon.setVisibility(imageIconVisibility);
        imageClick.setVisibility(optionIconVisibility);

        try
        {
            if(listMode == 1)
            {
                if(mListOfPlayList.get(position).contains("ß"))
                {
                    tv1.setText(mListOfPlayList.get(position).split("ß")[0]);
                    tv3.setText(mListOfPlayList.get(position).split("ß")[1] + " Audio content");
                    mListOfPlayList.set(position, mListOfPlayList.get(position).split("ß")[0]);
                    notifyDataSetChanged();
                }
                else
                {
                    tv1.setText(mListOfPlayList.get(position));
                }
            }
            else
            {
                FileInputStream fi = null;
                Bitmap bm = null;
                File filePath = null;
                try
                {

                    tv1.setText(mainList.get(position).songTitle);
                    String imName = mainList.get(position).songTitle;
                    filePath = getApplicationContext().getFileStreamPath(imName + ".png");
                    fi = new FileInputStream(filePath);
                    bm = BitmapFactory.decodeStream(fi);
                    imageIcon.setImageBitmap(bm);
                }
                catch (Exception e)
                {
                    new AsyncTaskImageDownloader(imageIcon).execute(mainList.get(position).thumbNail);
                }
                finally
                {
                    if(bm == null)
                        imageIcon.setImageBitmap(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_launcher, new BitmapFactory.Options()));
                }
                tv3.setText(mainList.get(position).duration);
            }
            imageClick.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    switch (v.getId())
                    {
                        case R.id.row_click_imageView1:

                            PopupMenu popup = new PopupMenu(getApplicationContext(), v);
                            if(listMode == 1)
                            {
                                popup.getMenuInflater().inflate(R.menu.popup_menu_playlist, popup.getMenu());
                                popup.show();
                                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                                {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item)
                                    {

                                        switch (item.getItemId())
                                        {
                                            case R.id.play_this_list:
                                                HandlerGlobal.currentIndex = 0;
                                                HandlerGlobal.songInfoGlobal = (ArrayList<HandlerSongInformation>) (new HandlerDatabase(getApplicationContext()).getPlayList(mListOfPlayList.get(position)));
                                                if (HandlerGlobal.mediaPlayer != null && HandlerGlobal.mediaPlayer.isPlaying())
                                                {
                                                    HandlerGlobal.mediaPlayer.stop();
                                                }
                                                HandlerGlobal.songTitle = HandlerGlobal.songInfoGlobal.get(0).songTitle;
                                                HandlerGlobal.duration = HandlerGlobal.songInfoGlobal.get(0).duration;
                                                HandlerGlobal.thumbNail = HandlerGlobal.songInfoGlobal.get(0).thumbNail;
                                                HandlerGlobal.mediaPlayerState = "play";

                                                HandlerGlobal.mediaPlayer = MediaPlayer.create(appContext, Uri.parse(getApplicationContext().getFilesDir().toString() + "/" + HandlerGlobal.songTitle + ".mp3"));
                                                HandlerGlobal.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                                btnPlay.performClick();
                                                break;
                                            case R.id.delete_playlist:
                                                new HandlerDatabase(appContext).deletePlaylist(mListOfPlayList.get(position));
                                                mListOfPlayList.remove(mListOfPlayList.remove(position));
                                                notifyDataSetChanged();
                                            default:
                                                break;
                                        }
                                        return true;
                                    }
                                });
                            }
                            else
                            {
                                popup.getMenuInflater().inflate(R.menu.popup_menu_search,popup.getMenu());
                                popup.show();

                                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                                {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item)
                                    {

                                        switch (item.getItemId())
                                        {
                                            case R.id.add_to_playlist:
                                                showPlaylistDialog(position);
                                                break;
                                            case R.id.play_now:
                                                AsyncTaskDownloadMusic asyncTaskDownloadMusic = new AsyncTaskDownloadMusic(getApplicationContext());
                                                asyncTaskDownloadMusic.setMode(1);
                                                asyncTaskDownloadMusic.execute("http://www.youtubeinmp3.com/download/?video=https://www.youtube.com/watch?v=" + mainList.get(position).id);
                                                break;
                                            default:
                                                break;
                                        }
                                        return true;
                                    }
                                });
                            }
                            break;
                        default:
                            break;
                    }
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return convertView;
    }

    public Context getApplicationContext()
    {
        return appContext;
    }

    public void setListMode(int mode)
    {
        listMode = mode;
    }

    public void setAppContext(Context aContext)
    {
        this.appContext = aContext;
    }

    public void setImageIconVisibility(int visibility)
    {
        imageIconVisibility = visibility;
    }

    public void setOptionIconVisibility(int visibility)
    {
        optionIconVisibility = visibility;
    }

    public void setPlayButton (ImageButton bPlay)
    {
        btnPlay = bPlay;
    }

    private void showPlaylistDialog(final int position)
    {
        try
        {
            String names[] = (new HandlerDatabase(getApplicationContext()).getListOfPlayList(false)).toArray(new String[0]);
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getApplicationContext());
            LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View convertView = (View) inflater.inflate(R.layout.listviewdialog, null);
            convertView.setBackgroundColor(ContextCompat.getColor(appContext,android.R.color.background_light));
            alertDialog.setView(convertView);
            alertDialog.setTitle("Playlist");
            ListView lv = (ListView) convertView.findViewById(R.id.listView1);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, names);
            lv.setAdapter(adapter);
            final AlertDialog ad = alertDialog.show();
            lv.setOnItemClickListener(new ListView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
                {
                    HandlerGlobal.songTitle = mainList.get(arg2).songTitle;
                    HandlerGlobal.id = mainList.get(arg2).id;
                    HandlerGlobal.thumbNail = mainList.get(arg2).thumbNail;
                    AsyncTaskDownloadMusic asyncTaskDownloadMusic = new AsyncTaskDownloadMusic(getApplicationContext());
                    asyncTaskDownloadMusic.setMode(2);
                    asyncTaskDownloadMusic.execute("http://www.youtubeinmp3.com/download/?video=https://www.youtube.com/watch?v=" + mainList.get(position).id);
                    new HandlerDatabase(appContext).addMusic((String)arg0.getItemAtPosition(arg2), mainList.get(position).songTitle, mainList.get(position).id, mainList.get(position).thumbNail, mainList.get(position).duration);
                    ad.dismiss();
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}


