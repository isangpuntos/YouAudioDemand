package com.isang.puntos.youaudiodemand;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.lapism.searchview.SearchView;
import com.melnykov.fab.FloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.os.Handler;
import android.widget.Toast;

public class PageMainActivity extends AppCompatActivity implements ISearchCompleted, IDownloadComplete, MediaPlayer.OnCompletionListener
{
    private ImageButton btnPlay;
    private ImageButton btnForward;
    private ImageButton btnBackward;
    private ImageButton btnNext;
    private ImageButton btnPrevious;
    private ImageButton btnRepeat;
    private ImageButton btnShuffle;
    private SeekBar songProgressBar;
    private TextView songTitleLabel;
    private TextView songCurrentDurationLabel;
    private TextView songTotalDurationLabel;
    private GUIRecyclerViewAdapter gruAdapter = null;
    private ArrayList<HandlerSongInformation> songList;
    private AdView mAdView;
    private RecyclerView searchListViewHandler = null;
    private ListView playListViewHandler = null;
    FloatingActionButton btnFAB = null;
    TabHost tabHost;
    private GUIMPUtilities utils = new GUIMPUtilities();
    private int seekForwardTime = 5000;
    private int seekBackwardTime = 5000;
    private int currentSongIndex = 0;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private Handler mHandler = new Handler();
    private HandlerDatabase db;
    private InterstitialAd mInterstitialAd;
    private String currKeyword = "";
    private String currNextPageToken = "";
    //private Future longRunningTaskFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_main);

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-5615080352979741~7701578513");
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-5615080352979741/6817797715");
        db = new HandlerDatabase(getApplicationContext());

        initializeTabHost();
        initializeEvents();
        initializePlaylist();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        songTitleLabel.setText(HandlerGlobal.songTitle);
        if(HandlerGlobal.mediaPlayer != null )
        {
            if (HandlerGlobal.mediaPlayer.isPlaying())
                btnPlay.setImageResource(R.drawable.btn_pause);
            loadImageToMediaPlayer();
            updateProgressBar();
        }
    }


    @Override
    public void onCompletion(MediaPlayer mediaPlayer)
    {
        if (isRepeat)
        {
            HandlerGlobal.currentIndex = (HandlerGlobal.currentIndex + 1) % HandlerGlobal.songInfoGlobal.size();
            playSong(HandlerGlobal.currentIndex);
        }
        else if (isShuffle)
        {
            Random r = new Random();
            HandlerGlobal.currentIndex = r.nextInt(HandlerGlobal.songInfoGlobal.size() - 1);
            playSong(HandlerGlobal.currentIndex);
        }
    }

    private void initializeTabHost()
    {
        tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();

        TabHost.TabSpec spec = tabHost.newTabSpec("Media Player");
        spec.setContent(R.id.linearLayout);
        spec.setIndicator("Media Player");
        spec.setIndicator(createTabIndicator(getLayoutInflater(), tabHost, "Media Player", R.drawable.ic_action_slideshow));
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec("Search");
        spec.setContent(R.id.linearLayout2);
        spec.setIndicator("Search");
        spec.setIndicator(createTabIndicator(getLayoutInflater(), tabHost, "Search", R.drawable.ic_action_search));
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec("Playlist");
        spec.setContent(R.id.linearLayout3);
        spec.setIndicator(createTabIndicator(getLayoutInflater(), tabHost, "Playlist", R.drawable.ic_action_view_as_list));
        tabHost.addTab(spec);
    }

    private void initializeEvents()
    {
        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        btnForward = (ImageButton) findViewById(R.id.btnForward);
        btnBackward = (ImageButton) findViewById(R.id.btnBackward);
        btnNext = (ImageButton) findViewById(R.id.btnNext);
        btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
        btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
        btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);
        songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
        songTitleLabel = (TextView) findViewById(R.id.songTitle);
        songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
        songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);
        searchListViewHandler = (RecyclerView) findViewById(R.id.listView);
        playListViewHandler = (ListView) findViewById(R.id.pListView);
        btnFAB = (FloatingActionButton) findViewById(R.id.fab);

        HandlerGlobal.gBtnNext = btnNext;
        HandlerGlobal.gBtnPrevious = btnPrevious;
        HandlerGlobal.gPlayListViewHandler = playListViewHandler;
        searchListViewHandler.setLayoutManager(new LinearLayoutManager(this));
        gruAdapter = new GUIRecyclerViewAdapter(PageMainActivity.this, searchListViewHandler, songList);

        ((SearchView) findViewById(R.id.searchView1)).setOnQueryTextListener(
                new SearchView.OnQueryTextListener()
                {
                    @Override
                    public boolean onQueryTextChange(String newText)
                    {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextSubmit(String query)
                    {
                        currNextPageToken = "";
                        songList = songList != null ? null : songList;
                        loadYoutubeSearch(query);
                        return true;
                    }

                });

        gruAdapter.setOnLoadMoreListener(new GUIRecyclerViewAdapter.OnLoadMoreListener()
        {
            @Override
            public void onLoadMore()
            {
                if (!currNextPageToken.equals(""))
                {
                    gruAdapter.notifyItemInserted(songList.size() - 1);
                    loadYoutubeSearch(currKeyword);
                }
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                if (HandlerGlobal.mediaPlayer != null)
                {
                    if (HandlerGlobal.mediaPlayer.isPlaying())
                    {
                        HandlerGlobal.mediaPlayer.pause();
                        btnPlay.setImageResource(R.drawable.btn_play);
                        HandlerGlobal.mediaPlayerState = "pause";
                    }
                    else
                    {
                        HandlerGlobal.mediaPlayer.start();
                        btnPlay.setImageResource(R.drawable.btn_pause);
                        HandlerGlobal.mediaPlayerState = "play";
                    }
                    btnPlay.setImageResource(R.drawable.btn_pause);
                    songProgressBar.setProgress(0);
                    songProgressBar.setMax(100);
                    loadImageToMediaPlayer();

                    if (songTitleLabel.getText() != HandlerGlobal.songTitle)
                        displayNotification();

                    updateProgressBar();
                    songTitleLabel.setText(HandlerGlobal.songTitle);
                    //longRunningTaskFuture = Executors.newSingleThreadExecutor().submit(mUpdateTimeTask);
                }
            }
        });

        btnForward.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                int currentPosition = HandlerGlobal.mediaPlayer.getCurrentPosition();
                if (currentPosition + seekForwardTime <= HandlerGlobal.mediaPlayer.getDuration())
                {
                    HandlerGlobal.mediaPlayer.seekTo(currentPosition + seekForwardTime);
                }
                else
                {
                    HandlerGlobal.mediaPlayer.seekTo(HandlerGlobal.mediaPlayer.getDuration());
                }
            }
        });

        btnBackward.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                int currentPosition = HandlerGlobal.mediaPlayer.getCurrentPosition();
                if (currentPosition - seekBackwardTime >= 0)
                {
                    HandlerGlobal.mediaPlayer.seekTo(currentPosition - seekBackwardTime);
                }
                else
                {
                    HandlerGlobal.mediaPlayer.seekTo(0);
                }

            }
        });


        btnNext.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View arg0)
            {
                try
                {
                    HandlerGlobal.currentIndex = (HandlerGlobal.currentIndex + 1) % HandlerGlobal.songInfoGlobal.size();
                    playSong(HandlerGlobal.currentIndex);
                }
                catch(Exception ex)
                {

                }
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                try
                {
                    HandlerGlobal.currentIndex = (HandlerGlobal.songInfoGlobal.size() + (HandlerGlobal.currentIndex - 1)) % HandlerGlobal.songInfoGlobal.size();
                    playSong(HandlerGlobal.currentIndex);
                }
                catch (Exception ex)
                {

                }
            }
        });

        btnRepeat.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                if (isRepeat)
                {
                    isRepeat = false;
                    btnRepeat.setImageResource(R.drawable.btn_repeat);
                }
                else
                {
                    isRepeat = true;
                    isShuffle = false;
                    btnRepeat.setImageResource(R.drawable.btn_repeat_focused);
                    btnShuffle.setImageResource(R.drawable.btn_shuffle);
                }
            }
        });

        btnShuffle.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View arg0)
            {
                if (isShuffle)
                {
                    isShuffle = false;
                    btnShuffle.setImageResource(R.drawable.btn_shuffle);
                }
                else
                {
                    isShuffle = true;
                    isRepeat = false;
                    btnShuffle.setImageResource(R.drawable.btn_shuffle_focused);
                    btnRepeat.setImageResource(R.drawable.btn_repeat);
                }
            }
        });

        songProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b)
            {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
                mHandler.removeCallbacks(mUpdateTimeTask);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                mHandler.removeCallbacks(mUpdateTimeTask);
                int totalDuration = HandlerGlobal.mediaPlayer.getDuration();
                int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);
                HandlerGlobal.mediaPlayer.seekTo(currentPosition);
                updateProgressBar();
            }
        });

        playListViewHandler.setOnItemClickListener(
                new ListView.OnItemClickListener()
                {
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
                    {
                        try
                        {
                            String audioList = (String) arg0.getItemAtPosition(arg2);
                            showPlaylistContentDialog(audioList);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });

        btnFAB.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(PageMainActivity.this, R.style.myDialog));
                alert.setTitle("New Playlist");
                alert.setMessage("Please enter name of new playlist");
                final EditText input = new EditText(PageMainActivity.this);
                alert.setView(input);
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        String inputName = input.getText().toString();

                        SharedPreferences.Editor e = getSharedPreferences("prefs", MODE_PRIVATE).edit();
                        e.putString("Playlist", inputName);
                        e.commit();
                        db.addPlayList(inputName);
                        initializePlaylist();
                        Toast.makeText(getApplicationContext(), inputName + " was added to the list", Toast.LENGTH_LONG).show();

                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {

                    }
                });
                alert.show();
            }
        });

        mInterstitialAd.setAdListener(new AdListener()
        {
            @Override
            public void onAdLoaded()
            {
                mInterstitialAd.show();
            }

            @Override
            public void onAdClosed()
            {
                //requestNewInterstitial();
            }
        });

    }

    private void initializePlaylist()
    {
        List<String> playList = db.getListOfPlayList(true);
        GUIListAdapter mAdapter = new GUIListAdapter(PageMainActivity.this, null, (ArrayList) playList);
        mAdapter.setAppContext(getApplicationContext());
        mAdapter.setListMode(1);
        mAdapter.setPlayButton(btnPlay);
        mAdapter.setImageIconVisibility(View.GONE);
        playListViewHandler.setAdapter(mAdapter);
    }

    private View createTabIndicator(LayoutInflater inflater, TabHost tabHost, String tabTitle, int iconResource)
    {
        View tabIndicator = inflater.inflate(R.layout.tab_indicator, tabHost.getTabWidget(), false);
        ((TextView) tabIndicator.findViewById(android.R.id.title)).setText(tabTitle);
        ((ImageView) tabIndicator.findViewById(android.R.id.icon)).setImageResource(iconResource);
        return tabIndicator;
    }

    private Runnable mUpdateTimeTask = new Runnable()
    {
        public void run()
        {
            try
            {
                long totalDuration = (HandlerGlobal.mediaPlayerState.equals("play") || HandlerGlobal.mediaPlayerState.equals("pause")) ? HandlerGlobal.mediaPlayer.getDuration() : 0;
                long currentDuration = (HandlerGlobal.mediaPlayerState.equals("play") || HandlerGlobal.mediaPlayerState.equals("pause")) ? HandlerGlobal.mediaPlayer.getCurrentPosition() : 0;

                if (HandlerGlobal.mediaPlayerState.equals("stop") || HandlerGlobal.mediaPlayerState.equals("pause"))
                    btnPlay.setImageResource(R.drawable.btn_play);

                songTotalDurationLabel.setText("" + utils.milliSecondsToTimer(totalDuration));
                songCurrentDurationLabel.setText("" + utils.milliSecondsToTimer(currentDuration));

                int progress = (int) (utils.getProgressPercentage(currentDuration, totalDuration));
                songProgressBar.setProgress(progress);

                mHandler.postDelayed(this, 100);
            }
            catch (Exception e)
            {

            }
        }
    };

    private void loadYoutubeSearch(String keyword)
    {
        try
        {
            if (currNextPageToken.equals(""))
                requestNewInterstitial();

            AsyncTaskYoutubeSearch asyncTaskYoutubeSearch = new AsyncTaskYoutubeSearch(PageMainActivity.this);
            asyncTaskYoutubeSearch.setKeyword(keyword);
            asyncTaskYoutubeSearch.setNextPageToken(currNextPageToken);
            asyncTaskYoutubeSearch.execute();
            currKeyword = keyword;
        }
        catch (Exception e)
        {
            String ee = e.toString();
            e.printStackTrace();
        }
    }

    private void loadImageToMediaPlayer()
    {
        File filePath = getApplicationContext().getFileStreamPath(HandlerGlobal.songTitle + ".png");
        FileInputStream fi = null;
        Bitmap bm = null;
        try
        {
            fi = new FileInputStream(filePath);
            bm = BitmapFactory.decodeStream(fi);
        }
        catch (FileNotFoundException e)
        {
            bm = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_launcher, new BitmapFactory.Options());
            e.printStackTrace();
        }
        ((ImageView) (findViewById(R.id.dispThumbnail))).setImageBitmap(Bitmap.createScaledBitmap(bm, 300, 300, false));
    }

    private void playSong(int songIndex)
    {
        try
        {
            HandlerGlobal.currentIndex = songIndex;
            if (HandlerGlobal.mediaPlayer != null && HandlerGlobal.mediaPlayer.isPlaying())
            {
                HandlerGlobal.mediaPlayer.stop();
                //longRunningTaskFuture.cancel(true);
            }
            HandlerGlobal.songTitle = HandlerGlobal.songInfoGlobal.get(songIndex).songTitle;
            HandlerGlobal.duration = HandlerGlobal.songInfoGlobal.get(songIndex).duration;
            HandlerGlobal.thumbNail = HandlerGlobal.songInfoGlobal.get(songIndex).thumbNail;
            HandlerGlobal.id = HandlerGlobal.songInfoGlobal.get(songIndex).id;
            HandlerGlobal.mediaPlayerState = "play";
            HandlerGlobal.mediaPlayer = MediaPlayer.create(PageMainActivity.this, Uri.parse(getApplicationContext().getFilesDir().toString() + "/" + HandlerGlobal.songTitle + ".mp3"));
            HandlerGlobal.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            HandlerGlobal.mediaPlayer.setOnCompletionListener(this);
            btnPlay.performClick();
        }
        catch (Exception e)
        {
            AsyncTaskDownloadMusic asyncTaskDownloadMusic = new AsyncTaskDownloadMusic(this);
            asyncTaskDownloadMusic.execute("http://www.youtubeinmp3.com/download/?video=https://www.youtube.com/watch?v=" + HandlerGlobal.id + "," +
                    HandlerGlobal.songTitle + "," +
                    HandlerGlobal.duration + "," +
                    HandlerGlobal.thumbNail + "," +
                    HandlerGlobal.id);
        }
    }

    public void updateProgressBar()
    {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    private void showPlaylistContentDialog(final String playlist)
    {
        try
        {
            final List<HandlerSongInformation> pList = new HandlerDatabase(getApplicationContext()).getPlayList(playlist);
            if (pList.size() > 0)
            {
                final android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(this);
                LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View convertView = (View) inflater.inflate(R.layout.listviewdialog, null);
                convertView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.background_light));
                ListView lv = (ListView) convertView.findViewById(R.id.listView1);
                GUIListAdapter mAdapter = new GUIListAdapter(this, (ArrayList) pList, null);
                mAdapter.setAppContext(getApplicationContext());
                mAdapter.setListMode(2);
                mAdapter.setOptionIconVisibility(View.GONE);
                lv.setAdapter(mAdapter);
                alertDialog.setView(convertView);
                alertDialog.setTitle("Select Audio");
                final android.app.AlertDialog aDialog = alertDialog.show();
                lv.setOnItemClickListener(new ListView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
                    {
                        HandlerGlobal.currentPlaylist = playlist;
                        HandlerGlobal.songInfoGlobal = (ArrayList<HandlerSongInformation>) pList;
                        playSong(arg2);
                        requestNewInterstitial();
                        aDialog.dismiss();
                    }
                });
            }
            else
            {
                Toast.makeText(getApplicationContext(), "List is empty", Toast.LENGTH_LONG).show();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void displayNotification()
    {
        Intent serviceIntent = new Intent(PageMainActivity.this, GUINotificationService.class);
        serviceIntent.setAction(GUINotificationConstants.ACTION.STARTFOREGROUND_ACTION);
        startService(serviceIntent);
    }

    private void requestNewInterstitial()
    {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
    }

    @Override
    public void onSearchCompleted(ArrayList<HandlerSongInformation> handlerSongInformations, String nextPageToken)
    {
        //((TextView) findViewById(R.id.textView)).setText("Search Results: " + Integer.toString(handlerSongInformations.size()) + " Found!");
        if (currNextPageToken.equals(""))
        {
            songList = handlerSongInformations;
            gruAdapter = null;
            gruAdapter = new GUIRecyclerViewAdapter(this, searchListViewHandler, songList);
            searchListViewHandler.setAdapter(gruAdapter);
        }
        else
        {
            songList.addAll(handlerSongInformations);
            gruAdapter.notifyDataSetChanged();
        }
        gruAdapter.setLoaded();
        gruAdapter.setNextPageToken(nextPageToken);
        currNextPageToken = nextPageToken;
    }

    @Override
    public void onDownloadCompleted(int mode)
    {
        initializePlaylist();
        if (mode == 1)
        {
            if (new File(getApplicationContext().getFilesDir().toString() + "/" + HandlerGlobal.songTitle + ".mp3").exists())
            {
                if (HandlerGlobal.mediaPlayer != null && HandlerGlobal.mediaPlayer.isPlaying())
                {
                    HandlerGlobal.mediaPlayer.stop();
                }
                HandlerGlobal.currentPlaylist = "";
                HandlerGlobal.mediaPlayer = MediaPlayer.create(this, Uri.parse(getApplicationContext().getFilesDir().toString() + "/" + HandlerGlobal.songTitle + ".mp3"));
                btnPlay.performClick();
            }
        }
    }

}
