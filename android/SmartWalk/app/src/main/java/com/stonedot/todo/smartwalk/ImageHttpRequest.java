package com.stonedot.todo.smartwalk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by komatsu on 2016/11/03.
 */

public class ImageHttpRequest extends AsyncTask<Uri.Builder, Void, Bitmap> {

    private ImageView imageView;
    private HttpURLConnection con = null;
    private InputStream stream = null;
    private Bitmap bitmap = null;

    public ImageHttpRequest(ImageView imageView) {
        this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(Uri.Builder... builders) {
        try {
            URL url = new URL(builders[0].toString());
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.connect();
            stream = con.getInputStream();
            bitmap = BitmapFactory.decodeStream(stream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(con != null) con.disconnect();
            try {
                if(stream != null) stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }
}
