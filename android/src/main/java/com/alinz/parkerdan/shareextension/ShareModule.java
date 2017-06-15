package com.alinz.parkerdan.shareextension;

import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import android.graphics.Bitmap;
import java.io.InputStream;


public class ShareModule extends ReactContextBaseJavaModule {


  public ShareModule(ReactApplicationContext reactContext) {
      super(reactContext);
  }

  @Override
  public String getName() {
      return "ReactNativeShareExtension";
  }

  @ReactMethod
  public void close() {
    getCurrentActivity().finish();
  }

  @ReactMethod
  public void data(Promise promise) {
      promise.resolve(processIntent());
  }

  public WritableMap processIntent() {
      WritableMap map = Arguments.createMap();

      String value = "";
      String type = "";
      String action = "";

      Activity currentActivity = getCurrentActivity();

      if (currentActivity != null) {
        Intent intent = currentActivity.getIntent();
        action = intent.getAction();
        type = intent.getType();
        if (type == null) {
          type = "";
        }
        if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
          value = intent.getStringExtra(Intent.EXTRA_TEXT);
        }
        else if (Intent.ACTION_SEND.equals(action) && ("image/*".equals(type) || "image/jpeg".equals(type) || "image/png".equals(type) || "image/jpg".equals(type) ) ) {
          String sharedImagePath = null;
          Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
          File cacheDir = currentActivity.getCacheDir();
          File tempFile = new File(cacheDir, "SharedImage");
          //android.util.Log.i("ReactNativeShare", "Sharing path " + tempFile.toString());
          tempFile.delete();

          InputStream inStream = null;
          OutputStream outStream = null;
          try {
            inStream = currentActivity.getContentResolver().openInputStream(uri);
            outStream = new FileOutputStream(tempFile);
            //android.util.Log.i("ReactNativeShare", "Opened streams");
            byte[] buf = new byte[4096];
            int len;
            while((len=inStream.read(buf))>0){
                outStream.write(buf,0,len);
            }
            sharedImagePath = Uri.fromFile(tempFile).toString();
            tempFile.deleteOnExit()
          } catch (FileNotFoundException ex) {
            sharedImagePath = "file://ERROR/FILENOTFOUND";
          } catch (IOException ex){
            sharedImagePath = "file://ERROR/IO";
          }
          finally {
            if (inStream !=null) {
              try {
                inStream.close();
              } catch (IOException ex) {
                // oh well, eh?
            }
            }
            if (outStream !=null) {
              try {
                outStream.close();
              } catch (IOException ex) {
                // oh well, eh?
              }
            }
          }
         //android.util.Log.i("ReactNativeShare", "sharedImagePath is "+ sharedImagePath);
         value = sharedImagePath;

       } else {
         value = "";
       }
      } else {
        value = "";
        type = "";
      }

      map.putString("type", type);
      map.putString("value",value);

      return map;
  }
}
