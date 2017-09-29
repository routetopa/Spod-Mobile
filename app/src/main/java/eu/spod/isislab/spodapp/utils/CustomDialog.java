package eu.spod.isislab.spodapp.utils;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import eu.spod.isislab.spodapp.R;

public class CustomDialog extends DialogFragment {

    String title;
    String body;
    CustomDialog self;

    public CustomDialog() {
        self = this;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.custom_dialog, container, false);
        ((TextView)v.findViewById(R.id.dialog_title)).setText(title);
        ((TextView)v.findViewById(R.id.dialog_body)).setText(body);

        ((Button)v.findViewById(R.id.dialog_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                self.dismiss();

            }
        });
        return v;
    }

}
