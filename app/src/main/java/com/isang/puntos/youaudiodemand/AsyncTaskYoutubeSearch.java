package com.isang.puntos.youaudiodemand;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.ListView;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Joiner;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class AsyncTaskYoutubeSearch extends AsyncTask<Void, Void, ArrayList<HandlerSongInformation>>
{
    private static final String PROPERTIES_FILENAME = "youtube.properties";
    private static final long NUMBER_OF_VIDEOS_RETURNED = 25;
    private static YouTube youtube;
    private List<HandlerSongInformation> searchResult;
    private String keyword = "";
    ProgressDialog progressBar;
    private Context context;
    List<SearchResult> searchResultList;
    private ISearchCompleted iSearchCallback;
    private String nextPageToken = "";
    private static boolean isLoading = true;

    public AsyncTaskYoutubeSearch(Context con)
    {
        try
        {
            context = con;
            youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer()
            {
                public void initialize(HttpRequest request) throws IOException
                {

                }
            }).setApplicationName("youtube-cmdline-search-sample").build();
            iSearchCallback = (ISearchCompleted) context;

        }
        catch (Exception e)
        {

        }
    }

    @Override
    protected void onPreExecute()
    {
        isLoading = true;
        if (nextPageToken.equals(""))
        {
            progressBar = new ProgressDialog(getContext());
            progressBar.setCancelable(false);
            progressBar.setMessage("Loading....");
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.setMax(100);
            progressBar.show();
        }
    }

    @Override
    protected ArrayList<HandlerSongInformation> doInBackground(Void... voids)
    {
        if(isLoading)
        {
            if (searchResult != null)
            {
                searchResult.clear();
            }

            searchResult = new ArrayList<HandlerSongInformation>();

            try
            {
                YouTube.Search.List search = youtube.search().list("id,snippet");

                String queryTerm = keyword;
                String apiKey = "AIzaSyDR5i0jQLLp_0RgMFUNm4ncTWhfRLqPVhY";//properties.getProperty("youtube.apikey");
                search.setKey(apiKey);
                search.setQ(queryTerm);
                search.setType("video");

                //search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
                search.setFields("items(id/videoId),nextPageToken,pageInfo");
                search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
                if (!nextPageToken.equals(""))
                {
                    search.setPageToken(nextPageToken);
                }
                SearchListResponse searchResponse = search.execute();
                searchResultList = searchResponse.getItems();
                List<String> videoIds = new ArrayList<String>();

                for (SearchResult searchResult : searchResultList)
                {
                    videoIds.add(searchResult.getId().getVideoId());
                }
                Joiner stringJoiner = Joiner.on(',');
                String videoId = stringJoiner.join(videoIds);

                YouTube.Videos.List listVideosRequest = youtube.videos().list("snippet, contentDetails").setId(videoId).setKey("AIzaSyDR5i0jQLLp_0RgMFUNm4ncTWhfRLqPVhY");
                VideoListResponse listResponse = listVideosRequest.execute();

                List<Video> videoList = listResponse.getItems();
                nextPageToken = searchResponse.getNextPageToken();
                if (nextPageToken == null)
                {
                    nextPageToken = "";
                }
                if (searchResultList != null)
                {
                    accumulateResults(videoList.iterator(), queryTerm);
                }
                return (ArrayList) searchResult;
            }

            catch (Exception e)
            {
                System.err.println("There was an error reading " + PROPERTIES_FILENAME + ": " + e.getCause() + " : " + e.getMessage());
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<HandlerSongInformation> songInfo)
    {
        if (progressBar != null && progressBar.isShowing())
        {
            progressBar.dismiss();
        }
        if(isLoading)
        {
            isLoading = false;
            iSearchCallback.onSearchCompleted(songInfo, nextPageToken);
        }
    }

    public void setKeyword(String kWord)
    {
        keyword = kWord;
    }


    public void setNextPageToken(String npToken)
    {
        nextPageToken = npToken;
    }

    private void accumulateResults(Iterator<Video> iteratorVideoResults, String query)
    {
        while (iteratorVideoResults.hasNext())
        {
            Video singleVideo = iteratorVideoResults.next();
            Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();
            HandlerSongInformation handlerSongInformation = new HandlerSongInformation();
            handlerSongInformation.id = singleVideo.getId();
            handlerSongInformation.songTitle = singleVideo.getSnippet().getTitle().replace("/", "-");
            handlerSongInformation.thumbNail = thumbnail.getUrl();
            handlerSongInformation.duration = singleVideo.getContentDetails().getDuration().replaceAll("(H)([0-9]+)(S)", "H00M$2")
                    .replaceAll("PT|S", "")
                    .replaceAll("[HM]", ":")
                    .replaceAll("(:)([0-9])(:)", ":0$2:")
                    .replaceAll("(:)([0-9])$", ":0$2")
                    .replaceAll("(:)$", ":00");
            searchResult.add(handlerSongInformation);
        }
    }

    public Context getContext()
    {
        return context;
    }

}

