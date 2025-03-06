package com.ytapp; // replace your-apps-package-name with your app’s package name
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

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;


public class MyMainModule extends ReactContextBaseJavaModule {
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
}