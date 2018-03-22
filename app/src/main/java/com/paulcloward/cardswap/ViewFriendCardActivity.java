package com.paulcloward.cardswap;

import android.content.Intent;
import android.graphics.Typeface;
import android.media.Image;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

public class ViewFriendCardActivity extends AppCompatActivity {

    private String userID;
    private String cardID;

    private DatabaseReference rootRef;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseStorage mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_friend_card);

        //Sets status bar color to match background of page
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        rootRef = mFirebaseDatabase.getReference();
        mStorage = FirebaseStorage.getInstance();

        if(!getIntent().getStringExtra("USER_ID").equals(null)){

            userID = getIntent().getStringExtra("USER_ID");
            cardID = getIntent().getStringExtra("CARD_ID");

            rootRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    Card displayCard = dataSnapshot.child("users").child(userID).child("cards").child(cardID).getValue(Card.class);

                    TextView tvToolbarTitle = findViewById(R.id.tv_viewfriendcard_toolbar_title);
                    tvToolbarTitle.setText("");

                    ImageView ivImage = findViewById(R.id.iv_viewfriendcard_image);
                    Picasso.with(ViewFriendCardActivity.this).load(displayCard.getImgURL()).into(ivImage);
                    TextView tvName = findViewById(R.id.tv_viewfriendcard_name);
                    tvName.setText(displayCard.getName());
                    TextView tvTitle = findViewById(R.id.tv_viewfriendcard_title);
                    tvTitle.setText(displayCard.getTitle());
                    TextView tvCompany = findViewById(R.id.tv_viewfriendcard_company);
                    tvCompany.setText(displayCard.getCompany());
                    TextView tvPhoneNumber = findViewById(R.id.tv_viewfriendcard_phone);
                    tvPhoneNumber.setText(displayCard.getPhoneNumber());
                    TextView tvEmail = findViewById(R.id.tv_viewfriendcard_email);
                    tvEmail.setText(displayCard.getEmailAddress());
                    TextView tvStreetAddress = findViewById(R.id.tv_viewfriendcard_street_address);
                    tvStreetAddress.setText(displayCard.getStreetAddress());
                    TextView tvWebsite = findViewById(R.id.tv_viewfriendcard_website);
                    tvWebsite.setText(displayCard.getWebsiteAddress());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        //Go back to last page
        ImageView ivBack = findViewById(R.id.iv_viewfriendcard_toolbar_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ViewFriendCardActivity.this, ProfileActivity.class));
            }
        });

        setTypeface();
    }

    //Setup Raleway font family through page
    private void setTypeface(){

        Typeface tfRaleway = Typeface.createFromAsset(getAssets(), "fonts/Raleway-Regular.ttf");
        TextView tvToolbarTitle = findViewById(R.id.tv_viewfriendcard_toolbar_title);
        tvToolbarTitle.setTypeface(tfRaleway);
        TextView tvName = findViewById(R.id.tv_viewfriendcard_name);
        tvName.setTypeface(tfRaleway);
        TextView tvTitle = findViewById(R.id.tv_viewfriendcard_title);
        tvTitle.setTypeface(tfRaleway);
        TextView tvCompany = findViewById(R.id.tv_viewfriendcard_company);
        tvCompany.setTypeface(tfRaleway);
        TextView tvPhone = findViewById(R.id.tv_viewfriendcard_phone);
        tvPhone.setTypeface(tfRaleway);
        TextView tvEmail = findViewById(R.id.tv_viewfriendcard_email);
        tvEmail.setTypeface(tfRaleway);
        TextView tvStreetAddress = findViewById(R.id.tv_viewfriendcard_street_address);
        tvStreetAddress.setTypeface(tfRaleway);
        TextView tvWebsite = findViewById(R.id.tv_viewfriendcard_website);
        tvWebsite.setTypeface(tfRaleway);
    }
}
