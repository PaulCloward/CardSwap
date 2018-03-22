package com.paulcloward.cardswap.Auth;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.paulcloward.cardswap.MainActivity;
import com.paulcloward.cardswap.ProfileActivity;
import com.paulcloward.cardswap.R;

public class RegisterActivity extends AppCompatActivity {

    private Button btnRegister;
    private ProgressBar progressBar;
    private TextView tvHolder;
    private ImageView ivBack;

    private EditText etEmail;
    private EditText etPassword;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Sets status bar color to match background of page
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("Firebase Auth: ", "onAuthStateChanged:signed_in:" + user.getUid());
                    Toast.makeText(RegisterActivity.this, "Successfully registered", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, ProfileActivity.class));

                } else {
                    // User is signed out
                    // Log.d("Firebase Auth: ", "onAuthStateChanged:signed_out");
                    //Toast.makeText(LoginActivity.this, "Successfully signed out", Toast.LENGTH_SHORT).show();
                }
                // ...
            }
        };

        //Set Custom Fonts
        tvHolder = (TextView) findViewById(R.id.tv_signup_toolbar);
        Typeface myCustomRalewayFont = Typeface.createFromAsset(getAssets(), "fonts/Raleway-Regular.ttf");
        tvHolder.setTypeface(myCustomRalewayFont);
        tvHolder = (TextView) findViewById(R.id.sign_up_button);
        tvHolder.setTypeface(myCustomRalewayFont);
        tvHolder = (TextView) findViewById(R.id.tv_agreement_terms);
        tvHolder.setTypeface(myCustomRalewayFont);
        tvHolder = (TextView) findViewById(R.id.et_signup_first_name);
        tvHolder.setTypeface(myCustomRalewayFont);
        tvHolder = (TextView) findViewById(R.id.et_signup_last_name);
        tvHolder.setTypeface(myCustomRalewayFont);
        tvHolder = (TextView) findViewById(R.id.et_signup_email);
        tvHolder.setTypeface(myCustomRalewayFont);
        tvHolder = (TextView) findViewById(R.id.et_signup_password);
        tvHolder.setTypeface(myCustomRalewayFont);

        EditText et = (EditText) findViewById(R.id.et_signup_first_name);
        et.getBackground().setColorFilter(getResources().getColor(R.color.colorLightGray), PorterDuff.Mode.SRC_IN);
        et = (EditText) findViewById(R.id.et_signup_last_name);
        et.getBackground().setColorFilter(getResources().getColor(R.color.colorLightGray), PorterDuff.Mode.SRC_IN);
        etEmail = (EditText) findViewById(R.id.et_signup_email);
        etEmail.getBackground().setColorFilter(getResources().getColor(R.color.colorLightGray), PorterDuff.Mode.SRC_IN);
        etPassword = (EditText) findViewById(R.id.et_signup_password);
        etPassword.getBackground().setColorFilter(getResources().getColor(R.color.colorLightGray), PorterDuff.Mode.SRC_IN);

        ivBack = (ImageView)findViewById(R.id.iv_signup_back);

        ivBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            }
        });

        btnRegister = (Button)findViewById(R.id.sign_up_button);
        btnRegister.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                if(!email.equals("") && !password.equals("")){

                    mAuth.signInWithEmailAndPassword(email,password);
                    mAuth.createUserWithEmailAndPassword(email, password);
                }
                else{
                    Toast.makeText(RegisterActivity.this, "You didn't fill out all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
