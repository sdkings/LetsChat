package com.example.letschat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class Signup extends AppCompatActivity {

    EditText rg_username, rg_email, rg_password;
    Button rg_signup;
    CircleImageView rg_profileImg;
    FirebaseAuth auth;
    Uri imageURI;
    String imageuri;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog progressDialog;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Views and Firebase instances
        rg_username = findViewById(R.id.editTexLog);
        rg_email = findViewById(R.id.editTexLogEmail);
        rg_password = findViewById(R.id.editTextLogPassword);
        rg_profileImg = findViewById(R.id.profilerg0);
        rg_signup = findViewById(R.id.regbutton);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Establishing The Account");
        progressDialog.setCancelable(false);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        rg_signup.setOnClickListener(v -> registerUser());

        rg_profileImg.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
        });
    }

    private void registerUser() {
        String namee = rg_username.getText().toString();
        String emaill = rg_email.getText().toString();
        String password = rg_password.getText().toString();
        String status = "Hey I'm Using This Application";

        if (TextUtils.isEmpty(namee) || TextUtils.isEmpty(emaill) || TextUtils.isEmpty(password)) {
            Toast.makeText(Signup.this, "Please Enter Valid Information", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!emaill.matches(emailPattern)) {
            rg_email.setError("Type A Valid Email Here");
            return;
        }

        if (password.length() < 6) {
            rg_password.setError("Password Must Be 6 Characters Or More");
            return;
        }

        progressDialog.show();

        auth.createUserWithEmailAndPassword(emaill, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String id = task.getResult().getUser().getUid();
                DatabaseReference reference = database.getReference().child("user").child(id);
                StorageReference storageReference = storage.getReference().child("Upload").child(id);

                if (imageURI != null) {
                    storageReference.putFile(imageURI).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                imageuri = uri.toString();
                                saveUserToDatabase(reference, id, namee, emaill, password, imageuri, status);
                            });
                        } else {
                            showToastAndHideProgress("Error uploading image");
                        }
                    });
                } else {
                    imageuri = "https://firebasestorage.googleapis.com/v0/b/letschat-b1051.appspot.com/o/20DoQ.png?alt=media&token=61bc71c2-e3ef-4482-88a5-b2c9f7affdea";
                    saveUserToDatabase(reference, id, namee, emaill, password, imageuri, status);
                }
            } else {
                showToastAndHideProgress(task.getException().getMessage());
            }
        });
    }

    private void saveUserToDatabase(DatabaseReference reference, String id, String namee, String emaill, String password, String imageuri, String status) {
        List<String> friendsEmails = new ArrayList<>();  // Initialize an empty list of friends
        users user = new users(id, namee, emaill, password, imageuri, status, friendsEmails);
        reference.setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                progressDialog.dismiss();
                Intent intent = new Intent(Signup.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                showToastAndHideProgress("Error in creating the user");
            }
        });
    }

    private void showToastAndHideProgress(String message) {
        progressDialog.dismiss();
        Toast.makeText(Signup.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            imageURI = data.getData();
            rg_profileImg.setImageURI(imageURI);
        }
    }
}
