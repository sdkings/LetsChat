package com.example.letschat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {

    private ArrayList<FriendRequest> friendRequests;
    private FriendRequestsActivity activity;
    private static final String DEFAULT_PROFILE_PIC_URL = "https://firebasestorage.googleapis.com/v0/b/letschat-b1051.appspot.com/o/20DoQ.png?alt=media&token=61bc71c2-e3ef-4482-88a5-b2c9f7affdea";

    public FriendRequestAdapter(ArrayList<FriendRequest> friendRequests, FriendRequestsActivity activity) {
        this.friendRequests = friendRequests;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_friend_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendRequest request = friendRequests.get(position);

        // Bind the sender's name, status, and email
        holder.senderName.setText(request.getSenderName());
        holder.senderStatus.setText(request.getSenderStatus());
        holder.senderEmail.setText(request.getSenderEmail());

        // Load the sender's profile picture using Picasso
        String profilePicUrl = request.getSenderProfilePicUrl();
        if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
            Picasso.get().load(profilePicUrl).into(holder.senderProfilePic);
        } else {
            // Load the default profile picture from the Firebase Storage URL
            Picasso.get().load(DEFAULT_PROFILE_PIC_URL).into(holder.senderProfilePic);
        }

        // Set click listeners for the accept and decline buttons
        holder.acceptButton.setOnClickListener(v -> activity.acceptFriendRequest(request));
        holder.declineButton.setOnClickListener(v -> activity.declineFriendRequest(request));
    }

    @Override
    public int getItemCount() {
        return friendRequests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView senderProfilePic;
        TextView senderName, senderStatus, senderEmail;
        Button acceptButton, declineButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            senderProfilePic = itemView.findViewById(R.id.senderProfilePic);
            senderName = itemView.findViewById(R.id.senderName);
            senderStatus = itemView.findViewById(R.id.senderStatus);
            senderEmail = itemView.findViewById(R.id.senderEmail);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            declineButton = itemView.findViewById(R.id.declineButton);
        }
    }
}
