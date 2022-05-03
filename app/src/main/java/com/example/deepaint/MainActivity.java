package com.example.deepaint;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
    protected void onResume(){
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
    }

    private static final int REQUEST_PICK_IMAGE = 12345;
    private ImageView imageView;

    //TODO Drawing Canvas for EditPage
    private GLSurfaceView glView;
    private boolean isEditor = false;

    @SuppressLint("ClickableViewAccessibility")
    private void init(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }

        imageView = findViewById(R.id.imageView3);

        if(!MainActivity.this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            findViewById(R.id.openCameraButton).setVisibility(View.GONE);
        }

        final Button openCameraButton = findViewById(R.id.openCameraButton);

        openCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(takePictureIntent.resolveActivity(getPackageManager())!= null){
                    //create a file for the photo that was just taken
                    final File photoFile = createImageFile();
                    imageUri = Uri.fromFile(photoFile);
                    final SharedPreferences myPrefs = getSharedPreferences(appID,0 );
                    myPrefs.edit().putString("path", photoFile.getAbsolutePath()).apply();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
                else{
                    Toast.makeText(MainActivity.this, "Your Camera app is not compatible",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });


        final Button galleryButton = findViewById(R.id.galleryButton);

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                final Intent pickIntent = new Intent(Intent.ACTION_PICK);
                pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                final Intent chooserIntent  = Intent.createChooser(intent, "Choose from Gallery");
                startActivityForResult(chooserIntent, REQUEST_PICK_IMAGE);
            }
        });

        final Button objectsButton = findViewById(R.id.objectsButton);

        final Button backButton = findViewById(R.id.buttonBack);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.welcomeScreen).setVisibility(View.VISIBLE);
                findViewById(R.id.editScreen).setVisibility(View.GONE);
            }
        });


        // DRAWING PART - EDIT BUTTON

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.

        // Read the edit button
        final Button editButton = findViewById(R.id.button4);
        // Define action listener for edit button
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // compute the coordinates of the top left corner of the img
                int deviceWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
                int deviceHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
                float topLeftX = (float)(deviceWidth - width) / 2;
                float topLeftY = (float)(deviceHeight - height) / 2;
                float[] coordinates = {topLeftX, topLeftY};

                // When Edit is pressed, open the drawing canvas
                isEditor = !isEditor;
                // And display other buttons related to saving what is drawn
                if (isEditor) {
                    // Enable the listener - draw when user touches
                    // TODO button click does not result in a drawing...
                    glView = new Canvas(MainActivity.this, coordinates, width, height);
                } else {
                    // Save the drawings
                    glView = null;
                }
            }
        });

        // ImageView = Get the coordinates of the touched/clicked pixel
        // Click does not have a motion event, so it is not possible to
        // retrieve coordinates of a click event...
        imageView.setOnTouchListener(new View.OnTouchListener() {
            // TODO do this operations only when image removal button is pressed
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                float eventX = event.getX();
                float eventY = event.getY();
                float[] eventXY = new float[] {eventX, eventY};

                Matrix invertMatrix = new Matrix();
                ((ImageView)view).getImageMatrix().invert(invertMatrix);

                invertMatrix.mapPoints(eventXY);
                int x = (int) eventXY[0];
                int y = (int) eventXY[1];

                System.out.println(
                        "touched position in imgView: "
                                + String.valueOf(eventX) + " / "
                                + String.valueOf(eventY));
                System.out.println(
                        "touched position in actual image: "
                                + String.valueOf(x) + " / "
                                + String.valueOf(y));

                Drawable imgDrawable = ((ImageView)view).getDrawable();
                Bitmap bitmap = ((BitmapDrawable)imgDrawable).getBitmap();

                System.out.println(
                        "drawable size: "
                                + String.valueOf(bitmap.getWidth()) + " / "
                                + String.valueOf(bitmap.getHeight()));

                //Limit x, y range within bitmap
                if(x < 0){
                    x = 0;
                }else if(x > bitmap.getWidth()-1){
                    x = bitmap.getWidth()-1;
                }

                if(y < 0){
                    y = 0;
                }else if(y > bitmap.getHeight()-1){
                    y = bitmap.getHeight()-1;
                }

                int touchedRGB = bitmap.getPixel(x, y);

                System.out.println("touched color: " + "#" + Integer.toHexString(touchedRGB));


                // TODO send a request to check if the region
                //  is contained by a segmented region
                //  actual coordinates in the image is (x, y)

                return true;
            }
        });

    }

    private static final int REQUEST_IMAGE_CAPTURE = 1012;
    private static final String appID = "deepaint";
    private Uri imageUri;

    private File createImageFile(){
        final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
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
        else if( data == null) {
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

}