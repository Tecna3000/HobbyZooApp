package com.example.hobbyzooapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;


public class RegisterActivity extends AppCompatActivity {

    EditText emailEt, passwordEt, pseudoET;
    Button registerBtn;
    TextView haveAccountTv;

    // photo
    ImageView profileIv;

    ProgressDialog progressDialog;
    private FirebaseAuth auth;
    //photo
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private Uri imageUri;

    private static final int PICK_IMAGE = 1;
    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        registerBtn = findViewById(R.id.register_btn);
        haveAccountTv = findViewById(R.id.have_accountTv);
        pseudoET = findViewById(R.id.pseudoET);
        profileIv = findViewById(R.id.photoIV);//photo

        auth = FirebaseAuth.getInstance();

        //photo
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering User...");

        profileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, PICK_IMAGE);
            }
        });

        //
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEt.getText().toString().trim();
                String password = passwordEt.getText().toString().trim();
                String pseudo = pseudoET.getText().toString().trim();

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailEt.setError("Invalid email address");
                    emailEt.setFocusable(true);
                } else if (password.length() < 8) {
                    passwordEt.setError("password must have at least 8 characters");
                    passwordEt.setFocusable(true);
                } else {
                    // Ajout de la photo
                    if (imageUri == null) {
                        Toast.makeText(RegisterActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
                    } else {
                        registerUser(email, password, pseudo, imageUri);
                    }
                }
            }
        });

        haveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void registerUser(String email, String password, String pseudo, Uri imageUri) {
        progressDialog.show();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> emailVerificationTask) {
                                        progressDialog.dismiss();
                                        if (emailVerificationTask.isSuccessful()) {
                                            String email = user.getEmail();
                                            String uid = user.getUid();
                                            String pseudo = pseudoET.getText().toString().trim();

                                            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("users/" + uid + "/profile.jpg");

                                            storageRef.putFile(imageUri)
                                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                        @Override
                                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                @Override
                                                                public void onSuccess(Uri uri) {
                                                                    String imageUrl = uri.toString();
                                                                    HashMap<String, Object> userMap = new HashMap<>();
                                                                    userMap.put("email", email);
                                                                    userMap.put("uid", uid);
                                                                    userMap.put("pseudo", pseudo);
                                                                    userMap.put("image", imageUrl);
                                                                    userMap.put("emailVerified", false);

                                                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                                                                    reference.child(uid).setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                Toast.makeText(RegisterActivity.this, "Registered...\nA verification email has been sent to " + email, Toast.LENGTH_LONG).show();
                                                                                // Déconnecter l'utilisateur après l'enregistrement
                                                                                FirebaseAuth.getInstance().signOut();
                                                                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                                                                finish();
                                                                            } else {
                                                                                Toast.makeText(RegisterActivity.this, "Failed to register. Please try again.", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(RegisterActivity.this, "Failed to upload image. " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(RegisterActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    //photo
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileIv.setImageURI(imageUri);

            uploadImageToFirebase();
        }
    }

    private void uploadImageToFirebase() {
        progressDialog.setMessage("Uploading image...");
        progressDialog.show();

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            StorageReference profileRef = storageReference.child("users/" + user.getUid() + "/profile.jpg");
            profileRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    progressDialog.dismiss();
                                    String imageUrl = uri.toString();
                                    databaseReference.child("Users").child(user.getUid()).child("image").setValue(imageUrl);
                                    Toast.makeText(RegisterActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    //

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
