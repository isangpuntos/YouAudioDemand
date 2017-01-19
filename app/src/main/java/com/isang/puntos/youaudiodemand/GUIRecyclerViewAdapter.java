package com.isang.puntos.youaudiodemand;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

/**
 * Created by ADMIN on 04/08/2016.
 */
public class GUIRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    public interface OnLoadMoreListener
    {
        void onLoadMore();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder
    {
        private TextView tv1 = null;
        private TextView tv3 = null;
        private ImageView imageIcon = null;
        private ImageView imageClick = null;

        public UserViewHolder(View itemView)
        {
            super(itemView);
            tv1 = (TextView) itemView.findViewById(R.id.row_textView1);
            tv3 = (TextView) itemView.findViewById(R.id.row_textView2);
            imageIcon = (ImageView) itemView.findViewById(R.id.row_imageView1);
            imageClick = (ImageView) itemView.findViewById(R.id.row_click_imageView1);
        }
    }

    public static class LoadingViewHolder extends RecyclerView.ViewHolder
    {
        public ProgressBar progressBar;

        public LoadingViewHolder(View itemView)
        {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
        }
    }

    private TextView tv1 = null;
    private TextView tv3 = null;
    private ImageView imageIcon = null;
    private ImageView imageClick = null;
    private RecyclerView mRecyclerView = null;
    private Context appContext = null;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private static OnLoadMoreListener mOnLoadMoreListener;
    ArrayList<HandlerSongInformation> mSearchRes;
    private boolean isLoading;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;
    private String currNextPageToken = "";
    private RelativeLayout thisLayout = null;


    public GUIRecyclerViewAdapter(Context aContext, RecyclerView recyclerView, ArrayList<HandlerSongInformation> searchRes)
    {
        mRecyclerView = recyclerView;
        appContext = aContext;
        mSearchRes = searchRes;

        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                super.onScrolled(recyclerView, dx, dy);

                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold))
                {
                    if (mOnLoadMoreListener != null)
                    {
                        mOnLoadMoreListener.onLoadMore();
                    }
                    isLoading = true;
                }
            }
        });

    }


    @Override
    public int getItemViewType(int position)
    {
        return mSearchRes.size() - 1 == position && !currNextPageToken.equals("") ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        if (viewType == VIEW_TYPE_ITEM)
        {
            View view = LayoutInflater.from(appContext).inflate(R.layout.search_view_row, parent, false);
            tv1 = (TextView) view.findViewById(R.id.row_textView1);
            tv3 = (TextView) view.findViewById(R.id.row_textView2);
            imageIcon = (ImageView) view.findViewById(R.id.row_imageView1);
            imageClick = (ImageView) view.findViewById(R.id.row_click_imageView1);
            thisLayout = (RelativeLayout) view.findViewById(R.id.svrRelativeLayout);
            return new UserViewHolder(view);
        }
        else if (viewType == VIEW_TYPE_LOADING)
        {
            View view = LayoutInflater.from(appContext).inflate(R.layout.progressbar_item, parent, false);
            return new LoadingViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position)
    {
        if (holder instanceof UserViewHolder)
        {
            try
            {
                tv1.setText(mSearchRes.get(position).songTitle);
                tv3.setText(mSearchRes.get(position).duration);
                String imName = mSearchRes.get(position).songTitle;
                File filePath = appContext.getFileStreamPath(imName + ".png");
                FileInputStream fi = null;
                fi = new FileInputStream(filePath);
                Bitmap bm = BitmapFactory.decodeStream(fi);
                imageIcon.setImageBitmap(bm);
            }
            catch (Exception e)
            {
                new AsyncTaskImageDownloader(imageIcon).execute(mSearchRes.get(position).thumbNail);
            }

            thisLayout.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                  playAudio(position);
                }
            });
            imageClick.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    switch (v.getId())
                    {
                        case R.id.row_click_imageView1:
                            PopupMenu popup = new PopupMenu(appContext, v);
                            popup.getMenuInflater().inflate(R.menu.popup_menu_search, popup.getMenu());
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
                                            playAudio(position);
                                            break;
                                        default:
                                            break;
                                    }
                                    return true;
                                }
                            });
                            break;
                    }

                }
            });
        }
        else if (holder instanceof LoadingViewHolder)
        {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount()
    {
        return mSearchRes == null ? 0 : mSearchRes.size();
    }


    public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener)
    {
        this.mOnLoadMoreListener = mOnLoadMoreListener;
    }

    public void setLoaded()
    {
        isLoading = false;
    }

    public void setNextPageToken(String nextPageToken)
    {
        currNextPageToken = nextPageToken;
    }

    private void showPlaylistDialog(final int position)
    {
        try
        {
            String names[] = (new HandlerDatabase(appContext).getListOfPlayList(false)).toArray(new String[0]);
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(appContext);
            LayoutInflater inflater = (LayoutInflater) appContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View convertView = (View) inflater.inflate(R.layout.listviewdialog, null);
            convertView.setBackgroundColor(ContextCompat.getColor(appContext, android.R.color.background_light));
            alertDialog.setView(convertView);
            alertDialog.setTitle("Playlist");
            ListView lv = (ListView) convertView.findViewById(R.id.listView1);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(appContext, android.R.layout.simple_list_item_1, names);
            lv.setAdapter(adapter);
            final AlertDialog ad = alertDialog.show();
            lv.setOnItemClickListener(new ListView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
                {
                    /*HandlerGlobal.songTitle = mSearchRes.get(arg2).songTitle;
                    HandlerGlobal.id = mSearchRes.get(arg2).id;
                    HandlerGlobal.thumbNail = mSearchRes.get(arg2).thumbNail;*/
                    AsyncTaskDownloadMusic asyncTaskDownloadMusic = new AsyncTaskDownloadMusic(appContext);
                    asyncTaskDownloadMusic.setMode(2);
                    asyncTaskDownloadMusic.execute("http://www.youtubeinmp3.com/download/?video=https://www.youtube.com/watch?v=" + mSearchRes.get(position).id + "," +
                                                   mSearchRes.get(position).songTitle + "," +
                                                   mSearchRes.get(position).id + "," +
                                                   mSearchRes.get(position).thumbNail + "," +
                                                   mSearchRes.get(position).duration);
                    new HandlerDatabase(appContext).addMusic((String) arg0.getItemAtPosition(arg2), mSearchRes.get(position).songTitle, mSearchRes.get(position).id, mSearchRes.get(position).thumbNail, mSearchRes.get(position).duration);
                    ad.dismiss();
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void playAudio(int position)
    {
        HandlerGlobal.songTitle = mSearchRes.get(position).songTitle;
        HandlerGlobal.id = mSearchRes.get(position).id;
        HandlerGlobal.thumbNail = mSearchRes.get(position).thumbNail;
        HandlerGlobal.duration = mSearchRes.get(position).duration;
        AsyncTaskDownloadMusic asyncTaskDownloadMusic = new AsyncTaskDownloadMusic(appContext);
        asyncTaskDownloadMusic.setMode(1);
        asyncTaskDownloadMusic.execute("http://www.youtubeinmp3.com/download/?video=https://www.youtube.com/watch?v=" + mSearchRes.get(position).id + "," +
                                                                                                                        mSearchRes.get(position).songTitle + "," +
                                                                                                                        mSearchRes.get(position).id + "," +
                                                                                                                        mSearchRes.get(position).thumbNail + "," +
                                                                                                                        mSearchRes.get(position).duration);
    }

}







