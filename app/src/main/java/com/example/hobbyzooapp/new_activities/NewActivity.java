package com.example.hobbyzooapp.new_activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hobbyzooapp.R;

public class NewActivity extends AppCompatActivity {

    String name, animalName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_activity);
    }
}
