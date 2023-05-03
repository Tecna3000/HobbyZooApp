package com.example.hobbyzooapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class endSession extends AppCompatActivity {



    private static final int RETOUR_PRENDRE_PHOTO =1;
    ImageView petPic;
    Button takeApic;
    ImageView takenImage;

    Button validateButton;
    EditText commentField;
    private String photoPath =null;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_session);
        petPic = findViewById(R.id.petPicture);
        petPic.setImageResource(R.drawable.koala);
        takeApic=findViewById(R.id.takeAPic);
        takenImage =findViewById(R.id.takenImage);
        commentField =findViewById(R.id.commentText);
        validateButton = findViewById(R.id.validateButton);

        takeApic.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    takePicture();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        validateButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String comment = String.valueOf(commentField.getText());
                Toast.makeText(endSession.this, comment, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(endSession.this, endSessioPart2.class);
                startActivity(intent);
            }
        });

    }


    private void takePicture() throws IOException {
        Intent intent=new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager())!=null){
            String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File photoDir =getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File photoFile= File.createTempFile("Session of "+date,".jpg",photoDir);
            photoPath =photoFile.getAbsolutePath();
                Uri photoUri = FileProvider.getUriForFile(endSession.this,
                        endSession.this.getApplicationContext().getOpPackageName()+".provider",
                        photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
                startActivityForResult(intent, RETOUR_PRENDRE_PHOTO);

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap image = BitmapFactory.decodeFile(photoPath);
        takenImage.setImageBitmap(image);
        takenImage.setVisibility(View.VISIBLE);
        takeApic.setWidth(90);
        takeApic.setHeight(60);
        petPic.setImageResource(R.drawable.koa);




    }
}