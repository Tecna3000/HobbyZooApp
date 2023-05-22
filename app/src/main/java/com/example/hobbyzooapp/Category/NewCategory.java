package com.example.hobbyzooapp.Category;

import android.annotation.SuppressLint;
import android.content.Intent;
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

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hobbyzooapp.Activities.NewActivity;
import com.example.hobbyzooapp.HomeActivity;
import com.example.hobbyzooapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;


public class NewCategory extends AppCompatActivity {

    String name, user_id, colorHex;
    int colorRGB = 0;
    int red, blue, green;
    CircleImageView colorPicker;
    View displayColors;
    Bitmap bitmap;
    FirebaseAuth firebaseAuth;
    ImageView validationButton, returnButton;
    String regexPattern = "^[a-zA-Z0-9 ]+$";
    Pattern pattern;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_category);
        initialisation();

        colorPicker.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE){
                    float x = event.getX();
                    float y = event.getY();

                    float centerX = colorPicker.getWidth() / 2f;
                    float centerY = colorPicker.getHeight() / 2f;
                    float radius = colorPicker.getWidth() / 2f;

                    if (Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2) <= Math.pow(radius, 2)) {
                        colorPicker.setDrawingCacheEnabled(true);
                        colorPicker.buildDrawingCache(true);
                        bitmap = colorPicker.getDrawingCache();

                        int pixels = bitmap.getPixel((int) x, (int) y);

                        red = Color.red(pixels);
                        blue = Color.blue(pixels);
                        green = Color.green(pixels);

                        displayColors.setBackgroundColor(Color.rgb(red, green, blue));
                        colorRGB = Color.rgb(red, green, blue);
                        colorHex = "#" + Integer.toHexString(red) + Integer.toHexString(green) + Integer.toHexString(blue);
                    }
                }

                return true;
            }
        });

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        validationButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                EditText text = findViewById(R.id.categoryName);
                name = text.getText().toString();
                Matcher matcherCategoryName = pattern.matcher(name);
                if(name.trim().isEmpty() || colorRGB == 0){
                    Toast.makeText(getApplicationContext(),"Field can't be empty!!",Toast.LENGTH_LONG).show();
                }
                else if(!matcherCategoryName.matches()){
                    Toast.makeText(getApplicationContext(),"Name fields can't have special characters!",Toast.LENGTH_LONG).show();
                }

                else {
                    List<String> categories = new ArrayList<>();
                    DatabaseReference databaseReferenceChild = FirebaseDatabase.getInstance().getReference().child("Category");
                    databaseReferenceChild.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String userId = snapshot.child("user_id").getValue(String.class);
                                String activityName = snapshot.child("category_name").getValue(String.class);
                                if(userId.equals(user_id) && activityName.equals(name))
                                    categories.add(name);
                            }
                            if(categories.size() == 0){
                                addDBCategory();
                                setIntentCategory();
                                finish();
                            }
                            else{
                                Toast.makeText(getApplicationContext(),"This Category already exists!",Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Gérez l'erreur en cas d'annulation de la requête
                        }
                    });
                }
            }
        });

    }

    private void setIntentCategory() {
        Intent intent = new Intent().setClass(getApplicationContext(), NewActivity.class);
        Intent intentActivity = getIntent();
        intent.putExtra("activity_name", intentActivity.getStringExtra("activity_name"));
        intent.putExtra("animal_name", intentActivity.getStringExtra("animal_name"));
        intent.putExtra("pos_animal", intentActivity.getStringExtra("pos_animal"));
        intent.putExtra("category_name", intentActivity.getStringExtra(name));
        startActivity(intent);
    }


    private void initialisation() {
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        user_id = user.getUid();
        validationButton = findViewById(R.id.validationButton);
        returnButton = findViewById(R.id.returnButton);
        colorPicker = findViewById(R.id.colorPickers);
        displayColors = findViewById(R.id.displayColors);
        colorPicker.setDrawingCacheEnabled(true);
        colorPicker.buildDrawingCache(true);
        pattern = Pattern.compile(regexPattern);
    }

    private void addDBCategory(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference newChildRef = databaseReference.push();
        String category_id = newChildRef.getKey();
        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("category_id", category_id);
        hashMap.put("category_name", name.replace(" ", ","));
        hashMap.put("category_color", colorHex);
        hashMap.put("user_id", user_id);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Category");
        reference.child(category_id).setValue(hashMap);
    }
}