package com.example.travelo.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.travelo.R;
import com.example.travelo.constants.Constant;
import com.example.travelo.databinding.ActivityLoginBinding;
import com.example.travelo.fragments.CreateRoomFragment;
import com.example.travelo.fragments.SignupFragment;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SendCallback;

import es.dmoral.toasty.Toasty;
import shortbread.Shortcut;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";
    ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        if (ParseUser.getCurrentUser() != null) {
            goMainActivity();
        }
        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "User clicked login button");
                String username = binding.etUsername.getText().toString();
                String password = binding.etPassword.getText().toString();
                loginUser(username, password);
            }
        });

        binding.btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goSignupActivity();
            }
        });
    }

    private void loginUser(String username, String password) {
        Log.i(TAG, "User attempted to login");
        // Take user to main activity if signed in correctly
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with login", e);
                    Toasty.error(LoginActivity.this, "Issue with login", Toast.LENGTH_SHORT, true).show();
                    return;
                }
                Log.i(TAG, "User logged in");
                String userId = user.getObjectId();
                ParsePush.subscribeInBackground(userId);
                goMainActivity();
                Toasty.success(LoginActivity.this, "You have logged in!", Toast.LENGTH_SHORT, true).show();
            }
        });
    }

    private void goMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void goSignupActivity() {
        FragmentManager fm = getSupportFragmentManager();
        SignupFragment signupFragment = SignupFragment.newInstance("Sign Up");
        signupFragment.show(fm, "fragment_signup");
    }

}