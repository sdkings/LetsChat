package com.example.letschat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class search extends AppCompatActivity {

    private EditText searchEmailEditText;
    private Button searchButton, sendRequestButton;
    private RelativeLayout userInfoLayout;
    private FirebaseAuth auth;
    private FirebaseDatabase database;

    private TextView username, userstatus;
    private de.hdodenhof.circleimageview.CircleImageView userimg;

    private String foundUserId;  // Store the found user's ID
    private users foundUser; // Store the found user's details

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchEmailEditText = findViewById(R.id.searchEmailEditText);
        searchButton = findViewById(R.id.searchButton);
        sendRequestButton = findViewById(R.id.sendRequestButton);
        userInfoLayout = findViewById(R.id.userInfoLayout);

        username = findViewById(R.id.username);
        userstatus = findViewById(R.id.userstatus);
        userimg = findViewById(R.id.userimg);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        sendRequestButton.setEnabled(false);

        searchButton.setOnClickListener(v -> searchUserByEmail());

        sendRequestButton.setOnClickListener(v -> sendFriendRequest());
    }

    private void searchUserByEmail() {
        String email = searchEmailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(search.this, "Please enter an email", Toast.LENGTH_SHORT).show();
            return;
        }

        database.getReference().child("user").orderByChild("mail").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                foundUserId = userSnapshot.getKey();
                                foundUser = userSnapshot.getValue(users.class);

                                if (foundUser != null) {
                                    // Populate the user details
                                    username.setText(foundUser.getUserName());
                                    userstatus.setText(foundUser.getStatus());
                                    if (foundUser.getProfilepic() != null && !foundUser.getProfilepic().isEmpty()) {
                                        Picasso.get().load(foundUser.getProfilepic()).into(userimg);
                                    }

                                    userInfoLayout.setVisibility(View.VISIBLE);
                                    sendRequestButton.setVisibility(View.VISIBLE);
                                    sendRequestButton.setEnabled(true);
                                }
                            }
                        } else {
                            userInfoLayout.setVisibility(View.GONE);
                            sendRequestButton.setVisibility(View.GONE);
                            Toast.makeText(search.this, "User not found.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(search.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendFriendRequest() {
        if (foundUserId != null && foundUser != null) {
            String currentUserEmail = auth.getCurrentUser().getEmail();
            String receiverEmail = searchEmailEditText.getText().toString().trim(); // Email of the receiver
            String requestId = database.getReference().child("friendRequests").push().getKey(); // Generate unique ID for request

            // Create a new FriendRequest object with all parameters including receiverEmail
            FriendRequest friendRequest = new FriendRequest(
                    requestId,
                    currentUserEmail,
                    foundUser.getUserName(),
                    foundUser.getStatus(),
                    foundUser.getProfilepic(),
                    "pending",
                    receiverEmail // Ensure you include this field
            );

            // Save the friend request to the "friendRequests" node in the database
            DatabaseReference requestsRef = database.getReference().child("friendRequests");
            requestsRef.child(requestId).setValue(friendRequest)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(search.this, "Friend request sent!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(search.this, "Failed to send friend request.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
