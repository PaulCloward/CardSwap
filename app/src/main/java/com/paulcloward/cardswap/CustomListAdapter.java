package com.paulcloward.cardswap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 4/4/2017.
 */

public class CustomListAdapter extends ArrayAdapter<Card> {

    private static final String TAG = "CustomListAdapter";

    private Context mContext;
    private int mResource;
    private int mResourceList;
    private int lastPosition = -1;
    private ArrayList<Card> cardUploads;

    private FirebaseStorage mStorage;
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth mAuth;


    /**
     * Holds variables in a View
     */
    private static class ViewHolder {
        TextView name;
        TextView title;
        TextView company;
        ImageView image;
        ProgressBar dialog;
    }

    /**
     * Default constructor for the PersonListAdapter
     *
     * @param context
     * @param resource
     * @param objects
     */
    public CustomListAdapter(Context context, int resource, ArrayList<Card> objects, int resourceList) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
        mResourceList = resourceList;
        cardUploads = objects;
        mStorage = FirebaseStorage.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        //sets up the image loader library
        setupImageLoader();
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {


        //get the persons information
        String name = getItem(position).getName();
        String title = getItem(position).getTitle();
        String company = getItem(position).getCompany();
        String imgUrl = getItem(position).getImgURL();


        try {
            //create the view result for showing the animation
            final View result;

            //ViewHolder object
            final ViewHolder holder;

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(mResource, parent, false);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.cardName);
                holder.title = (TextView) convertView.findViewById(R.id.cardTitle);
                holder.company = (TextView) convertView.findViewById(R.id.cardCompany);
                holder.image = (ImageView) convertView.findViewById(R.id.cardImage);
                holder.dialog = (ProgressBar) convertView.findViewById(R.id.cardProgressBar);

                result = convertView;

                //Listener for when you click the card
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent;
                        if(mResourceList == R.id.listView) {
                            intent = new Intent(getContext(), ViewCardActivity.class);
                        } else {
                            intent = new Intent(getContext(), ViewFriendCardActivity.class);
                        }
                        intent.putExtra("USER_ID",cardUploads.get(position).getUserKey());
                        intent.putExtra("CARD_ID",cardUploads.get(position).getKey());
                        mContext.startActivity(intent);
                    }
                });

                ImageView ivShare = (ImageView)convertView.findViewById(R.id.iv_card_share);

                //Popup screen for share click on card
                ivShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickCardShare();
                    }
                });

                ImageView ivThreeDot = (ImageView) convertView.findViewById(R.id.iv_card_three_dot);

                //popup code for delete card or make invisible
                ivThreeDot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        PopupMenu popup = new PopupMenu(getContext(), v);
                        popup.setGravity(Gravity.END);//Makes Popup go left
                        Menu m = popup.getMenu();
                        MenuInflater inflater = popup.getMenuInflater();
                        inflater.inflate(R.menu.card_threedot_popup, popup.getMenu());


                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                            @Override
                            public boolean onMenuItemClick(MenuItem item) {

                                if (item.getItemId() == R.id.make_invisible) {
                                    Log.i("Popup: ", "make invisible");
                                } else if (item.getItemId() == R.id.delete_card) {

                                    final Card selectedCard = cardUploads.get(position);
                                    Log.i("Delete Key: ", selectedCard.getKey());
                                    StorageReference imageRef = mStorage.getReferenceFromUrl(selectedCard.getImgURL());
                                    imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mDatabaseRef.child("users").child(mAuth.getCurrentUser().getUid())
                                                    .child("cards").child(selectedCard.getKey()).removeValue();
                                            Toast.makeText(getContext(), "Selected Card Deleted", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                return true;
                            }
                        });

                        popup.show();
                    }


                });

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
                result = convertView;
            }


           /* Animation animation = AnimationUtils.loadAnimation(mContext,
                    (position > lastPosition) ? R.anim.load_down_anim : R.anim.load_up_anim);
            result.startAnimation(animation);*/
            lastPosition = position;

            holder.name.setText(name);
            holder.title.setText(title);
            holder.company.setText(company);
            //create the imageloader object
            ImageLoader imageLoader = ImageLoader.getInstance();

            int defaultImage = mContext.getResources().getIdentifier("@drawable/image_failed", null, mContext.getPackageName());

            //create display options
            DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                    .cacheOnDisc(true).resetViewBeforeLoading(true)
                    .showImageForEmptyUri(defaultImage)
                    .showImageOnFail(defaultImage)
                    .showImageOnLoading(defaultImage).build();

            //download and display image from url
            imageLoader.displayImage(imgUrl, holder.image, options, new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            holder.dialog.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            holder.dialog.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            holder.dialog.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {

                        }
                    }
            );

            return convertView;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "getView: IllegalArgumentException: " + e.getMessage());
            return convertView;
        }

    }

    /**
     * Required for setting up the Universal Image loader Library
     */
    private void setupImageLoader() {
        // UNIVERSAL IMAGE LOADER SETUP
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                mContext)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100 * 1024 * 1024).build();

        ImageLoader.getInstance().init(config);
        // END - UNIVERSAL IMAGE LOADER SETUP
    }

    private void onClickCardShare(){
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext());
        View mView = inflater.inflate(R.layout.popup_share_card, null);

        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();

        ImageView mClose = (ImageView)mView.findViewById(R.id.iv_popup_share_close);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    dialog.dismiss();
            }
        });
        dialog.show();


       /* Typeface tfRalewayMedium = Typeface.createFromAsset(getAssets(), "fonts/Raleway-Medium.ttf");
        TextView tvEmail = (TextView) mView.findViewById(R.id.tv_popup_email_title);
        tvEmail.setTypeface(tfRalewayMedium);*/


    }
}