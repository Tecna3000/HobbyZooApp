package com.example.hobbyzooapp.Sessions;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hobbyzooapp.Activities.ActivitiesCallBack;
import com.example.hobbyzooapp.Activities.Activity;
import com.example.hobbyzooapp.Activities.ExpandableListAdapter;
import com.example.hobbyzooapp.Activities.MyActivities;
import com.example.hobbyzooapp.Calendar.CalendarActivity;
import com.example.hobbyzooapp.Calendar.CalendarUtils;
import com.example.hobbyzooapp.Category.Category;
import com.example.hobbyzooapp.HomeActivity;
import com.example.hobbyzooapp.R;
import com.example.hobbyzooapp.OnItemClickListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class MyDailySessions extends AppCompatActivity {

    private ImageButton homeButton;
    private Button addSessionButton;
    private ImageButton calendarButton;
    private View sessionButton;
    FirebaseAuth firebaseAuth;
    public static LocalDate localDate = null;
    MyDailySessionsAdapter adapter;




    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_daily_sessions);
        firebaseAuth = FirebaseAuth.getInstance();

        homeButton = findViewById(R.id.home_button);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMainActivity();
                finish();
            }
        });

        addSessionButton = findViewById(R.id.add_session_button);
        addSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyDailySessions.this, NewSession.class));
            }
        });

        calendarButton = findViewById(R.id.calendar_button);
        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCalendar();
            }
        });

        sessionButton = findViewById(R.id.session);
        //sessionList.add(new Session("Dessin", new Time(0, 1, 10), 10, 10, 2023));
        //sessionList.add(new Session("Bougie", new Time(0, 2, 0), 15, 5, 2023));


        GridView sessionListView = findViewById(R.id.session_list_view);
        //LocalDate localDate = CalendarUtils.selectedDate;
        ArrayList<Session> sessions;

        SessionsCallback callback = new SessionsCallback() {
            @Override
            public void onSessionsLoaded(ArrayList<Session> mySessions) {
                adapter = new MyDailySessionsAdapter(MyDailySessions.this, mySessions, localDate);
                adapter.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        new AlertDialog.Builder(MyDailySessions.this)
                                .setTitle("Vous avez selectionné une session de " + adapter.getItem(position).getName() + " de " + adapter.getItem(position).getTime())
                                .setMessage("Voulez-vous commencer cette session ?")
                                .setNegativeButton(android.R.string.no, null)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        openRunSession();
                                    }
                                }).create().show();
                    }
                });
                sessionListView.setAdapter(adapter);
            }
        };
        sessions= getSessions(callback);



        TextView dateSession = findViewById(R.id.dateSession);
        String date = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            date = localDate.getDayOfMonth() + "/" + localDate.getMonth().getValue() + "/" + localDate.getYear();
        }
        dateSession.setText(date);




    }


    public void openMainActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);

    }

    public void openCalendar() {
        Intent intent = new Intent(this, CalendarActivity.class);
        startActivity(intent);
    }

    public void openRunSession() {
        Intent intent = new Intent(this, RunSession.class);
        startActivity(intent);

    }

 private ArrayList<Session> getSessions(SessionsCallback callback) {
     FirebaseDatabase database = FirebaseDatabase.getInstance();
     DatabaseReference reference = database.getReference("Session");
     FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
     FirebaseUser user = firebaseAuth.getCurrentUser();
     String uid = user.getUid();
     ArrayList<Session> mySessions = new ArrayList<>();
     reference.addListenerForSingleValueEvent(new ValueEventListener() {
         @Override
         public void onDataChange(DataSnapshot dataSnapshot) {
             for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                 String session_id = snapshot.child("session_id").getValue(String.class);
                 String session_duration = snapshot.child("session_duration").getValue(String.class);
                 String activity_id = snapshot.child("activity_id").getValue(String.class);

                 DatabaseReference referenceActivity = database.getReference("Activity");

                 referenceActivity.child(activity_id).addListenerForSingleValueEvent(new ValueEventListener() {
                     @Override
                     public void onDataChange(DataSnapshot dataSnapshot) {
                         if (dataSnapshot.exists()) {
                             String activityName = dataSnapshot.child("activity_name").getValue(String.class);
                             int hourDuration = Integer.parseInt(session_duration)/60;
                             int minutesDuration = Integer.parseInt(session_duration)%60;

                             mySessions.add(new Session(session_id,activity_id,activityName,new Time(hourDuration,minutesDuration,0),22,5,2023));
                             callback.onSessionsLoaded(mySessions);
                         } else {
                             // L'activité n'existe pas dans la base de données
                         }
                     }
                     @Override
                     public void onCancelled(DatabaseError databaseError) {
                         // Une erreur s'est produite lors de la récupération des données
                     }
                 });

             }
         }

         @Override
         public void onCancelled(DatabaseError databaseError) {
             Log.w("TAG", "Erreur lors de la récupération des données", databaseError.toException());
         }
     });
     return mySessions;
 }



}