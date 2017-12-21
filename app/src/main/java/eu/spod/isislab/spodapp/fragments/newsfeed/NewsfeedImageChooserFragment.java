package eu.spod.isislab.spodapp.fragments.newsfeed;

import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;

import eu.spod.isislab.spodapp.R;
import eu.spod.isislab.spodapp.adapters.NewsfeedImageChooserAdapter;

public class NewsfeedImageChooserFragment extends BottomSheetDialogFragment{

    private NewsfeedImageChooserAdapter.ItemClickListener mListener;
    private NewsfeedImageChooserAdapter mAdapter;

    public NewsfeedImageChooserFragment() { }

    public static NewsfeedImageChooserFragment getInstance(String[] links){
        NewsfeedImageChooserFragment fragment = new NewsfeedImageChooserFragment();

        Bundle args = new Bundle();
        args.putStringArray("LINKS", links);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.newsfeed_image_chooser_bottom_sheet, null);
        GridView imagesGrid = (GridView) view.findViewById(R.id.newsfeed_image_chooser_grid);

        String[] links = getArguments().getStringArray("LINKS");

        mAdapter = new NewsfeedImageChooserAdapter(getContext(), links);
        mAdapter.setOnItemClickListener(mListener);
        imagesGrid.setAdapter(mAdapter);

        int widthPixels = getContext().getResources().getDisplayMetrics().widthPixels;
        int columnNumber = widthPixels / 100; //100 is the dimension of an image viewù
        imagesGrid.setNumColumns(columnNumber);
        dialog.setContentView(view);
    }

    public void setOnItemClickListener(NewsfeedImageChooserAdapter.ItemClickListener listener) {
        this.mListener = listener;
    }
}
