package com.example.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class chatwindo extends AppCompatActivity {
    private static final String TAG = "chatwindo";

    String reciverimg, reciverUid, reciverName, SenderUID;
    CircleImageView profile;
    TextView reciverNName;
    FirebaseDatabase database;
    FirebaseAuth firebaseAuth;
    public static String senderImg;
    public static String reciverIImg;
    CardView sendbtn;
    EditText textmsg;

    String senderRoom, reciverRoom;
    RecyclerView messageAdpter;
    ArrayList<msgModelclass> messagesArrayList;
    messagesAdpter mmessagesAdpter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatwindo);

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        // Retrieve intent data
        reciverName = getIntent().getStringExtra("nameeee");
        reciverimg = getIntent().getStringExtra("reciverImg");
        reciverUid = getIntent().getStringExtra("uid");

        // Log if receiver data is null
        if (reciverName == null) {
            Log.e(TAG, "Receiver name is null");
        }
        if (reciverimg == null) {
            Log.e(TAG, "Receiver image URL is null");
        }
        if (reciverUid == null) {
            Log.e(TAG, "Receiver UID is null");
        }

        // Initialize UI elements
        messagesArrayList = new ArrayList<>();
        sendbtn = findViewById(R.id.sendbtnn);
        textmsg = findViewById(R.id.textmsg);
        reciverNName = findViewById(R.id.recivername);
        profile = findViewById(R.id.profile);
        messageAdpter = findViewById(R.id.msgadpter);

        // Set up RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messageAdpter.setLayoutManager(linearLayoutManager);
        mmessagesAdpter = new messagesAdpter(chatwindo.this, messagesArrayList);
        messageAdpter.setAdapter(mmessagesAdpter);

        // Load profile image and name
        Picasso.get().load(reciverimg).into(profile);
        reciverNName.setText(reciverName);

        // Initialize UIDs
        SenderUID = firebaseAuth.getUid();

        // Log if SenderUID is null
        if (SenderUID == null) {
            Log.e(TAG, "Sender UID is null");
            Toast.makeText(this, "Error: Sender UID cannot be null", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity
            return;
        }

        if (reciverUid == null) {
            Log.e(TAG, "Receiver UID is null");
            Toast.makeText(this, "Error: Receiver UID cannot be null", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity
            return;
        }

        // Create chat rooms
        senderRoom = SenderUID + reciverUid;
        reciverRoom = reciverUid + SenderUID;

        // Load sender's profile picture
        DatabaseReference reference = database.getReference().child("user").child(SenderUID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Object profilePicObj = snapshot.child("profilepic").getValue();
                    if (profilePicObj != null) {
                        senderImg = profilePicObj.toString();
                    } else {
                        Log.e(TAG, "Sender profile picture is null");
                        senderImg = ""; // Default or placeholder image URL
                    }
                    reciverIImg = reciverimg; // Set receiver image
                } else {
                    Log.e(TAG, "Snapshot does not exist for the current user");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load sender's profile picture: " + error.getMessage());
            }
        });

        // Load and listen for messages
        DatabaseReference chatreference = database.getReference().child("chats").child(senderRoom).child("messages");
        chatreference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    msgModelclass messages = dataSnapshot.getValue(msgModelclass.class);
                    if (messages != null) {
                        messagesArrayList.add(messages);
                    } else {
                        Log.e(TAG, "Message is null in the snapshot");
                    }
                }
                mmessagesAdpter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load messages: " + error.getMessage());
            }
        });

        // Send message
        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = textmsg.getText().toString();
                if (message.isEmpty()) {
                    Toast.makeText(chatwindo.this, "Enter The Message First", Toast.LENGTH_SHORT).show();
                    return;
                }
                textmsg.setText("");
                Date date = new Date();
                msgModelclass messagess = new msgModelclass(message, SenderUID, date.getTime());

                DatabaseReference chatRef = database.getReference().child("chats");
                chatRef.child(senderRoom).child("messages").push().setValue(messagess)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    chatRef.child(reciverRoom).child("messages").push().setValue(messagess);
                                } else {
                                    Log.e(TAG, "Failed to send message: " + task.getException());
                                }
                            }
                        });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listeners if needed to avoid memory leaks
        // Reference examples:
        // chatreference.removeEventListener(valueEventListener);
        // reference.removeEventListener(valueEventListener);
    }
}
