package com.stonedot.todo.smartwalk;


import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by komatsu on 2016/11/03.
 */

public class AboutDialogFragment extends DialogFragment {

    private final String GOO_HREF = "http://www.goo.ne.jp";
    private final String GOO_URI = "http://u.xgoo.jp/img/sgoo.png";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_about, null);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(R.string.about)
            .setView(dialogView)
            .create();
        AlertDialog dialog = dialogBuilder.show();
        setGooImage(dialogView);
        return dialog;
    }

    private void setGooImage(View view) {
        ImageView imageView = (ImageView) view.findViewById(R.id.goo_image);
        imageView.setOnClickListener(gooAnchor);
        Uri uri = Uri.parse(GOO_URI);
        Uri.Builder builder = uri.buildUpon();
        ImageHttpRequest request = new ImageHttpRequest(imageView);
        request.execute(builder);
    }

    private View.OnClickListener gooAnchor = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Uri uri = Uri.parse(GOO_HREF);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    };
}
