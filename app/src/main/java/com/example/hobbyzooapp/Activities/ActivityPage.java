package com.example.hobbyzooapp.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hobbyzooapp.HomeActivity;
import com.example.hobbyzooapp.OnSessionListRetrievedListener2;
import com.example.hobbyzooapp.R;
import com.example.hobbyzooapp.Sessions.NewSession;
import com.example.hobbyzooapp.Sessions.OnSessionListRetrievedListener;
import com.example.hobbyzooapp.Sessions.Session;
import com.example.hobbyzooapp.TodoTask;
import com.example.hobbyzooapp.Sessions.ListSessionsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ActivityPage extends AppCompatActivity {

    ImageView petPic, sessionLastPicture;
    TextView petName, goalsText, activityNameDisplay, sessionCommentDisplay;
    EditText editTextPetName, editTextActivityName, addToTodoListText;
    Button editNamePetButton, validatePetName, showMoreButton, showLessButton, addToTodoListButton, deleteActivityButton;
    ImageButton addSessionButton, homeButton;
    RecyclerView recyclerView, recyclerViewTodoList;
    List<String> items = new ArrayList<>();
    ListSessionsAdapter adapter;
    private List<TodoTask> todoList = new ArrayList<>();
    private List<Session> mySessions = new ArrayList<>();
    private List<String> lastSessionData = new ArrayList<>();
    Boolean allSessions = false;
    FirebaseAuth firebaseAuth;
    String activityId, activityName, activityPetName, activityPet, weeklyGoal, spentTime, feeling, category_id;
    LinearLayout header;
    FirebaseDatabase database;
    DatabaseReference referenceActivity;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page);
        Intent intent = getIntent();
        String activity_id = intent.getStringExtra("activity_id");
        initialisation();
        getActivityData(activity_id);

        getSessionList(activity_id, new OnSessionListRetrievedListener() {
            @Override
            public void onSessionListRetrieved(ArrayList<Session> sessionList) {
                mySessions = sessionList;
                adapter = new ListSessionsAdapter(sessionList);
                recyclerView.setAdapter(adapter);
                changeManager();

            }
        });

        deleteActivityButton = findViewById(R.id.deleteActivityButton);
        getLastSessionPicCom(activity_id, new OnSessionListRetrievedListener2() {
            @Override
            public void onSessionListRetrieved(ArrayList<String> sessionPicCom) {
                lastSessionData = sessionPicCom;
                sessionCommentDisplay.setText(lastSessionData.get(1));

                String image = lastSessionData.get(0);

                if (!image.equals("")) {
                    Glide.with(ActivityPage.this)
                            .load(image)
                            .into(sessionLastPicture);
                }
            } //todo a finir

        });



        FirebaseUser user = firebaseAuth.getCurrentUser();

        database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference("Tasks");
        String thisActivityId = activity_id;

        HashMap<String, String> tasks = new HashMap<>();
        TodoAdapter adapterTodoList = new TodoAdapter(todoList);


        recyclerViewTodoList.setAdapter(adapterTodoList);

        databaseReference.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                todoList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String activityId = snapshot.child("activityId").getValue(String.class);
                    String taskName  = snapshot.child("taskName").getValue(String.class);
                    String taskStatus = snapshot.child("taskStatus").getValue(String.class);
                    if (activityId.equals(thisActivityId)){
                        boolean boolTaskStatus = Boolean.parseBoolean(taskStatus);
                        todoList.add(new TodoTask(taskName, boolTaskStatus));
                    }
                }
                adapterTodoList.notifyDataSetChanged();
                if (todoList.size()>0 && todoList.size()<=5)
                    recyclerViewTodoList.setLayoutManager(new GridLayoutManager(ActivityPage.this, todoList.size(), GridLayoutManager.HORIZONTAL, false));
                else if (todoList.size()==0 )
                    recyclerViewTodoList.setLayoutManager(new GridLayoutManager(ActivityPage.this, 1, GridLayoutManager.HORIZONTAL, false));
                else
                    recyclerViewTodoList.setLayoutManager(new GridLayoutManager(ActivityPage.this, 5, GridLayoutManager.HORIZONTAL, false));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        addToTodoListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newElement = String.valueOf(addToTodoListText.getText());
                if(newElement.trim().isEmpty()){
                    Toast.makeText(ActivityPage.this, "Nothing written!", Toast.LENGTH_LONG).show();
                }else{
                    todoList.add(new TodoTask(newElement, Boolean.FALSE));
                    DatabaseReference newChildRef = databaseReference.push();
                    String taskId = newChildRef.getKey();
                    tasks.put("taskId", taskId);
                    tasks.put("taskName", newElement);
                    tasks.put("taskStatus", "FALSE");
                    tasks.put("activityId", thisActivityId);

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference reference = database.getReference("Tasks");
                    reference.child(taskId).setValue(tasks);
                    adapterTodoList.notifyDataSetChanged();
                }
                addToTodoListButton.setVisibility(View.VISIBLE);
                addToTodoListText.setText("");
            }
        });

        editTextPetName = findViewById(R.id.activityPagePetNameEdit);
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(10);
        editTextPetName.setFilters(filters);

        validatePetName = findViewById(R.id.activityPagecheckPetNameButton);
        //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.koa);
        /*
        int newWidth = (int) (bitmap.getWidth() * (70 / 100.0));
        int newHeight = (int) (bitmap.getHeight() * (70 / 100.0));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
        petPic.setScaleType(ImageView.ScaleType.CENTER_CROP);*/

       // petPic.setImageBitmap(bitmap);

        editNamePetButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                editTextPetName.setText(petName.getText());
                editTextActivityName.setText(activityNameDisplay.getText());
                editTextPetName.setVisibility(View.VISIBLE);
                editTextActivityName.setVisibility(View.VISIBLE);
                editNamePetButton.setVisibility(View.GONE);
                validatePetName.setVisibility(View.VISIBLE);
                petName.setVisibility(View.GONE);
                activityNameDisplay.setVisibility(View.GONE);
                deleteActivityButton.setVisibility(View.VISIBLE);
            }
        });

        addSessionButton = findViewById(R.id.add_session_button);
        addSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityPage.this, NewSession.class));
            }
        });

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMainActivity();
            }
        });

        deleteActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference databaseReferenceTasks = database.getReference().child("Tasks");
                databaseReferenceTasks.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String activity_id_task = snapshot.child("activity_id").getValue(String.class);
                            String task_id = snapshot.child("task_id").getValue(String.class);
                            if(activity_id_task.equals(activityId)){
                                databaseReferenceTasks.child(task_id).removeValue();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Gérez l'erreur en cas d'annulation de la requête
                    }
                });
                DatabaseReference databaseReferenceSession = database.getReference().child("Session");
                databaseReferenceSession.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String activity_id_session = snapshot.child("activity_id").getValue(String.class);
                            String session_id = snapshot.child("session_id").getValue(String.class);
                            if(activity_id_session.equals(activityId)){
                                databaseReferenceSession.child(session_id).removeValue();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                referenceActivity.child(activityId).removeValue();
                finish();
            }
        });

        showMoreButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                allSessions =true;
                changeManager();
                adapter.setExpanded(!adapter.isExpanded());
                adapter.notifyDataSetChanged();
                showLessButton.setVisibility(View.VISIBLE);
                showMoreButton.setVisibility(View.GONE);

            }
        });

        showLessButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                allSessions=false;
                changeManager();
                adapter.setExpanded(!adapter.isExpanded());
                adapter.notifyDataSetChanged();
                showMoreButton.setVisibility(View.VISIBLE);
                showLessButton.setVisibility(View.GONE);
            }
        });

        validatePetName.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                editTextPetName.setVisibility(View.GONE);
                editTextActivityName.setVisibility(View.GONE);
                String newPetName = String.valueOf(editTextPetName.getText());;
                String newActivityName = String.valueOf(editTextActivityName.getText());;
                petName.setText(newPetName);
                activityNameDisplay.setText(newActivityName);
                editNamePetButton.setVisibility(View.VISIBLE);
                validatePetName.setVisibility(View.GONE);
                petName.setVisibility(View.VISIBLE);
                activityNameDisplay.setVisibility(View.VISIBLE);
                deleteActivityButton.setVisibility(View.GONE);

                DatabaseReference activitiesRef = FirebaseDatabase.getInstance().getReference("Activity");
                DatabaseReference activityRef = activitiesRef.child(activity_id);
                activityRef.child("activity_pet_name").setValue(newPetName, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            System.out.println("Activité modifiée avec succès !");
                        } else {
                            System.err.println("Erreur lors de la modification de l'activité : " + databaseError.getMessage());
                        }
                    }
                });

                activityRef.child("activity_name").setValue(newActivityName, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            System.out.println("Activité modifiée avec succès !");
                        } else {
                            System.err.println("Erreur lors de la modification de l'activité : " + databaseError.getMessage());
                        }
                    }
                });
            }
        });

    }

    public void getActivityData(String activity_id){
        database = FirebaseDatabase.getInstance();
        referenceActivity = database.getReference().child("Activity");

        referenceActivity.child(activity_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Récupérez les informations de l'activité
                    activityId = activity_id;
                    activityName = dataSnapshot.child("activity_name").getValue(String.class).replace(",", " ");
                    activityPetName = dataSnapshot.child("activity_pet_name").getValue(String.class).replace(",", " ");
                    activityPet = dataSnapshot.child("activity_pet").getValue(String.class);
                    weeklyGoal = dataSnapshot.child("weekly_goal").getValue(String.class);
                    spentTime = dataSnapshot.child("spent_time").getValue(String.class);
                    category_id = dataSnapshot.child("category_id").getValue(String.class);
                    int feelingPointer = Integer.parseInt(Objects.requireNonNull(dataSnapshot.child("feeling").getValue(String.class)));
                    feeling = HomeActivity.animalsFeeling.get(feelingPointer);
                    petName.setText(activityPetName);
                    String resourceName;
                    if(feelingPointer == 0)
                        resourceName = "none_icon_gone";
                    else
                        resourceName = activityPet + "_icon_" + feeling;
                    int resId = ActivityPage.this.getResources().getIdentifier(resourceName,"drawable",ActivityPage.this.getPackageName());
                    petPic.setImageResource(resId);
                    activityNameDisplay.setText(activityName);
                    int weeklyHours = Integer.parseInt(weeklyGoal) / 60;
                    int weeklyMinutes = Integer.parseInt(weeklyGoal) % 60;


                    int weeklySpentHours = Integer.parseInt(spentTime) / 60;
                    int weeklySpentMinutes = Integer.parseInt(spentTime) % 60;
                    goalsText.setText(new Time(weeklySpentHours,weeklySpentMinutes,0)+
                            "/"+
                            new Time(weeklyHours,weeklyMinutes,0));


                    DatabaseReference referenceCategory = database.getReference("Category");

                    referenceCategory.child(category_id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String category_color = dataSnapshot.child("category_color").getValue(String.class);
                                int color = Color.parseColor(category_color);
                                header.setBackgroundColor(color);


                            } else {
                                // L'activité n'existe pas dans la base de données
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Gérez les erreurs de la récupération des données
                        }
                    });

                } else {
                    // L'activité n'existe pas dans la base de données
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Gérez les erreurs de la récupération des données
            }
        });

    }

    private void initialisation(){
        firebaseAuth = FirebaseAuth.getInstance();
        petPic =findViewById(R.id.activityPagePetPic);
        petName = findViewById(R.id.activityPagePetName);
        activityNameDisplay =findViewById(R.id.activityPageActivityName);
        editTextActivityName=findViewById(R.id.activityPageActivityNameEdit);
        header = findViewById(R.id.headerLayout);
        goalsText = findViewById(R.id.activityPageGoalsText);
        addToTodoListText = findViewById(R.id.addToTodoListText);
        recyclerView = findViewById(R.id.activityPageRecyclerView);
        recyclerViewTodoList = findViewById(R.id.todoRecyclerView);
        sessionCommentDisplay = findViewById(R.id.activityPageCommentText);
        sessionLastPicture = findViewById(R.id.activityPagePicture);
        petPic.setImageResource(R.drawable.koala_icon);
        showMoreButton = findViewById(R.id.activityPageShowMoreButton);
        showLessButton = findViewById(R.id.activityPageShowLessButton);
        homeButton = findViewById(R.id.homeButton);
        addToTodoListButton = findViewById(R.id.addToTodoListButton);
        editNamePetButton = findViewById(R.id.activityPageEditPetNameButton);
    }

    private ArrayList<Session> getSessionList(String activity_id, OnSessionListRetrievedListener listener){
        DatabaseReference reference = database.getReference("Session");
        DatabaseReference activityReference = database.getReference("Activity");

        ArrayList<Session> mySessions = new ArrayList<>();



        activityReference.child(activity_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String activity_name = dataSnapshot.child("activity_name").getValue(String.class);
                    reference.orderByChild("activity_id").equalTo(activity_id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String session_id = snapshot.child("session_id").getValue(String.class);
                                String session_duration = snapshot.child("session_duration").getValue(String.class);
                                String activity_id = snapshot.child("activity_id").getValue(String.class);
                                String session_day = snapshot.child("session_day").getValue(String.class);
                                String session_month = snapshot.child("session_month").getValue(String.class);
                                String session_year = snapshot.child("session_year").getValue(String.class);
                                String session_image = snapshot.child("session_picture").getValue(String.class);
                                String mnemonic = dataSnapshot.child("activity_pet").getValue(String.class);


                                int hourDuration = Integer.parseInt(session_duration)/60;
                                int minutesDuration = Integer.parseInt(session_duration)%60;

                                mySessions.add(new Session(session_id,activity_id,activity_name,
                                        new Time(hourDuration,minutesDuration,0),
                                        Integer.parseInt(session_day),
                                        Integer.parseInt(session_month),
                                        Integer.parseInt(session_year),
                                        session_image,
                                        mnemonic
                                ));
                            }
                            listener.onSessionListRetrieved(mySessions);
                            if (mySessions.size()>=3) showMoreButton.setVisibility(View.VISIBLE);
                            else showMoreButton.setVisibility(View.GONE);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w("TAG", "Erreur lors de la récupération des données", databaseError.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        return  mySessions;
    }

    private ArrayList<String> getLastSessionPicCom(String activity_id, OnSessionListRetrievedListener2 listener){
        DatabaseReference reference = database.getReference("Session");
        ArrayList<String> lastSessionData = new ArrayList<>();
        lastSessionData.add("");
        lastSessionData.add("No previous session");
        reference.orderByChild("activity_id").equalTo(activity_id).addListenerForSingleValueEvent(new ValueEventListener() {
            double lastDate = 0;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String session_day = snapshot.child("session_day").getValue(String.class);
                    String session_month = snapshot.child("session_month").getValue(String.class);
                    String session_year = snapshot.child("session_year").getValue(String.class);
                    String session_time = snapshot.child("session_time").getValue(String.class);
                    double date = Double.parseDouble(session_year + session_month + session_day + session_time);

                    String session_done = snapshot.child("session_done").getValue(String.class);
                    String session_picture = snapshot.child("session_picture").getValue(String.class);
                    String session_comment = snapshot.child("session_comment").getValue(String.class);

                    if (lastDate < date && session_done.equals("TRUE")) {
                        lastSessionData.set(0, session_picture);
                        lastSessionData.set(1, session_comment);
                        lastDate = date;
                    }
                }
                listener.onSessionListRetrieved(lastSessionData);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("TAG", "Erreur lors de la récupération des données", databaseError.toException());
            }
        });
        return  lastSessionData;
    }


    private void updateDBTasks(String statue, String taskName){
        DatabaseReference tasksRef = FirebaseDatabase.getInstance().getReference("Tasks");
        DatabaseReference taskRef = tasksRef.child(taskName);
        System.out.println(taskName);
        taskRef.child("taskStatus").setValue(statue, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    System.out.println("Tasks modified ! ");
                } else {
                    System.err.println("Error : " + databaseError.getMessage());
                }
            }
        });

    }
    private GridLayoutManager  changeManagerToDoList() {
        GridLayoutManager layoutManager;
        if (todoList.size()<=5 && todoList.size()>0 )
            layoutManager = new GridLayoutManager(this, todoList.size(), GridLayoutManager.HORIZONTAL, false);
        else
            layoutManager = new GridLayoutManager(this, 5, GridLayoutManager.HORIZONTAL, false);

        return  layoutManager;
    }

    private void changeManager() {
        GridLayoutManager layoutManager;
         if (allSessions)
             if (mySessions.size()<=5)
                 layoutManager = new GridLayoutManager(this, mySessions.size() , GridLayoutManager.HORIZONTAL, false);
             else
                layoutManager = new GridLayoutManager(this, 5, GridLayoutManager.HORIZONTAL, false);
        else
         if (mySessions.size()<=5 && mySessions.size()>0)
             layoutManager = new GridLayoutManager(this, mySessions.size(), GridLayoutManager.HORIZONTAL, false);
        else if (mySessions.size()==0)
             layoutManager = new GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false);
         else
             layoutManager = new GridLayoutManager(this, 3, GridLayoutManager.HORIZONTAL, false);

        recyclerView.setLayoutManager(layoutManager);
    }

    public void openMainActivity(){
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

}