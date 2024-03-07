package com.example.superchat.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class SelectOptionsDialog extends DialogFragment {

    private SelectOptionsDialogListener listener;

    public interface SelectOptionsDialogListener {
        void onFavoriteSelected();

        void onDeleteSelected();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select Options")
                .setPositiveButton("Favorite", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (listener != null) {
                            listener.onFavoriteSelected();
                        }
                    }
                })
                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (listener != null) {
                            listener.onDeleteSelected();
                        }
                    }
                });
        return builder.create();
    }

    public void setListener(SelectOptionsDialogListener listener) {
        this.listener = listener;
    }
}
