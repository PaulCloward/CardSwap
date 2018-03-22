package com.paulcloward.cardswap;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Random;

public class NewCardActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView tvNewCardName;
    private TextView tvNewCardTitle;
    private TextView tvNewCardCompany;
    private TextView tvNewCardEmail;
    private TextView tvNewCardPhone;
    private TextView tvNewCardWebsiteAddress;
    private TextView tvNewCardFaxAddress;
    private TextView tvNewCardStreetAddress;

    private ImageView ivNewCardImage;
    private Uri mImageUri;

    //Old Firebase implementation
    /*private DatabaseReference rootRef;

    private FirebaseDatabase mFirebaseDatabase;*/

    //Firebase stuff
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth mAuth;
    private StorageTask mUploadTask;


    int[] imgUrlArray = { R.drawable.business1, R.drawable.business2, R.drawable.business3, R.drawable.business4,R.drawable.business5,
            R.drawable.business6, R.drawable.business7};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_card);

        //Sets status bar color to match background of page
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        //Old Firebase implementation
        /*
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        rootRef = mFirebaseDatabase.getReference();*/

        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        ImageView ivGoBackNoSave = (ImageView) findViewById(R.id.img_close_no_save);
        ivGoBackNoSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(NewCardActivity.this, ProfileActivity.class));
            }
        });

        ImageView ivGoBackSave = (ImageView) findViewById(R.id.img_close_save);
        ivGoBackSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                /* Old way of saving card to firebase
                rootRef.child("users").child(userID).child("cards").push().setValue(newCard);*/

                uploadFile();
                startActivity(new Intent(NewCardActivity.this, ProfileActivity.class));
            }
        });

        setTypefaces();

        tvNewCardName = (TextView)findViewById(R.id.tv_newcard_name);
        tvNewCardTitle = (TextView)findViewById(R.id.tv_newcard_title);
        tvNewCardCompany = (TextView)findViewById(R.id.tv_newcard_company);
        tvNewCardEmail = (TextView)findViewById(R.id.tv_newcard_email);
        tvNewCardPhone = (TextView)findViewById(R.id.tv_newcard_phone);
        tvNewCardFaxAddress = (TextView)findViewById(R.id.tv_newcard_fax_address);
        tvNewCardWebsiteAddress = (TextView)findViewById(R.id.tv_newcard_website_address);
        tvNewCardStreetAddress = (TextView)findViewById(R.id.tv_newcard_street_address);
        ivNewCardImage = (ImageView)findViewById(R.id.iv_newcard_image);

        ivNewCardImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });
    }


    /*
    * onClickEmail(View v)
    *
    * Creates popup to allow user to add an email address
    * Also sets the Typeface of the text
    * */
    public void onClickEmail(View v){

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(NewCardActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.popup_email, null);
                final EditText mEmail = (EditText)mView.findViewById(R.id.et_popup_email);

                ImageView mCheckmark = (ImageView)mView.findViewById(R.id.iv_popup_email_checkmark);

                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();

                Typeface tfRalewayMedium = Typeface.createFromAsset(getAssets(), "fonts/Raleway-Medium.ttf");
                TextView tvEmail = (TextView) mView.findViewById(R.id.tv_popup_email_title);
                tvEmail.setTypeface(tfRalewayMedium);

                dialog.show();

                mCheckmark.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!mEmail.getText().toString().isEmpty()){

                            tvNewCardEmail.setText(mEmail.getText().toString().trim());
                            dialog.dismiss();


                        } else{
                            Toast.makeText(NewCardActivity.this, "Please fill in email address field", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    /*
    * onClickPhone(View v)
    *
    * Creates popup to allow user to add a phone number
    * Also sets the Typeface of the text
    * */
    public void onClickPhone(View v){

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(NewCardActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.popup_phone, null);
        final EditText mPhone = (EditText)mView.findViewById(R.id.et_popup_phone);

        ImageView mCheckmark = (ImageView)mView.findViewById(R.id.iv_popup_phone_checkmark);

        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();

        Typeface tfRalewayMedium = Typeface.createFromAsset(getAssets(), "fonts/Raleway-Medium.ttf");
        TextView tvPhone = (TextView) mView.findViewById(R.id.tv_popup_phone_title);
        tvPhone.setTypeface(tfRalewayMedium);

        dialog.show();

        mCheckmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mPhone.getText().toString().isEmpty()){

                    tvNewCardPhone.setText(mPhone.getText().toString().trim());
                    dialog.dismiss();

                } else{
                    Toast.makeText(NewCardActivity.this, "Please fill in phone number field", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /*
    * onClickCompanyInfo(View v)
    *
    * Creates popup to allow user to add name, company position, company name
    * Also sets the Typeface of the text
    * */
    public void onClickCompanyInformation(View v){

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(NewCardActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.popup_company_information, null);
        final EditText mName = (EditText)mView.findViewById(R.id.et_popup_companyinfo_name);
        final EditText mTitle = (EditText)mView.findViewById(R.id.et_popup_companyinfo_title);
        final EditText mCompany = (EditText)mView.findViewById(R.id.et_popup_companyinfo_company);

        ImageView mCheckmark = (ImageView)mView.findViewById(R.id.iv_popup_companyinfo_checkmark);

        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();

        Typeface tfRalewayMedium = Typeface.createFromAsset(getAssets(), "fonts/Raleway-Medium.ttf");
        TextView tvName = (TextView) mView.findViewById(R.id.tv_popup_companyinfo_name);
        tvName.setTypeface(tfRalewayMedium);
        TextView tvTitle = (TextView) mView.findViewById(R.id.tv_popup_companyinfo_title);
        tvTitle.setTypeface(tfRalewayMedium);
        TextView tvCompany = (TextView) mView.findViewById(R.id.tv_popup_companyinfo_company);
        tvCompany.setTypeface(tfRalewayMedium);

        dialog.show();

        mCheckmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mName.getText().toString().isEmpty() && !mTitle.getText().toString().isEmpty() && !mCompany.getText().toString().isEmpty()){

                    tvNewCardName.setText(mName.getText().toString().trim());
                    tvNewCardTitle.setText(mTitle.getText().toString().trim());
                    tvNewCardCompany.setText(mCompany.getText().toString().trim());

                    dialog.dismiss();

                } else{
                    Toast.makeText(NewCardActivity.this, "Please fill in empty fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /*
    * onClickContactInfo(View v)
    *
    * Creates popup to allow user to add contact information
    * Also sets the Typeface of the text
    * */
    public void onClickContactInformation(View v){

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(NewCardActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.popup_contact_information, null);

        final EditText mPhone = mView.findViewById(R.id.et_popup_contact_information_phone);
        final EditText mCell = mView.findViewById(R.id.et_popup_contact_information_cell);
        final EditText mFax = mView.findViewById(R.id.et_popup_contact_information_fax);
        final EditText mMailingAddress = mView.findViewById(R.id.et_popup_contact_information_mailing_address);
        final EditText mStreetAddress = mView.findViewById(R.id.et_popup_contact_information_street_address);
        final EditText mWebsite = mView.findViewById(R.id.et_popup_contact_information_website);

        ImageView ivCheck = mView.findViewById(R.id.iv_popup_contact_information_close);

        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();

        Typeface tfRalewayMedium = Typeface.createFromAsset(getAssets(), "fonts/Raleway-Medium.ttf");
        TextView tvTitle = (TextView) mView.findViewById(R.id.tv_popup_contactinfo_title);
        tvTitle.setTypeface(tfRalewayMedium);

        dialog.show();
        Log.i("IN CONTACT METHOD: ", "YES");
        ivCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i("IN CLOSE METHOD: ", "YES");

                if(!mFax.getText().toString().isEmpty()){
                    Log.i("The Fax is in",mFax.getText().toString());
                    tvNewCardFaxAddress.setText(mFax.getText().toString().trim());
                }
                if(!mStreetAddress.getText().toString().isEmpty()){
                    Log.i("The street is in",mStreetAddress.getText().toString());
                    tvNewCardStreetAddress.setText(mStreetAddress.getText().toString().trim());
                }
                if(!mWebsite.getText().toString().isEmpty()){
                    Log.i("The website is in",mWebsite.getText().toString());
                    tvNewCardWebsiteAddress.setText(mWebsite.getText().toString().trim());
                    Log.i("IN MWebsite METHOD: ","YES");
                    dialog.dismiss();
                }
            }
        });
    }


    /*
    * setTypeFaces()
    *
    * Sets the typeface for all the text in NewCardActivity.
    * (**Does not include popup AlertDialog pages. Set the typeface of their text in onClickMethods **)
    *
    * */
    public void setTypefaces(){

        Typeface tfRaleway = Typeface.createFromAsset(getAssets(), "fonts/Raleway-Regular.ttf");
        TextView tvToolbarTitle = (TextView)findViewById(R.id.tv_newcard_toolbar_title);
        tvToolbarTitle.setTypeface(tfRaleway);
        TextView tvName = (TextView)findViewById(R.id.tv_newcard_name);
        tvName.setTypeface(tfRaleway);
        TextView tvTitle = (TextView)findViewById(R.id.tv_newcard_title);
        tvTitle.setTypeface(tfRaleway);
        TextView tvCompany = (TextView)findViewById(R.id.tv_newcard_company);
        tvCompany.setTypeface(tfRaleway);
        TextView tvPhone = (TextView)findViewById(R.id.tv_newcard_phone);
        tvPhone.setTypeface(tfRaleway);
        TextView tvEmail = (TextView)findViewById(R.id.tv_newcard_email);
        tvEmail.setTypeface(tfRaleway);

        Typeface tfRalewayBold = Typeface.createFromAsset(getAssets(), "fonts/Raleway-Bold.ttf");
        TextView tvLogo = (TextView)findViewById(R.id.tv_newcard_logo);
        tvLogo.setTypeface(tfRalewayBold);
    }


    private void openFileChooser(){

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            mImageUri = data.getData();

            Picasso.with(this).load(mImageUri).into(ivNewCardImage);
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    //Upload card to firebase
    private void uploadFile(){
        if (mImageUri != null){
            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(mImageUri));

            mUploadTask = fileReference.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //mProgressBar.setProgress(0);
                        }
                    }, 500);

                    Toast.makeText(NewCardActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
                    Card newCard = new Card();
                    newCard.setImgURL(taskSnapshot.getDownloadUrl().toString());
                    newCard.setName(tvNewCardName.getText().toString().trim());
                    newCard.setTitle(tvNewCardTitle.getText().toString().trim());
                    newCard.setCompany(tvNewCardCompany.getText().toString().trim());
                    newCard.setPhoneNumber(tvNewCardPhone.getText().toString().trim());
                    newCard.setEmailAddress(tvNewCardEmail.getText().toString().trim());
                    newCard.setFaxAddress(tvNewCardFaxAddress.getText().toString().trim());
                    Log.i("street address: ", tvNewCardStreetAddress.getText().toString());
                    newCard.setStreetAddress(tvNewCardStreetAddress.getText().toString().trim());
                    newCard.setWebsiteAddress(tvNewCardWebsiteAddress.getText().toString().trim());


                    mDatabaseRef.child("users").child(mAuth.getCurrentUser().getUid()).child("cards").push().setValue(newCard);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i("TESTY: ", "In here");
                    Toast.makeText(NewCardActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    //mProgressBar.setProgress((int) progress);
                }
            });
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }
}
