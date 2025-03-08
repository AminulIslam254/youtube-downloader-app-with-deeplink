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

import android.Manifest;


public class MyMainModule extends ReactContextBaseJavaModule  {
    MyMainModule(ReactApplicationContext context) {

        super(context);
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