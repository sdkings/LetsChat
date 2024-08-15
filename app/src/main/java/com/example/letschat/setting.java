package com.example.letschat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class setting extends AppCompatActivity {
    ImageView setprofile;
    EditText setname, setstatus;
    Button donebut;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri setImageUri;
    String email, password;
    ProgressDialog progressDialog;
    List<String> friendsEmails; // Add friendsEmails list

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);


        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        // Initialize UI components
        setprofile = findViewById(R.id.settingprofile);
        setname = findViewById(R.id.settingname);
        setstatus = findViewById(R.id.settingstatus);
        donebut = findViewById(R.id.donebutt);

        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving...");
        progressDialog.setCancelable(false);

        // Reference to current user's data in Firebase
        DatabaseReference reference = database.getReference().child("user").child(auth.getUid());
        StorageReference storageReference = storage.getReference().child("upload").child(auth.getUid());

        // Load user data from Firebase
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                email = snapshot.child("mail").getValue(String.class);
                password = snapshot.child("password").getValue(String.class);
                String name = snapshot.child("userName").getValue(String.class);
                String profile = snapshot.child("profilepic").getValue(String.class);
                String status = snapshot.child("status").getValue(String.class);

                // Initialize friendsEmails list
                friendsEmails = new ArrayList<>();
                for (DataSnapshot emailSnapshot : snapshot.child("friendsEmails").getChildren()) {
                    friendsEmails.add(emailSnapshot.getValue(String.class));
                }

                // Set data to UI components
                setname.setText(name);
                setstatus.setText(status);
                Picasso.get().load(profile).into(setprofile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(setting.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
            }
        });

        // Set profile image onClick listener
        setprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
            }
        });

        // Save data on done button click
        donebut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();

                String name = setname.getText().toString();
                String status = setstatus.getText().toString();

                if (setImageUri != null) {
                    // If a new image is selected, upload it and update the user's data
                    storageReference.putFile(setImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String finalImageUri = uri.toString();
                                        updateUser(reference, name, status, finalImageUri);
                                    }
                                });
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(setting.this, "Failed to upload image.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    // If no new image is selected, keep the old one
                    updateUser(reference, name, status, null);
                }
            }
        });
    }

    // Handle image selection result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            setImageUri = data.getData();
            setprofile.setImageURI(setImageUri);
        }
    }

    // Update user data in Firebase
    private void updateUser(DatabaseReference reference, String name, String status, String imageUri) {
        // If imageUri is null, retain the current image URI
        if (imageUri == null) {
            reference.child("profilepic").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        String currentImageUri = task.getResult().getValue(String.class);
                        users user = new users(auth.getUid(), name, email, password, currentImageUri, status, friendsEmails);
                        saveUser(reference, user);
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(setting.this, "Failed to retrieve profile picture.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            users user = new users(auth.getUid(), name, email, password, imageUri, status, friendsEmails);
            saveUser(reference, user);
        }
    }

    // Save user data to Firebase
    private void saveUser(DatabaseReference reference, users user) {
        reference.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(setting.this, "Data saved successfully.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(setting.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(setting.this, "Failed to save data.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
