package com.example.hobbyzooapp.new_activities;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hobbyzooapp.R;


public class NewCategory extends AppCompatActivity {

    String name;
    int colorRGB = 0;
    Color color;
    int red, blue, green;
    ImageView imgView;
    TextView mColorValues;
    View displayColors;
    Bitmap bitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_category);

        Button validationButton = findViewById(R.id.validationButton);
        imgView = findViewById(R.id.colorPickers);
        displayColors = findViewById(R.id.displayColors);

        imgView.setDrawingCacheEnabled(true);
        imgView.buildDrawingCache(true);

        imgView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE){
                    bitmap = imgView.getDrawingCache();
                    int pixels = bitmap.getPixel((int)event.getX(), (int)event.getY());

                    red = Color.red(pixels);
                    blue = Color.blue(pixels);
                    green = Color.green(pixels);

                    displayColors.setBackgroundColor(Color.rgb(red,green,blue));
                    colorRGB = Color.rgb(red,green,blue);

                }
                return true;
            }
        });


        validationButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                EditText text = findViewById(R.id.categoryName);
                String colorHex = "#";
                colorHex += Integer.toHexString(red);
                colorHex += Integer.toHexString(green);
                colorHex += Integer.toHexString(blue);

                name = text.getText().toString();
                if(name.trim().isEmpty()){
                    Toast.makeText(getApplicationContext(),"Le champ nom ne peut pas être vide!",Toast.LENGTH_LONG).show();
                }
                else if (colorRGB == 0) {
                    Toast.makeText(getApplicationContext(),"Il faut choisir une couleur!",Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),"name: "+ name+", color: "+ colorHex,Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}