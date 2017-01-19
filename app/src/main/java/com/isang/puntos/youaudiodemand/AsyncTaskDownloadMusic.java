package com.isang.puntos.youaudiodemand;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

class AsyncTaskDownloadMusic extends AsyncTask<String, String, String>
{

    private static boolean isLoading = true;
    private ProgressDialog pDialog;
    private Context context;
    private IDownloadComplete downloadCallBack;
    private int mode = 1;
    private String displayError = "";

    public AsyncTaskDownloadMusic(Context con)
    {
        context = con;
        downloadCallBack = (IDownloadComplete) context;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        isLoading = true;
        showDialog();
    }

    @Override
    protected String doInBackground(String... f_url)
    {
        int count;
        String path = getContext().getFilesDir().toString() + "/" + f_url[0].split(",")[1] + ".mp3";
        try
        {
            if (isLoading)
            {
                if (!(new File(path).exists()))
                {
                    URL url = new URL(getDLLink(f_url[0].split(",")[0]));
                    URLConnection connection = url.openConnection();
                    connection.connect();
                    int lengthOfFile = connection.getContentLength();

                    InputStream input = new BufferedInputStream(url.openStream(), 8192);
                    OutputStream output = new FileOutputStream(new File(path));//getContext().getFilesDir(), "/" + f_url[0].split(",")[1] + ".mp3"));

                    byte data[] = new byte[1024];

                    long total = 0;

                    while ((count = input.read(data)) != -1 && pDialog.isShowing())
                    {
                        total += count;
                        publishProgress("" + (int) ((total * 100) / lengthOfFile));
                        output.write(data, 0, count);
                    }
                    if (total < lengthOfFile || lengthOfFile == -1)
                    {
                        new File(getContext().getFilesDir().toString() + "/" + f_url[0].split(",")[1] + ".mp3").delete();
                        displayError = "Download was interrupted";
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if(displayError.equals(""))
                {
                    Thread.sleep(1000);
                    downloadThumbNail(f_url[0].split(",")[3], f_url[0].split(",")[1]);
                    downloadThumbNail(f_url[0].split(",")[3], f_url[0].split(",")[1]);
                }
            }
        }
        catch (Exception e)
        {
            Log.e("Error: ", e.getMessage());
            if(new File(path).exists())
                new File(path).delete();
            displayError = "Cannot load audio";
        }

        return null;
    }

    protected void onProgressUpdate(String... progress)
    {
        pDialog.setProgress(Integer.parseInt(progress[0]));
    }

    @Override
    protected void onPostExecute(String file_url)
    {
        dismissDialog();
        if (isLoading)
        {
            downloadCallBack.onDownloadCompleted(mode);

            if(!displayError.isEmpty())
                Toast.makeText(getContext(), displayError, Toast.LENGTH_LONG).show();

            isLoading = false;
        }
    }

    private void showDialog()
    {
        pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Loading music. Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setMax(100);
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setCancelable(true);
        pDialog.show();
    }

    private void dismissDialog()
    {
        pDialog.dismiss();
    }

    private String getDLLink(String link)
    {
        String downloadLink = "";
        try
        {
            URL url = new URL(link);

            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;
            while ((str = in.readLine()) != null)
            {
                if (str.contains("/download/get/?i="))
                {
                    downloadLink = "http://www.youtubeinmp3.com" + str.substring(str.indexOf("/download/get/?i="), str.indexOf("\">"));
                    break;
                }
            }
            in.close();
            return downloadLink;
        }
        catch (Exception e)
        {

        }
        finally
        {
            return downloadLink;
        }
    }

    private void downloadThumbNail(String thumbNailLink, String title)
    {
        try
        {
            URL url = new URL(thumbNailLink);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            Bitmap bm = BitmapFactory.decodeStream(is);
            FileOutputStream fos = context.openFileOutput(title + ".png", Context.MODE_PRIVATE);

            ByteArrayOutputStream outstream = new ByteArrayOutputStream();

            bm.compress(Bitmap.CompressFormat.PNG, 100, outstream);
            byte[] byteArray = outstream.toByteArray();

            fos.write(byteArray);
            //HandlerGlobal.thumbNail = context.getDir(title + ".png", Context.MODE_PRIVATE).getPath();
            fos.close();
            //HandlerGlobal.thumbNail = getContext().getFilesDir() + "/" + HandlerGlobal.songTitle + ".png";
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void setMode(int m)
    {
        mode = m;
    }


    public void setContext(Context con)
    {
        context = con;
    }

    public Context getContext()
    {
        return context;
    }


}
