package com.example.letschat;

public class FriendRequest {

    private String requestId;
    private String senderEmail;
    private String senderName;
    private String senderStatus;
    private String senderProfilePicUrl;
    private String status;
    private String receiverEmail;

    // Default constructor required for calls to DataSnapshot.getValue(FriendRequest.class)
    public FriendRequest() {
    }

    // Constructor with parameters
    public FriendRequest(String requestId, String senderEmail, String senderName, String senderStatus, String senderProfilePicUrl, String status, String receiverEmail) {
        this.requestId = requestId;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
        this.senderStatus = senderStatus;
        this.senderProfilePicUrl = senderProfilePicUrl;
        this.status = status;
        this.receiverEmail = receiverEmail;
    }

    // Getter and setter methods for all fields
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderStatus() {
        return senderStatus;
    }

    public void setSenderStatus(String senderStatus) {
        this.senderStatus = senderStatus;
    }

    public String getSenderProfilePicUrl() {
        return senderProfilePicUrl;
    }

    public void setSenderProfilePicUrl(String senderProfilePicUrl) {
        this.senderProfilePicUrl = senderProfilePicUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }
}
