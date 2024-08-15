package com.example.letschat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.app.ProgressDialog;
import android.widget.Toast;
import android.text.TextUtils;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class login extends AppCompatActivity {

    Button buttons;
    EditText email, password;
    FirebaseAuth auth;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Initialize ProgressDialog
        progressDialog = new ProgressDialog(login.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        Button button = findViewById(R.id.rebutton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(login.this, Signup.class);
                startActivity(intent);
            }
        });

        auth = FirebaseAuth.getInstance();
        buttons = findViewById(R.id.logbutton);
        email = findViewById(R.id.editTexLogEmail);
        password = findViewById(R.id.editTextLogPassword);

        buttons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Email = email.getText().toString();
                String pass = password.getText().toString();

                if (TextUtils.isEmpty(Email)) {
                    progressDialog.dismiss();
                    Toast.makeText(login.this, "Enter The Email", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(pass)) {
                    progressDialog.dismiss();
                    Toast.makeText(login.this, "Enter The Password", Toast.LENGTH_SHORT).show();
                } else if (!Email.matches(emailPattern)) {
                    progressDialog.dismiss();
                    email.setError("Give Proper Email Address");
                } else if (password.length() < 6) {
                    progressDialog.dismiss();
                    password.setError("More Than Six Characters");
                    Toast.makeText(login.this, "Password Needs To Be Longer Than Six Characters", Toast.LENGTH_SHORT).show();
                } else {
                    // Show the progress dialog before making the request
                    progressDialog.show();

                    auth.signInWithEmailAndPassword(Email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // Dismiss the progress dialog after the task is complete
                            progressDialog.dismiss();

                            if (task.isSuccessful()) {
                                try {
                                    Intent intent = new Intent(login.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } catch (Exception e) {
                                    Toast.makeText(login.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(login.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
