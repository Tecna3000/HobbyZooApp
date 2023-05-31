package com.example.hobbyzooapp.AccountManagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.hobbyzooapp.Activities.MyActivities;
import com.example.hobbyzooapp.CalendarEvolution.MyEvolutionActivity;
import com.example.hobbyzooapp.HomeActivity;
import com.example.hobbyzooapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ProfileActivity extends AppCompatActivity {
    private TextView usernameTextView, editProfileTextView;
    private ImageView profileImageView, addPhoto;
    private Button personalInfoButton, myActivitiesButton, followMyProgressButton, settingsButton, validate;
    private ImageButton backButton;
    private EditText usernameEdit;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private String userId;
    private static final int PICK_IMAGE = 1;
    private static final int EDIT_PROFILE_REQUEST_CODE = 1;
    private static final int REQUEST_PERSONAL_INFORMATION = 1;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        firebaseAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        FirebaseUser user = firebaseAuth.getCurrentUser();

        usernameTextView = findViewById(R.id.profile_username);
        profileImageView = findViewById(R.id.profile_image);
        editProfileTextView = findViewById(R.id.edit_profile);
        personalInfoButton = findViewById(R.id.account);
        myActivitiesButton = findViewById(R.id.my_activities);
        followMyProgressButton = findViewById(R.id.follow_my_progress);
        backButton = findViewById(R.id.backButton);
        settingsButton = findViewById(R.id.settings_button);
        usernameEdit = findViewById(R.id.username_edit);
        addPhoto = findViewById(R.id.add_photo);
        validate = findViewById(R.id.validateButton);
        followMyProgressButton= findViewById(R.id.follow_my_progress);

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        userId = user.getUid();

        if (user != null) {
            DatabaseReference reference = databaseReference.child(userId);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String pseudo = snapshot.child("pseudo").getValue(String.class);
                        if (pseudo != null) {
                            usernameTextView.setText(pseudo);
                            usernameEdit.setText(pseudo); // Afficher le pseudo dans le champ d'édition
                        }

                        String image = snapshot.child("image").getValue(String.class);
                        if (image != null) {
                            Glide.with(ProfileActivity.this)
                                    .load(image)
                                    .into(profileImageView);
                        } else {
                            Glide.with(ProfileActivity.this)
                                    .load(R.drawable.ic_animal)
                                    .into(profileImageView);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle data reading errors
                }
            });
        }

        myActivitiesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, MyActivities.class));
            }
        });
        personalInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(ProfileActivity.this, AccountActivity.class), 1);
            }
        });

        followMyProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, MyEvolutionActivity.class));
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
                finish();
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, SettingsActivity.class));
            }
        });

        editProfileTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usernameEdit.setVisibility(View.VISIBLE);
                addPhoto.setVisibility(View.VISIBLE);
                validate.setVisibility(View.VISIBLE);
                usernameTextView.setVisibility(View.GONE);
                editProfileTextView.setVisibility(View.GONE);

                // Remplir le champ d'édition avec le nom d'utilisateur actuel
                String currentUsername = usernameTextView.getText().toString().trim();
                usernameEdit.setText(currentUsername);

                // Supprimer l'URI de l'image sélectionnée pour conserver l'ancienne image
                imageUri = null;
            }
        });

        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, PICK_IMAGE);
            }
        });

        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newUsername = usernameEdit.getText().toString().trim();

                // Vérifier si le nom d'utilisateur a été modifié
                boolean isUsernameChanged = !newUsername.isEmpty() && !newUsername.equals(usernameTextView.getText().toString().trim());

                if (isUsernameChanged && imageUri == null) {
                    // Si seul le nom d'utilisateur a été modifié, mais aucune nouvelle image n'a été sélectionnée
                    updateUserProfile(newUsername, null);
                } else if (!isUsernameChanged && imageUri != null) {
                    // Si seule la nouvelle image a été sélectionnée, mais le nom d'utilisateur n'a pas été modifié
                    updateUserProfile(null, imageUri);
                } else if (isUsernameChanged && imageUri != null) {
                    // Si à la fois le nom d'utilisateur et la nouvelle image ont été modifiés
                    updateUserProfile(newUsername, imageUri);
                } else {
                    // Si ni le nom d'utilisateur ni la nouvelle image n'ont été modifiés
                    // Cacher les éléments d'édition du profil sans effectuer de mise à jour
                    usernameEdit.setVisibility(View.GONE);
                    addPhoto.setVisibility(View.GONE);
                    validate.setVisibility(View.GONE);
                    usernameTextView.setVisibility(View.VISIBLE);
                    editProfileTextView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void updateUserProfile(String newUsername, Uri imageUri) {
        DatabaseReference userRef = databaseReference.child(userId);

        // Mettre à jour le nom d'utilisateur si une nouvelle valeur est fournie
        if (newUsername != null && !newUsername.isEmpty()) {
            userRef.child("pseudo").setValue(newUsername);
            usernameTextView.setText(newUsername); // Mettre à jour le TextView usernameTextView immédiatement
        }

        // Mettre à jour l'image de profil si une nouvelle URI est fournie
        if (imageUri != null) {
            // Enregistrer la nouvelle image de profil dans Firebase Storage
            String imageFileName = userId + ".jpg";
            StorageReference imageRef = storageReference.child("profile_images").child(imageFileName);

            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Uploading image...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Obtenir l'URL de téléchargement de l'image
                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // Mettre à jour l'URL de l'image de profil
                                    userRef.child("image").setValue(uri.toString());

                                    progressDialog.dismiss();
                                    Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

                                    // Recharger l'image de profil mise à jour
                                    Glide.with(ProfileActivity.this)
                                            .load(uri)
                                            .into(profileImageView);

                                    // Cacher les éléments d'édition du profil après validation
                                    usernameEdit.setVisibility(View.GONE);
                                    addPhoto.setVisibility(View.GONE);
                                    validate.setVisibility(View.GONE);
                                    usernameTextView.setVisibility(View.VISIBLE);
                                    editProfileTextView.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            usernameEdit.setVisibility(View.GONE);
            addPhoto.setVisibility(View.GONE);
            validate.setVisibility(View.GONE);
            usernameTextView.setVisibility(View.VISIBLE);
            editProfileTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PERSONAL_INFORMATION && resultCode == RESULT_OK && data != null) {
            String newUsername = data.getStringExtra("newUsername");
            String newImageUri = data.getStringExtra("newImageUri");

            // Mettre à jour votre interface utilisateur avec les données modifiées
            if (newUsername != null) {
                // Mettre à jour le nom d'utilisateur affiché
                usernameTextView.setText(newUsername);
            }

            if (newImageUri != null) {
                // Mettre à jour l'image de profil
                Uri imageUri = Uri.parse(newImageUri);
                profileImageView.setImageURI(imageUri);
            }
        }
    }

    private void launchPersonalInformationActivity() {
        Intent intent = new Intent(ProfileActivity.this, AccountActivity.class);
        startActivityForResult(intent, REQUEST_PERSONAL_INFORMATION);
    }
}