package plugin.google.maps;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;

public class AsyncLoadImage extends AsyncTask<String, Void, Bitmap> {
  private HashMap<String, Bitmap> mCache = null;
  private AsyncLoadImageInterface targetPlugin;
  private int mWidth = -1;
  private int mHeight = -1;

  public AsyncLoadImage(AsyncLoadImageInterface plugin) {
    targetPlugin = plugin;
  }

  public AsyncLoadImage(int width, int height, AsyncLoadImageInterface plugin) {
    targetPlugin = plugin;
    mWidth = width;
    mHeight = height;
  }

  public AsyncLoadImage(AsyncLoadImageInterface plugin, HashMap<String, Bitmap> cache) {
    mCache = cache;
    targetPlugin = plugin;
  }
  public AsyncLoadImage(int width, int height, AsyncLoadImageInterface plugin, HashMap<String, Bitmap> cache) {
    mCache = cache;
    targetPlugin = plugin;
    mWidth = width;
    mHeight = height;
  }
  
  @SuppressLint("NewApi")
  protected Bitmap doInBackground(String... urls) {
    try {
      if (mCache != null && mCache.containsKey(urls[0])) {
        Bitmap myBitmap = mCache.get(urls[0]);
        return myBitmap.copy(Bitmap.Config.ARGB_8888, true);
        //return Bitmap.createBitmap(mCache.get(urls[0]));
      }
      URL url= new URL(urls[0]);
      HttpURLConnection http = (HttpURLConnection)url.openConnection(); 
      http.setRequestMethod("GET");
      http.setUseCaches(true);
      http.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
      http.addRequestProperty("User-Agent", "Mozilla");
      http.setInstanceFollowRedirects(true);
      HttpURLConnection.setFollowRedirects(true);
      
      boolean redirect = false;
      // normally, 3xx is redirect
      int status = http.getResponseCode();
      if (status != HttpURLConnection.HTTP_OK) {
        if (status == HttpURLConnection.HTTP_MOVED_TEMP
          || status == HttpURLConnection.HTTP_MOVED_PERM
            || status == HttpURLConnection.HTTP_SEE_OTHER)
        redirect = true;
      }
      if (redirect) {
        
        // get redirect URL from "location" header field
        String newUrl = http.getHeaderField("Location");
     
        // get the cookie if need, for login
        String cookies = http.getHeaderField("Set-Cookie");
     
        // open the new connection again
        http = (HttpURLConnection) new URL(newUrl).openConnection();
        http.setUseCaches(true);
        http.setRequestProperty("Cookie", cookies);
        http.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
        http.addRequestProperty("User-Agent", "Mozilla");
      }
      
      Bitmap myBitmap = null;
      InputStream inputStream = http.getInputStream();
      
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      int nRead;
      byte[] data = new byte[16384];
      while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
        buffer.write(data, 0, nRead);
      }
      buffer.flush();
      inputStream.close();
      byte[] imageBytes = buffer.toByteArray();
      
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inJustDecodeBounds = true;
      myBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);
      
      if (mWidth < 1 && mHeight < 1) {
        mWidth = options.outWidth;
        mHeight = options.outHeight;
      }
      
      // Resize
      float density = Resources.getSystem().getDisplayMetrics().density;
      int newWidth = (int)(mWidth * density);
      int newHeight = (int)(mHeight * density);
      
      /**
       * http://stackoverflow.com/questions/4821488/bad-image-quality-after-resizing-scaling-bitmap#7468636
       */
      Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Config.ARGB_8888);

      float ratioX = newWidth / (float) options.outWidth;
      float ratioY = newHeight / (float) options.outHeight;
      float middleX = newWidth / 2.0f;
      float middleY = newHeight / 2.0f;
      
      options.inJustDecodeBounds = false;
      options.inSampleSize = (int) Math.max(ratioX, ratioY);

      Matrix scaleMatrix = new Matrix();
      scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

      Canvas canvas = new Canvas(scaledBitmap);
      canvas.setMatrix(scaleMatrix);

      myBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);
      canvas.drawBitmap(myBitmap, middleX - options.outWidth / 2, middleY - options.outHeight / 2, new Paint(Paint.FILTER_BITMAP_FLAG));
      myBitmap.recycle();
      myBitmap = scaledBitmap;
      scaledBitmap = null;
      canvas = null;
      imageBytes = null;

      if (mCache != null) {
        mCache.put(urls[0], myBitmap.copy(Bitmap.Config.ARGB_8888, true));
      }
      return myBitmap;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  protected void onPostExecute(Bitmap image) {
    System.gc();
    this.targetPlugin.onPostExecute(image);
  }
}
