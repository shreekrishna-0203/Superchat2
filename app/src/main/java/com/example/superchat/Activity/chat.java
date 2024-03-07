package com.example.superchat.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.superchat.Adapter.MessagesAdapter;
import com.example.superchat.Model.Messages;
import com.example.superchat.R;
import com.google.android.gms.common.util.IOUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class chat extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_REQUEST_CODE = 101;
    private String receiverImage, receiverUID, receiverName, senderUID, senderRoom, receiverRoom;
    private CircleImageView profileImage;

    private WebView documentWebView;
    private EditText messageEditText;
    private Uri imageUri, savedImageUri, getImageUri;
    private CardView sendBtn, gallery, btnAttachFile;
    private EditText editTextMessage;
    private RelativeLayout chatLayout;

    private RecyclerView messageAdapter;
    private ArrayList<Messages> messagesArrayList;
    private MessagesAdapter adapter;
    private Messages messages;

    private static final int GALLERY_IMAGE_REQUEST_CODE = 125;
    private static final int DOCUMENT_PICKER_REQUEST_CODE = 126;


    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        receiverName = getIntent().getStringExtra("name");
        receiverImage = getIntent().getStringExtra("receiver-image");
        receiverUID = getIntent().getStringExtra("uid");
        senderUID = auth.getUid();

        messagesArrayList = new ArrayList<>();

        senderRoom = senderUID + receiverUID;
        receiverRoom = receiverUID + senderUID;

        messageAdapter = findViewById(R.id.messageAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        adapter = new MessagesAdapter(this, messagesArrayList);
        messageAdapter.setLayoutManager(linearLayoutManager);
        messageAdapter.setAdapter(adapter);

        profileImage = findViewById(R.id.receiver_image);
        TextView receiverNameTextView = findViewById(R.id.receiver_name);
        sendBtn = findViewById(R.id.send_btn);
        editTextMessage = findViewById(R.id.edittext_message);

        btnAttachFile = findViewById(R.id.btnAttachFile);



        Picasso.get().load(receiverImage).into(profileImage);
        receiverNameTextView.setText(receiverName);

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true); // For debugging
        built.setLoggingEnabled(true); // For debugging

        messageEditText = findViewById(R.id.edittext_message);

        documentWebView = findViewById(R.id.documentWebView);

        if (documentWebView != null) {
            documentWebView.getSettings().setJavaScriptEnabled(true);
            String documentUrl = "https://www.example.com/sample.pdf";
            documentWebView.loadUrl("https://docs.google.com/gview?embedded=true&url=" + documentUrl);
        } else {
            Log.e("WebView", "documentWebView is null");
        }

        String messageText = messageEditText.getText().toString();
        String imageUrl = "";
        if (!TextUtils.isEmpty(messageText) || !TextUtils.isEmpty(imageUrl)) {
            messageEditText.setText("");
            sendMessageToDatabase(messages, senderRoom, receiverRoom);
        }


        DatabaseReference messagesRef = database.getReference().child("messages");

        messagesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Handle added messages
                Messages newMessage = snapshot.getValue(Messages.class);
                if (newMessage != null) {
                    // Check if the new message is an image
                    if (newMessage.isImage()) {
                        // Load and display the image
                        loadImageAndDisplay(newMessage.getSender(), newMessage.getImageUrl());
                    }

                    messagesArrayList.add(newMessage);
                    adapter.notifyItemInserted(messagesArrayList.size() - 1);
                    messageAdapter.scrollToPosition(messagesArrayList.size() - 1);
                }
            }

            private void loadImageAndDisplay(String sender, String imageUri) {
                if (!TextUtils.isEmpty(imageUri)) {
                    Picasso.get().load(imageUri).into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            Messages imageMessage = new Messages(sender, imageUri, "image", Calendar.getInstance().getTimeInMillis());
                            messagesArrayList.add(imageMessage);
                            adapter.notifyItemInserted(messagesArrayList.size() - 1);
                            messageAdapter.scrollToPosition(messagesArrayList.size() - 1);

                            // Save the new image message to Firebase
                            sendMessageToDatabase(imageMessage, senderRoom, receiverRoom);
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                            Log.e("ImageLoading", "Failed to load image");
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                        }
                    });
                } else {
                    Log.e("ImageLoading", "Empty imageUri");
                }
            }


            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Handle updated messages
                Messages updatedMessage = snapshot.getValue(Messages.class);
                if (updatedMessage != null) {
                    // Find and update the existing message in your list
                    for (int i = 0; i < messagesArrayList.size(); i++) {
                        Messages existingMessage = messagesArrayList.get(i);
                        if (existingMessage.getMessageId().equals(updatedMessage.getMessageId())) {
                            messagesArrayList.set(i, updatedMessage);
                            adapter.notifyItemChanged(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // Handle removed messages if needed
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Handle moved messages if needed
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

        DatabaseReference reference = database.getReference().child("user").child(auth.getUid());
        DatabaseReference chatReference = database.getReference().child("chats").child(senderRoom).child("messages");

        chatReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Messages message = snapshot.getValue(Messages.class);
                messagesArrayList.add(message);
                adapter.notifyItemInserted(messagesArrayList.size() - 1);
                messageAdapter.scrollToPosition(messagesArrayList.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Handle updated messages if needed
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // Handle removed messages if needed
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Handle moved messages if needed
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
            }
        });

        chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Messages message = dataSnapshot.getValue(Messages.class);
                    messagesArrayList.add(message);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
            }
        });

        btnAttachFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAttachmentOptions();
            }
        });


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editTextMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    sendMessage(senderUID, message, "text");
                }
            }
        });

    }


    private void sendMessage(String sender, String message, String messageType) {
        if (messageType.equals("text")) {
            Messages textMessage = new Messages(sender, message, messageType, Calendar.getInstance().getTimeInMillis());
            sendMessageToDatabase(textMessage, senderRoom, receiverRoom);
        } else if (messageType.equals("image")) {
            sendImageMessage(sender, message, "image");
        } else if (messageType.equals("document")) {
            sendDocumentMessage(sender, message);
        }

        

        editTextMessage.setText("");
    }



    private void sendDocumentMessage(String sender, String documentUri) {
        Messages documentMessage = new Messages(sender, documentUri, "document", Calendar.getInstance().getTimeInMillis());
        sendMessageToDatabase(documentMessage, senderRoom, receiverRoom);
    }


    private void sendMessageToDatabase(Messages message, String senderRoom, String receiverRoom) {
        DatabaseReference chatsReference = FirebaseDatabase.getInstance().getReference().child("chats");
        chatsReference.child(senderRoom).child("messages").push().setValue(message);
        chatsReference.child(receiverRoom).child("messages").push().setValue(message);
    }


    private void sendImageMessage(String sender, String imageUri, String image) {
        Messages imageMessage = new Messages(sender, imageUri, "image", Calendar.getInstance().getTimeInMillis());
        sendMessageToDatabase(imageMessage, senderRoom, receiverRoom);
    }


    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GALLERY_IMAGE_REQUEST_CODE:
                    handleGalleryResult(data);
                    break;

                case DOCUMENT_PICKER_REQUEST_CODE:
                    handleDocumentResult(data);
                    break;
            }
        }
    }

    private void handleGalleryResult(Intent data) {
        if (data != null && data.getData() != null) {
            try {
                Uri imageUri = data.getData();
                Bitmap selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                // Check if the selectedImage is not null before proceeding
                if (selectedImage != null) {
                    // Your code to compress and save the image
                    saveImageToGallery(selectedImage);

                    // Send the image
                    sendMessage(senderUID, imageUri.toString(), "image");
                } else {
                    Log.e("Bitmap", "Failed to create Bitmap from selected image");
                }

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to handle gallery result", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveDocumentToInternalStorage(byte[] documentBytes, String documentFileName) {
        try {
            // Get the directory for the app's internal files
            File internalStorageDir = new File(getFilesDir(), "Superchat Documents");

            // Create the directory if it doesn't exist
            if (!internalStorageDir.exists()) {
                if (!internalStorageDir.mkdirs()) {
                    // Handle the case where directory creation failed
                    Log.e("DirectoryCreation", "Failed to create directory");
                }
            }

            // Create a File object for the document file in the internal storage directory
            File documentFile = new File(internalStorageDir, documentFileName);

            // Create a FileOutputStream to write into the file
            FileOutputStream fos = new FileOutputStream(documentFile);

            // Write the document bytes into the file
            fos.write(documentBytes);

            // Close the FileOutputStream
            fos.close();

            // Now, you can use documentFile.getAbsolutePath() as the URI or file path
            String documentUri = documentFile.getAbsolutePath();

            // Display the document in a WebView
            displayDocumentInWebView(documentUri);

            // Your code to send the document message to Firebase
            sendDocumentMessage(senderUID, documentUri);

            // Inform the user that the file has been saved
            Toast.makeText(this, "File saved successfully", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save document to internal storage", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayDocumentInWebView(String documentUri) {
        WebView webView = findViewById(R.id.documentWebView);

        if (webView != null) {
            // Enable JavaScript for the WebView
            webView.getSettings().setJavaScriptEnabled(true);

            // Set a WebViewClient to handle external URL loading
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    // Open external URL in a PDF reader or other document viewer
                    openExternalDocument(view.getContext(), Uri.parse(url));
                    return true; // The WebView will not load the URL
                }
            });

            // Load the document into the WebView
            webView.loadUrl("file://" + documentUri);
        } else {
            Log.e("WebView", "WebView is null");

            // Handle the case where WebView is null
            Toast.makeText(this, "Failed to display document: WebView is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void openExternalDocument(Context context, Uri documentUri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(documentUri, "application/pdf"); // Adjust the MIME type if necessary
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Handle the case where no PDF reader or document viewer app is installed
            Toast.makeText(context, "No app installed to view PDF", Toast.LENGTH_SHORT).show();
        }
    }
    private void handleDocumentResult(Intent data) {
        if (data != null && data.getData() != null) {
            try {
                Uri documentUri = data.getData();

                // Read the document bytes
                InputStream inputStream = getContentResolver().openInputStream(documentUri);
                byte[] documentBytes = IOUtils.toByteArray(inputStream);
                inputStream.close();

                // Save the document to internal storage
                saveDocumentToInternalStorage(documentBytes, "your_document_name.pdf");

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to handle document result", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void showAttachmentOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select an option");
        String[] options = {"Attach Document", "Gallery"};
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    attachDocument();
                } else if (which == 1) {
                    openGallery();
                }
            }
        });
        builder.show();
    }

    private void attachDocument() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, DOCUMENT_PICKER_REQUEST_CODE);
    }

    private void saveImageToGallery(Bitmap image) {
        // Compress the image
        Bitmap compressedImage = compressImage(image);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";

        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Superchat");

        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File imageFile = new File(storageDir, imageFileName);

        try {
            FileOutputStream fos = new FileOutputStream(imageFile);
            // Save the compressed image
            compressedImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();

            // Save the Uri of the saved image to SharedPreferences
            saveImageUriToPrefs(Uri.fromFile(imageFile));
            galleryAddPic(imageFile.getAbsolutePath());
            Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image to gallery", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap compressImage(Bitmap originalImage) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // Adjust the quality and size based on your requirements
        originalImage.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        byte[] byteArray = stream.toByteArray();
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }


    private void saveImageUriToPrefs(Uri imageUri) {
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("imageUri", imageUri.toString());
        editor.apply();
    }

    private void galleryAddPic(String imagePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);

        if (path != null) {
            return Uri.parse(path);
        }

        return null;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
