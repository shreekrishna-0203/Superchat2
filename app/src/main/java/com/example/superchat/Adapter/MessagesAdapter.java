package com.example.superchat.Adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.superchat.Model.Messages;
import com.example.superchat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_TEXT_SENDER = 1;
    public static final int VIEW_TYPE_TEXT_RECEIVER = 2;
    public static final int VIEW_TYPE_IMAGE_SENDER = 3;
    public static final int VIEW_TYPE_IMAGE_RECEIVER = 4;
    public static final int VIEW_TYPE_PDF_SENDER = 5;
    public static final int VIEW_TYPE_PDF_RECEIVER = 6;
    public static final int VIEW_TYPE_DOCUMENT_SENDER = 7;
    public static final int VIEW_TYPE_DOCUMENT_RECEIVER = 8;

    private final Context context;
    private static List<Messages> messagesList;
    private String senderUID;

    public MessagesAdapter(Context context, List<Messages> messagesList) {
        this.context = context;
        this.messagesList = messagesList;
        this.senderUID = FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView;

        switch (viewType) {

            case VIEW_TYPE_TEXT_RECEIVER:
                itemView = inflater.inflate(R.layout.receiver_layout_item, parent, false);
                return new TextMessageViewHolder(itemView);

            case VIEW_TYPE_IMAGE_SENDER:
                itemView = inflater.inflate(R.layout.sender_image_item, parent, false);
                return new ImageMessageViewHolder(itemView);

            case VIEW_TYPE_IMAGE_RECEIVER:
                itemView = inflater.inflate(R.layout.receiver_image_item, parent, false);
                return new ImageMessageViewHolder(itemView);

            case VIEW_TYPE_PDF_SENDER:
                itemView = inflater.inflate(R.layout.sender_layout_item, parent, false);
                return new PdfMessageViewHolder(itemView, context);

            case VIEW_TYPE_PDF_RECEIVER:
                itemView = inflater.inflate(R.layout.receiver_layout_item, parent, false);
                return new PdfMessageViewHolder(itemView, context);

            case VIEW_TYPE_DOCUMENT_SENDER:
                itemView = inflater.inflate(R.layout.sender_webview_layout, parent, false);
                return new DocumentMessageViewHolder(itemView);

            case VIEW_TYPE_DOCUMENT_RECEIVER:
                itemView = inflater.inflate(R.layout.receiver_webview_layout, parent, false);
                return new DocumentMessageViewHolder(itemView);

            default:
                // Default to text sender
                itemView = inflater.inflate(R.layout.sender_layout_item, parent, false);
                return new TextMessageViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Messages message = messagesList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_TEXT_SENDER:
            case VIEW_TYPE_TEXT_RECEIVER:
                ((TextMessageViewHolder) holder).bindTextMessage(message);
                ((TextMessageViewHolder) holder).bindTimestamp(message.getTimestamp());
                break;

            case VIEW_TYPE_IMAGE_SENDER:
            case VIEW_TYPE_IMAGE_RECEIVER:
                ((ImageMessageViewHolder) holder).bindImageMessage(message);
                ((ImageMessageViewHolder) holder).bindTimestamp(message.getTimestamp());
                break;

            case VIEW_TYPE_PDF_SENDER:
            case VIEW_TYPE_PDF_RECEIVER:
                ((PdfMessageViewHolder) holder).bindPdfMessage(message);
                break;

            case VIEW_TYPE_DOCUMENT_SENDER:
            case VIEW_TYPE_DOCUMENT_RECEIVER:
                ((DocumentMessageViewHolder) holder).bindDocumentMessage(message);
                ((DocumentMessageViewHolder) holder).getFormattedTimestamp(message.getTimestamp());
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Messages message = messagesList.get(position);

        if (message.isImage()) {
            return message.getSender().equals(senderUID) ? VIEW_TYPE_IMAGE_SENDER : VIEW_TYPE_IMAGE_RECEIVER;
        } else if (message.isPdf()) {
            return message.getSender().equals(senderUID) ? VIEW_TYPE_PDF_SENDER : VIEW_TYPE_PDF_RECEIVER;
        } else if (message.isDocument()) {
            return message.getSender().equals(senderUID) ? VIEW_TYPE_DOCUMENT_SENDER : VIEW_TYPE_DOCUMENT_RECEIVER;
        } else {
            return message.getSender().equals(senderUID) ? VIEW_TYPE_TEXT_SENDER : VIEW_TYPE_TEXT_RECEIVER;
        }
    }

    // View holder for text messages
    private static class TextMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtMessages;
        private final TextView txtTimestamp;
        private final CheckBox checkBox;

        TextMessageViewHolder(View itemView) {
            super(itemView);
            txtMessages = itemView.findViewById(R.id.txtMessages);
            txtTimestamp = itemView.findViewById(R.id.txtTimestamp);
            checkBox = itemView.findViewById(R.id.checkBox);
        }

        void bindTextMessage(Messages message) {
            txtMessages.setText(message.getMessage());

            // Set the visibility of CheckBox based on the selection state
            checkBox.setVisibility(message.isSelected() ? View.VISIBLE : View.GONE);
            checkBox.setChecked(message.isSelected());
        }

        void bindTimestamp(long timestamp) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            String formattedTime = dateFormat.format(new Date(timestamp));
            txtTimestamp.setText(formattedTime);
        }
    }

    public class ImageMessageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgMessages;
        private final TextView txtTimestamp;

        ImageMessageViewHolder(View itemView) {
            super(itemView);
            imgMessages = itemView.findViewById(R.id.imgMessages);
            txtTimestamp = itemView.findViewById(R.id.txtTimestamp);
        }

        void bindImageMessage(Messages message) {
            if (message != null && message.isImage()) {
                String imagePath = message.getImageUrl();

                // Check if imgMessages is not null
                if (imgMessages != null) {
                    // Check if imagePath is not null or empty
                    if (!TextUtils.isEmpty(imagePath)) {
                        // Load the image asynchronously using Picasso
                        Picasso.get().load(new File(imagePath)).into(imgMessages);
                    } else {
                        // Handle null or empty path (e.g., show a placeholder)
                        imgMessages.setImageResource(R.drawable.gallery);
                    }
                } else {
                    Log.e("ImageLoading", "imgMessages is null");
                }
            }
        }





        void bindTimestamp(long timestamp) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                String formattedTime = dateFormat.format(new Date(timestamp));

                if (txtTimestamp != null) {
                    txtTimestamp.setText(formattedTime);
                } else {
                    Log.e("ViewHolderError", "txtTimestamp is null");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class PdfMessageViewHolder extends RecyclerView.ViewHolder {
        private final Context context;
        private final WebView pdfWebView;

        PdfMessageViewHolder(View itemView, Context context) {
            super(itemView);
            this.context = context;
            pdfWebView = itemView.findViewById(R.id.documentWebView);

            // Set up a click listener on the itemView (assuming the whole item is clickable)
            itemView.setOnClickListener(v -> {
                Messages message = messagesList.get(getAdapterPosition());
                openPdfWithIntent(message.getMessage());
            });
        }

        void bindPdfMessage(Messages message) {
            if (message.isPdf()) {
                // Load PDF using Google Docs Viewer
                String pdfUrl = "https://docs.google.com/gview?embedded=true&url=" + message.getMessage();
                pdfWebView.getSettings().setJavaScriptEnabled(true);
                pdfWebView.loadUrl(pdfUrl);
            }
        }

        private void openPdfWithIntent(String pdfUrl) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(pdfUrl), "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {

                Toast.makeText(context, "No PDF viewer app installed. Please install one to view the document.", Toast.LENGTH_SHORT).show();
            }
        }

    }

    // View holder for document messages
    // View holder for document messages
    public class DocumentMessageViewHolder extends RecyclerView.ViewHolder {
        private WebView documentWebView;
        private TextView txtTimestamp;

        public DocumentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            documentWebView = itemView.findViewById(R.id.documentWebView);
            txtTimestamp = itemView.findViewById(R.id.txtTimestamp);

            // Set up a click listener on the documentWebView to open a PDF reader
            documentWebView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    // Open the document in an external PDF reader
                    openPdfWithIntent(url);
                    return true;
                }
            });
        }

        public void bindDocumentMessage(Messages message) {
            // Assuming the document URL or local path is stored in message.getDocumentUrl()
            String documentUrl = message.getDocumentUrl();

            // Enable JavaScript for the WebView
            documentWebView.getSettings().setJavaScriptEnabled(true);

            // Load the document into the WebView
            documentWebView.loadUrl("file://" + documentUrl);

            // Set the timestamp
            txtTimestamp.setText(getFormattedTimestamp(message.getTimestamp()));
        }

        private String getFormattedTimestamp(long timestamp) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            return dateFormat.format(new Date(timestamp));
        }

        private void openPdfWithIntent(String pdfUrl) {
            Context context = itemView.getContext();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(pdfUrl), "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, "No PDF viewer app installed. Please install one to view the document.", Toast.LENGTH_SHORT).show();
            }
        }
    }



}
