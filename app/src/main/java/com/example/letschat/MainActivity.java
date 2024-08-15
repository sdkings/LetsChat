package com.example.letschat;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    RecyclerView mainUserRecyclerView;
    UserAdpter adapter;
    FirebaseDatabase database;
    ArrayList<users> usersArrayList;
    ImageView imglogout;
    ImageView camBut,setbut;
    ImageView requestsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        usersArrayList = new ArrayList<>();
        mainUserRecyclerView = findViewById(R.id.mainUserRecyclerView);
        mainUserRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdpter(MainActivity.this, usersArrayList);
        mainUserRecyclerView.setAdapter(adapter);

        camBut = findViewById(R.id.camBut);
        setbut = findViewById(R.id.settingBut);
        requestsButton = findViewById(R.id.requests);
        requestsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FriendRequestsActivity.class);
            startActivity(intent);
        });
        camBut.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, search.class);
            startActivity(intent);
        });

        setbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, setting.class);
                startActivity(intent);
            }
        });

        if (auth.getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, login.class);
            startActivity(intent);
            finish();
            return;
        }

        loadCurrentUserFriends();

        imglogout = findViewById(R.id.logoutimg);
        imglogout.setOnClickListener(v -> {
            Dialog dialog = new Dialog(MainActivity.this, R.style.dialoge);
            dialog.setContentView(R.layout.dialog_layout);
            Button yes = dialog.findViewById(R.id.yesbnt);
            Button no = dialog.findViewById(R.id.nobnt);
            yes.setOnClickListener(v1 -> {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, login.class);
                startActivity(intent);
                finish();
            });
            no.setOnClickListener(v12 -> dialog.dismiss());
            dialog.show();
        });
    }

    private void loadCurrentUserFriends() {
        DatabaseReference currentUserRef = database.getReference().child("user").child(auth.getCurrentUser().getUid());

        currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Retrieve the list of friends' emails
                DataSnapshot friendsEmailsSnapshot = snapshot.child("friendsEmails");
                List<String> friendsEmails = new ArrayList<>();

                if (friendsEmailsSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : friendsEmailsSnapshot.getChildren()) {
                        String email = childSnapshot.getValue(String.class);
                        if (email != null) {
                            friendsEmails.add(email);
                        }
                    }
                }

                // Load the friends' data based on their emails
                loadFriends(friendsEmails);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
            }
        });
    }

    private void loadFriends(List<String> friendsEmails) {
        DatabaseReference usersRef = database.getReference().child("user");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // Extract user details
                    String userId = dataSnapshot.getKey(); // Get user ID from the key
                    String userEmail = dataSnapshot.child("mail").getValue(String.class);
                    if (userEmail != null && friendsEmails.contains(userEmail)) {
                        String userName = dataSnapshot.child("userName").getValue(String.class);
                        String profilePic = dataSnapshot.child("profilepic").getValue(String.class);
                        String status = dataSnapshot.child("status").getValue(String.class);

                        // Create a user object and set its fields
                        users user = new users();
                        user.setUserId(userId);
                        user.setMail(userEmail);
                        user.setUserName(userName);
                        user.setProfilepic(profilePic);
                        user.setStatus(status);

                        // Add the user to the list
                        usersArrayList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }
}
