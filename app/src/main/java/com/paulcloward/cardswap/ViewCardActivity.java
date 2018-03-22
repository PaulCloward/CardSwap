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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import org.w3c.dom.Text;

public class ViewCardActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private String userID;
    private String cardID;

    private DatabaseReference mDatabaseRef;
    private FirebaseDatabase mFirebaseDatabase;
    private StorageReference mStorageRef;
    private StorageTask mUploadTask;

    private ImageView ivImage;
    private String imagePath;
    private TextView tvViewCardName;
    private TextView tvViewCardTitle;
    private TextView tvViewCardCompany;
    private TextView tvViewCardPhoneNumber;
    private TextView tvViewCardEmailAddress;
    private TextView tvViewCardFaxAddress;
    private TextView tvViewCardWebsiteAddress;
    private TextView tvViewCardStreetAddress;

    private Uri mImageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_card);

        //Sets status bar color to match background of page
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseRef = mFirebaseDatabase.getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");

        if(!getIntent().getStringExtra("USER_ID").equals(null)){

            userID = getIntent().getStringExtra("USER_ID");
            cardID = getIntent().getStringExtra("CARD_ID");
            ivImage = findViewById(R.id.iv_viewcard_image);

            mDatabaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    Card displayCard = dataSnapshot.child("users").child(userID).child("cards").child(cardID).getValue(Card.class);
                    Picasso.with(ViewCardActivity.this).load(displayCard.getImgURL()).into(ivImage);

                    imagePath = displayCard.getImgURL();
                    tvViewCardName = findViewById(R.id.tv_viewcard_name);
                    tvViewCardName.setText(displayCard.getName());
                    tvViewCardTitle = findViewById(R.id.tv_viewcard_title);
                    tvViewCardTitle.setText(displayCard.getTitle());
                    tvViewCardCompany = findViewById(R.id.tv_viewcard_company);
                    tvViewCardCompany.setText(displayCard.getCompany());
                    tvViewCardPhoneNumber = findViewById(R.id.tv_viewcard_phone);
                    tvViewCardPhoneNumber.setText(displayCard.getPhoneNumber());
                    tvViewCardEmailAddress = findViewById(R.id.tv_viewcard_email);
                    tvViewCardEmailAddress.setText(displayCard.getEmailAddress());
                    tvViewCardFaxAddress = findViewById(R.id.tv_viewcard_fax_address);
                    tvViewCardFaxAddress.setText(displayCard.getFaxAddress());
                    tvViewCardStreetAddress = findViewById(R.id.tv_viewcard_street_address);
                    tvViewCardStreetAddress.setText(displayCard.getStreetAddress());
                    tvViewCardWebsiteAddress = findViewById(R.id.tv_viewcard_website_address);
                    tvViewCardWebsiteAddress.setText(displayCard.getWebsiteAddress());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        //Onclick checkmark (save edits)
        ImageView ivCheckmark = findViewById(R.id.iv_viewcard_toolbar_save);
        ivCheckmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEditCard();//Save edits to card before going back
                startActivity(new Intent(ViewCardActivity.this, ProfileActivity.class));
            }
        });

        //Go back to last page
        ImageView ivBack = findViewById(R.id.iv_viewcard_toolbar_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ViewCardActivity.this, ProfileActivity.class));
            }
        });

        //listen for image click
        ivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        setTypeface();
    }

    //Setup Raleway font family through page
    private void setTypeface(){

        Typeface tfRaleway = Typeface.createFromAsset(getAssets(), "fonts/Raleway-Regular.ttf");
        TextView tvToolbarTitle = (TextView)findViewById(R.id.tv_viewcard_toolbar_title);
        TextView tvName = findViewById(R.id.tv_viewcard_name);
        tvName.setTypeface(tfRaleway);
        TextView tvTitle = findViewById(R.id.tv_viewcard_title);
        tvTitle.setTypeface(tfRaleway);
        TextView tvCompany = findViewById(R.id.tv_viewcard_company);
        tvCompany.setTypeface(tfRaleway);
        tvToolbarTitle.setTypeface(tfRaleway);
        TextView tvPhone = (TextView)findViewById(R.id.tv_viewcard_phone);
        tvPhone.setTypeface(tfRaleway);
        TextView tvEmail = (TextView)findViewById(R.id.tv_viewcard_email);
        tvEmail.setTypeface(tfRaleway);
        TextView tvStreetAddress = (TextView)findViewById(R.id.tv_viewcard_street_address);
        tvStreetAddress.setTypeface(tfRaleway);
        TextView tvWebsite = (TextView)findViewById(R.id.tv_viewcard_website_address);
        tvWebsite.setTypeface(tfRaleway);
    }

    //Save all the changes to the card and overwrite preexisiting card in firebase
    private void saveEditCard(){

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
                        StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(mImageUri));
                        Card updatedCard = new Card();
                        updatedCard.setImgURL(taskSnapshot.getDownloadUrl().toString());
                        updatedCard.setName(tvViewCardName.getText().toString().trim());
                        updatedCard.setTitle(tvViewCardTitle.getText().toString().trim());
                        updatedCard.setCompany(tvViewCardCompany.getText().toString().trim());
                        updatedCard.setPhoneNumber(tvViewCardPhoneNumber.getText().toString().trim());
                        updatedCard.setEmailAddress(tvViewCardEmailAddress.getText().toString().trim());
                        updatedCard.setFaxAddress(tvViewCardFaxAddress.getText().toString().trim());
                        updatedCard.setStreetAddress(tvViewCardStreetAddress.getText().toString().trim());
                        updatedCard.setWebsiteAddress(tvViewCardWebsiteAddress.getText().toString().trim());

                        mDatabaseRef.child("users").child(userID).child("cards").child(cardID).setValue(updatedCard);
                                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ViewCardActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    //mProgressBar.setProgress((int) progress);
                }
            });
        } else {
            Card updatedCard = new Card();
            updatedCard.setImgURL(imagePath);
            updatedCard.setName(tvViewCardName.getText().toString().trim());
            updatedCard.setTitle(tvViewCardTitle.getText().toString().trim());
            updatedCard.setCompany(tvViewCardCompany.getText().toString().trim());
            updatedCard.setPhoneNumber(tvViewCardPhoneNumber.getText().toString().trim());
            updatedCard.setEmailAddress(tvViewCardEmailAddress.getText().toString().trim());
            updatedCard.setFaxAddress(tvViewCardFaxAddress.getText().toString().trim());
            updatedCard.setStreetAddress(tvViewCardStreetAddress.getText().toString().trim());
            updatedCard.setWebsiteAddress(tvViewCardWebsiteAddress.getText().toString().trim());

            mDatabaseRef.child("users").child(userID).child("cards").child(cardID).setValue(updatedCard);
        }
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

            Picasso.with(this).load(mImageUri).into(ivImage);
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    /*
   * onClickEmail(View v)
   *
   * Creates popup to allow user to edit an email address
   * Also sets the Typeface of the text
   * */
    public void onClickEmail(View v){

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(ViewCardActivity.this);
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

                    tvViewCardEmailAddress.setText(mEmail.getText().toString().trim());
                    dialog.dismiss();


                } else{
                    Toast.makeText(ViewCardActivity.this, "Please fill in email address field", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    /*
    * onClickPhone(View v)
    *
    * Creates popup to allow user to edit a phone number
    * Also sets the Typeface of the text
    * */
    public void onClickPhone(View v){

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(ViewCardActivity.this);
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

                    tvViewCardPhoneNumber.setText(mPhone.getText().toString().trim());
                    dialog.dismiss();

                } else{
                    Toast.makeText(ViewCardActivity.this, "Please fill in phone number field", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /*
    * onClickCompanyInfo(View v)
    *
    * Creates popup to allow user to edit name, company position, company name
    * Also sets the Typeface of the text
    * */
    public void onClickCompanyInformation(View v){

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(ViewCardActivity.this);
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

                    tvViewCardName.setText(mName.getText().toString().trim());
                    tvViewCardTitle.setText(mTitle.getText().toString().trim());
                    tvViewCardCompany.setText(mCompany.getText().toString().trim());

                    dialog.dismiss();

                } else{
                    Toast.makeText(ViewCardActivity.this, "Please fill in empty fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /*
    * onClickContactInfo(View v)
    *
    * Creates popup to allow user to edit contact information
    * Also sets the Typeface of the text
    * */
    public void onClickContactInformation(View v){

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(ViewCardActivity.this);
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
        ivCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!mFax.getText().toString().isEmpty()){
                    tvViewCardFaxAddress.setText(mFax.getText().toString().trim());
                }
                if(!mStreetAddress.getText().toString().isEmpty()){
                    tvViewCardStreetAddress.setText(mStreetAddress.getText().toString().trim());
                }
                if(!mWebsite.getText().toString().isEmpty()){
                    tvViewCardWebsiteAddress.setText(mWebsite.getText().toString().trim());
                    dialog.dismiss();
                }
            }
        });
    }
}
