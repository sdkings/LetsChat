package com.example.letschat;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FriendRequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FriendRequestAdapter adapter;
    private ArrayList<FriendRequest> friendRequestsList;
    private FirebaseAuth auth;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);

        recyclerView = findViewById(R.id.recyclerViewFriendRequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendRequestsList = new ArrayList<>();
        adapter = new FriendRequestAdapter(friendRequestsList, this);
        recyclerView.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        loadFriendRequests();
    }

    private void loadFriendRequests() {
        String currentUserEmail = auth.getCurrentUser().getEmail();

        database.getReference().child("friendRequests").orderByChild("receiverEmail").equalTo(currentUserEmail)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        friendRequestsList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            FriendRequest request = dataSnapshot.getValue(FriendRequest.class);
                            if (request != null && "pending".equals(request.getStatus())) {
                                friendRequestsList.add(request);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(FriendRequestsActivity.this, "Error loading friend requests", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void acceptFriendRequest(FriendRequest request) {
        String currentUserId = auth.getCurrentUser().getUid();

        database.getReference().child("user").orderByChild("mail").equalTo(request.getSenderEmail())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                String senderId = userSnapshot.getKey();
                                // Add each other to friends list
                                database.getReference().child("user").child(currentUserId).child("friendsEmails").push().setValue(request.getSenderEmail());
                                database.getReference().child("user").child(senderId).child("friendsEmails").push().setValue(auth.getCurrentUser().getEmail());

                                // Update the request status to accepted
                                database.getReference().child("friendRequests").child(request.getRequestId()).child("status").setValue("accepted");

                                Toast.makeText(FriendRequestsActivity.this, "Friend request accepted", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(FriendRequestsActivity.this, "Error accepting friend request", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void declineFriendRequest(FriendRequest request) {
        // Simply remove the request from the database
        database.getReference().child("friendRequests").child(request.getRequestId()).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(FriendRequestsActivity.this, "Friend request declined", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(FriendRequestsActivity.this, "Error declining friend request", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
