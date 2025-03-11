package com.ytapp; // replace your-apps-package-name with your app’s package name
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableNativeArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_android.YoutubeDLRequest;

import android.Manifest;

import java.io.*;




public class MyMainModule extends ReactContextBaseJavaModule  {
    private static final String TAG = MyMainModule.class.getSimpleName();
    MyMainModule(ReactApplicationContext context) {

        super(context);
        try {
            YoutubeDL.getInstance().init(context);
        } catch (Exception e) {
            Log.e(TAG, "failed to initialize youtubedl-android", e);
        }
    }

    @Override
    public String getName() {
        return "MyMainModule";
    }

    @ReactMethod
    public void getAvailableFormats(String youtubeLink, Callback callback) {
        new YouTubeExtractor(getReactApplicationContext()) {
            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                if (ytFiles == null) {
                    Log.e("YouTubeExtractor", "Extraction failed!");
                    callback.invoke("Error: Unable to extract video formats");
                    return;
                }

                WritableArray formatsArray = new WritableNativeArray();
                String videoTitle = vMeta.getTitle();

                // Loop through all available formats
                for (int i = 0; i < ytFiles.size(); i++) {
                    int key = ytFiles.keyAt(i);
                    YtFile ytFile = ytFiles.get(key);
                    // Filter for audio-only or videos with 360p+
                    if (ytFile.getFormat().getHeight() == -1 || ytFile.getFormat().getHeight() >= 360) {
                        WritableMap formatMap = new WritableNativeMap();
                        formatMap.putString("title", videoTitle);
                        formatMap.putString("format", (ytFile.getFormat().getHeight() == -1) ?
                                "Audio " + ytFile.getFormat().getAudioBitrate() + " kbps" :
                                ytFile.getFormat().getHeight() + "p");
                        formatMap.putString("url", ytFile.getUrl());
                        formatsArray.pushMap(formatMap);
                    }
                }

                // Send data back to React Native
                callback.invoke(formatsArray);
            }
        }.extract(youtubeLink, true, true);
    }

    @ReactMethod
    public void messageWarning(String message){
        Toast.makeText(getReactApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }




    @ReactMethod
    public void downloadFromYoutube(String youtubeDlUrl, String downloadTitle, String fileName,Callback callback){
        ReactApplicationContext context=getReactApplicationContext();
//        String ytDlpPath=installYtDlpIfneeded(context);
//
//        String directURL= getYoutubeDownloadURL(ytDlpPath,youtubeDlUrl);
//        Log.d("got info",ytDlpPath+" "+directURL);
//        downloadFromUrl(directURL,downloadTitle,fileName,callback);

        Log.d("Url from source",youtubeDlUrl);
        File youtubeDLDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "youtubedl-android");
        YoutubeDLRequest request = new YoutubeDLRequest(youtubeDlUrl);
        request.addOption("-o", youtubeDLDir.getAbsolutePath() + "/%(title)s.%(ext)s");
        YoutubeDL.getInstance().execute(request,null, (progress, etaInSeconds,line) -> {
            System.out.println(progress + "% (ETA " + etaInSeconds + " seconds) | " + line);
            return null;
        });
    }
//    @ReactMethod
//    public void checkAssests(){
//        ReactApplicationContext context=getReactApplicationContext();
//        try {
//            String[] files = context.getAssets().list("");
//            for (String file : files) {
//                Log.d("Assets", "File: " + file);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public String installYtDlpIfneeded(ReactApplicationContext context){
        File ytDlpFile=new File(context.getFilesDir(),"yt-dlp");
        Log.d("yt-dlp", "Checking file: " + ytDlpFile.getAbsolutePath() + ", Exists: " + ytDlpFile.exists());
        try {
            if(!ytDlpFile.exists()){
                URL url = new URL("https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp");
                HttpURLConnection connection=(HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.connect();
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e("yt-dlp", "Server returned HTTP " + connection.getResponseCode());
                    return "";
                }
                InputStream in =connection.getInputStream();
                File ylDlpFile=new File(context.getFilesDir(),"yt-dlp");
                Log.d("yldlpFile", "Path: " + ylDlpFile.getAbsolutePath() + ", Exists: " + ylDlpFile.exists());
                FileOutputStream out = new FileOutputStream(ylDlpFile);
                byte[] buffer= new byte[1024];
                int length;
                while ((length = in.read(buffer)) != -1) {
                    out.write(buffer, 0, length);
                }

                in.close();
                out.close();
                connection.disconnect();
                ylDlpFile.setExecutable(true,false);
                Log.d("yt-dlp", "yt-dlp downloaded and saved at: " + ylDlpFile.getAbsolutePath());

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ytDlpFile.getAbsolutePath();

    }

    public String getYoutubeDownloadURL(String ytDlpPath,String youtubeUrl){
        try{
            ProcessBuilder pb = new ProcessBuilder("/system/bin/sh", "-c", ytDlpPath + " -f best -g " + youtubeUrl);
            pb.redirectErrorStream(true);
            Process process=pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String url= reader.readLine();
            Log.d("URL came",url);
            process.waitFor();
            if (url != null && url.startsWith("http")) {
                return url;
            }

        }catch (Exception e){
            Log.e("Exception happened", Log.getStackTraceString(e));

        }
        return null;
    }



    @ReactMethod
    public void downloadFromUrl(String youtubeDlUrl, String downloadTitle, String fileName,Callback callback) {
        ReactApplicationContext context=getReactApplicationContext();
        Log.d("Download url",youtubeDlUrl);
        if (context == null) {
            callback.invoke("Error: Unable to get current activity");
            return;
        }
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.Q){
            if(ContextCompat.checkSelfPermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions((Activity) context.getCurrentActivity(),new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                callback.invoke("Storage permission required");
                return;
            }
        }

        Uri uri = Uri.parse(youtubeDlUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(downloadTitle);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.allowScanningByMediaScanner();
        DownloadManager manager=(DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if(manager==null){
            callback.invoke("Download manager not available");
            return;
        }
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            ContentValues values=new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME,fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE,"video/mp4");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_DOWNLOADS);
            Uri videoUri= context.getContentResolver().insert(MediaStore.Files.getContentUri("external"),values);
            if(videoUri!=null){
                request.setDestinationUri(videoUri);
            }else {
                callback.invoke("Error: Failed to create file URI");
                return;
            }
        }
        else{
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,fileName);
        }

        Log.d("Download", "Download URL: " + youtubeDlUrl);

        manager.enqueue(request);
        callback.invoke("Download Started");
    }
}