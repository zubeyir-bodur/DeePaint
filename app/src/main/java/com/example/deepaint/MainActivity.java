package com.example.deepaint;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button openCamera;
    private Button savedObjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //openCamera.setEnabled(false);
        //savedObjects.setEnabled(false);

        setContentView(R.layout.activity_main);
    }
}