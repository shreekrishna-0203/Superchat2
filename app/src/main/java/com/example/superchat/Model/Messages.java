package com.example.superchat.Model;

import android.net.Uri;
import android.util.Log;

import com.example.superchat.Adapter.MessagesAdapter;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Messages {
    private String sender;
    private String message;
    private String messageType;
    private long timestamp;
    private String imageUrl;
    private Messages getImageMessage;
    private String documentUrl;
    private String messageId;
    private String documentName;
    private Date timestampDate;
    private boolean selected;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Messages() {
        // Default constructor required for Firebase
    }

    public Messages(String sender, String message, String messageType, long timestamp) {
        this.sender = sender;
        this.message = message;
        this.messageType = messageType;
        this.timestamp = timestamp;
        this.timestampDate = timestampDate;



        // Subscribe to the topic when a message is sent
        subscribeToTopic();
    }

    private void subscribeToTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic("message_" + messageId);
    }

    private void sendImageMessage(Uri selectedImageUri, String senderUID, List<Messages> messagesList, MessagesAdapter messagesAdapter) {
        // Upload the image to Firebase Storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("chat_images/" + UUID.randomUUID().toString());
        storageReference.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Image uploaded successfully
                    // Get the download URL
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Create a new messages object for the image
                        Messages imageMessage = new Messages(senderUID, "", "image", Calendar.getInstance().getTimeInMillis());

                        // Set the imageUrl for the image message
                        imageMessage.setImageUrl(uri.toString());

                        // Add the image message to your messagesList
                        messagesList.add(imageMessage);

                        // Notify the adapter that the data set has changed
                        messagesAdapter.notifyDataSetChanged();
                    }).addOnFailureListener(e -> {
                        Log.e("sendImageMessage", "Failed to upload image: " + e.getMessage());
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("sendImageMessage", "Failed to upload image: " + e.getMessage());
                });
    }


    private void sendTextMessage(String text, String senderUID, List<Messages> messagesList, MessagesAdapter messagesAdapter) {
        // Create a new messages object for the text
        Messages textMessage = new Messages(senderUID, text, "text", Calendar.getInstance().getTimeInMillis());

        // Add the text message to your messagesList
        messagesList.add(textMessage);

        // Notify the adapter that the data set has changed
        messagesAdapter.notifyDataSetChanged();
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }


    public boolean isImage() {
        return "image".equals(messageType);
    }
    public boolean isCameraImage() {
        return messageType.equals("camera_image");
    }



    public boolean isDocument() {
        return messageType != null && messageType.equals("document");
    }

    public String getDocumentName() {
        return documentName;
    }

    public boolean isPdf() {

        return documentName != null && documentName.toLowerCase().endsWith(".pdf");

    }
    public String getDocumentUrl() {
        return documentUrl;
    }

    public Messages getGetImageMessage() {
        return getImageMessage;
    }

    public void setGetImageMessage(Messages getImageMessage) {
        this.getImageMessage = getImageMessage;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public Date getTimestampDate() {
        return timestampDate;
    }

    public void setTimestampDate(Date timestampDate) {
        this.timestampDate = timestampDate;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}