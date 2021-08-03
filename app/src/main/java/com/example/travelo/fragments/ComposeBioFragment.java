package com.example.travelo.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.travelo.R;
import com.example.travelo.constants.Constant;
import com.example.travelo.databinding.FragmentComposeBioBinding;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import es.dmoral.toasty.Toasty;


public class ComposeBioFragment extends DialogFragment {

    FragmentComposeBioBinding binding;
    Context context;

    public ComposeBioFragment() {
        // Required empty public constructor
    }

    // Defines the listener interface with a method passing back data result.
    public interface ComposeBioListener {
        void onFinishComposeBio(String bio);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentComposeBioBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();
        context = getContext();
        String oldBio = getArguments().getString("oldBio");
        binding.etCompose.setText(oldBio);
        // Add bio feature
        binding.btnUpdate.setOnClickListener(v -> updateBio());
        // Show soft keyboard automatically and request focus to field
        binding.etCompose.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return view;
    }

    @Override
    public void onDestroy() {
        binding = null;
        super.onDestroy();
    }

    public void updateBio() {
        String bio = binding.etCompose.getText().toString().replaceAll("\n","");
        if (bio.length() > Constant.MAX_BIO_LENGTH) {
            Toasty.error(context, "Sorry, your bio is too long", Toast.LENGTH_SHORT, true).show();
            return;
        }
        ParseQuery<ParseUser> userQuery = ParseQuery.getQuery(ParseUser.class);
        userQuery.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Toasty.error(context, "Couldn't update bio", Toast.LENGTH_SHORT, true).show();
                    dismiss();
                    return;
                }
                user.put("bio", bio);
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Toasty.error(context, "Couldn't update bio", Toast.LENGTH_SHORT, true).show();
                            dismiss();
                        } else {
                            Toasty.success(context, "Updated bio", Toast.LENGTH_SHORT, true).show();
                            ComposeBioListener listener = (ComposeBioListener)getParentFragment();
                            if (getParentFragment() == null) {
                                Toasty.info(context, "null fragment", Toasty.LENGTH_SHORT, true).show();
                                return;
                            }
                            listener.onFinishComposeBio(bio);
                            dismiss();
                        }
                    }
                });
            }
        });
    }
}