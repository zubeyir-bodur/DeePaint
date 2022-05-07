package com.example.deepaint;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.editScreen).setVisibility(View.GONE);
        init();
    }

    private static final int REQUEST_PERMISSIONS = 1234;
    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int PERMISSION_COUNT = 2;

    @SuppressLint("NewApi")
    private boolean notPermissions(){
        for (int i = 0; i < PERMISSION_COUNT; i++){
            if(checkSelfPermission(PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED){
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && notPermissions()){
            requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String [] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS && grantResults.length > 0){
            if(notPermissions()){
                ((ActivityManager) this.getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();
                recreate();
            }
        }
        startActivity(new Intent(MainActivity.this, AuthPageActivity.class));
    }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.welcomeScreen).setVisibility(View.VISIBLE);
                findViewById(R.id.editScreen).setVisibility(View.GONE);
            }
        });

    }

    private static final int REQUEST_IMAGE_CAPTURE = 1012;
    private static final String appID = "deepaint";
    private Uri imageUri;

    private File createImageFile(){
        final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format( new Date());
        final String imageFileName = "/JPEG_" + timeStamp + ".jpg";
        final File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(storageDir+imageFileName);
    }

    private boolean editMode = false;
    private Bitmap bitmap;
    private int width = 0;
    private int height = 0;
    private static final int MAX_PIXEL_COUNT = 2048;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if( resultCode != RESULT_OK){
            return;
        }

        //hello world
        if(requestCode == REQUEST_IMAGE_CAPTURE){
            if(imageUri == null){
                final SharedPreferences p = getSharedPreferences(appID, 0);
                final String path = p.getString("path", "");
                if(path.length() < 1){
                    recreate();
                    return;
                }
                imageUri = Uri.parse("file://" + path);

            }

            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imageUri));
        }
        else if( data == null){
            recreate();
            return;
        }
        else if(requestCode == REQUEST_PICK_IMAGE){
            imageUri = data.getData();
        }

        final ProgressDialog dialog = ProgressDialog.show(MainActivity.this, "Loading",
                "Please Wait", true );



        editMode = true;


        findViewById(R.id.welcomeScreen).setVisibility(View.GONE);
        findViewById(R.id.editScreen).setVisibility(View.VISIBLE);

        new Thread(){
            public void run(){
                bitmap = null;
                final BitmapFactory.Options bmpOptions = new BitmapFactory.Options();
                bmpOptions.inBitmap = bitmap;
                bmpOptions.inJustDecodeBounds = true;
                try( InputStream input = getContentResolver().openInputStream(imageUri)){
                    bitmap = BitmapFactory.decodeStream(input, null, bmpOptions);
                }catch (IOException e){
                    e.printStackTrace();
                }
                bmpOptions.inJustDecodeBounds = false;
                width = bmpOptions.outWidth;
                height = bmpOptions.outHeight;
                int resizeScale = 2;
                if(width > MAX_PIXEL_COUNT){
                    resizeScale = width/MAX_PIXEL_COUNT;
                } else if( height > MAX_PIXEL_COUNT){
                    resizeScale = height/MAX_PIXEL_COUNT;
                }
                if(width/resizeScale > MAX_PIXEL_COUNT || height/resizeScale > MAX_PIXEL_COUNT){
                    resizeScale++;
                }
                bmpOptions.inSampleSize = resizeScale;
                InputStream input = null;
                try {
                    input = getContentResolver().openInputStream(imageUri);
                }catch (FileNotFoundException e){
                    e.printStackTrace();
                    recreate();
                    return;
                }
                bitmap = BitmapFactory.decodeStream(input, null, bmpOptions);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(bitmap);
                        dialog.cancel();
                    }
                });
            }
        }.start();


    

}