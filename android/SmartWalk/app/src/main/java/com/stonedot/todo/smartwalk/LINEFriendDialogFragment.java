package com.stonedot.todo.smartwalk;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;

/**
 * Created by goto on 2016/10/30.
 */

public class LINEFriendDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_DeviceDefault_Dialog);
        return builder.setMessage(R.string.lfd_dialog_message)
                .setPositiveButton(R.string.lfd_dialog_accept, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Uri uri = Uri.parse("https://line.me/R/ti/p/%40tlv6714f");
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                        Log.d("LineFragment", "Start LINE.");
                    }
                })
                .setNegativeButton(R.string.lfd_dialog_decline, null).create();
    }
}
