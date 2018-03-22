package com.paulcloward.cardswap;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.paulcloward.cardswap.Auth.LoginActivity;
import com.paulcloward.cardswap.Auth.RegisterActivity;

public class MainActivity extends AppCompatActivity {

    Button btnCreateAccount, btnSignIn;
    TextView tvSignIn;
    TextView tvHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Sets status bar color to match background of page
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        //Set Custom Font
        tvHolder = (TextView) findViewById(R.id.tv_main_title_2);
        Typeface myCustomRalewayBold = Typeface.createFromAsset(getAssets(), "fonts/Raleway-Bold.ttf");
        tvHolder.setTypeface(myCustomRalewayBold);

        tvHolder = (TextView) findViewById(R.id.tv_main_title);
        Typeface myCustomRalewayRegular = Typeface.createFromAsset(getAssets(), "fonts/Raleway-Regular.ttf");
        tvHolder.setTypeface(myCustomRalewayRegular);
        tvHolder = (TextView) findViewById(R.id.btn_create_account);
        tvHolder.setTypeface(myCustomRalewayRegular);
        tvHolder = (TextView) findViewById(R.id.tv_sign_in);
        tvHolder.setTypeface(myCustomRalewayRegular);
        Typeface myCustomRalewayLight = Typeface.createFromAsset(getAssets(), "fonts/Raleway-Light.ttf");
        tvHolder = (TextView) findViewById(R.id.tv_main_subtitle);
        tvHolder.setTypeface(myCustomRalewayRegular);

        btnCreateAccount = (Button) findViewById(R.id.btn_create_account);

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });

        tvSignIn = (TextView)findViewById(R.id.tv_sign_in);

        tvSignIn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });



    }

}
