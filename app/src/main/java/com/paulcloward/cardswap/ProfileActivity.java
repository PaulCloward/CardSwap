package com.paulcloward.cardswap;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActionBarDrawerToggle mToggle;
    private DrawerLayout mDrawerLayout;
    private TabHost tabHost;
    private ListView listView;
    private ListView listViewNearby;
    private CustomListAdapter adapter;
    private CustomListAdapter adapterNearby;
    private ArrayList<Card> list;
    private ArrayList<Card> listNearby;

    //Firebase stuff
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseStorage mStorage;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Sets status bar color to match background of page
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        //declare the database reference object. This is what we use to access the database.
        //NOTE: unless you are signed in, this will not be useable.
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        rootRef = mFirebaseDatabase.getReference();
        mStorage = FirebaseStorage.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    //.d("Firebase Auth: ", "onAuthStateChanged:signed_in:" + user.getUid());
                    //toastMessage("Successfully signed in with: " + user.getEmail());

                } else {
                    // User is signed out
                    //Log.d("Firebase Auth: ", "onAuthStateChanged:signed_out");
                    //toastMessage("Successfully signed out");
                }
            }
        };

        rootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                list.clear();//Stops firebase from adding the cards twice in view
                listNearby.clear();
                showData(dataSnapshot);
                showNearbyData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Toolbar myToolbar = (Toolbar)findViewById(R.id.toolbar);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);

        mDrawerLayout=(DrawerLayout) findViewById(R.id.drawer);
        mToggle=new ActionBarDrawerToggle(this,mDrawerLayout,R.string.Open, R.string.Close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        NavigationView navigationView = (NavigationView)findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        myToolbar.setNavigationIcon(R.drawable.ic_menu);

        initializeProfileTabs();

        FloatingActionButton fabAddCard = (FloatingActionButton)findViewById(R.id.floating_action_button_add_card);
        fabAddCard.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, NewCardActivity.class));
            }
        });

        list = new ArrayList<>();
        listView = findViewById(R.id.listView);
        adapter = new CustomListAdapter(this, R.layout.card_layout_main, list, R.id.listView);
        listView.setAdapter(adapter);

        listNearby = new ArrayList<>();
        listViewNearby = findViewById(R.id.listViewNearby);
        adapterNearby = new CustomListAdapter(this, R.layout.card_layout_main, listNearby, R.id.listViewNearby);
        listViewNearby.setAdapter(adapterNearby);
    }

    private void initializeProfileTabs(){

        tabHost = (TabHost)findViewById(R.id.tabHost);
        tabHost.setup();

        TabHost.TabSpec spec = tabHost.newTabSpec("My Cards");
        spec.setContent(R.id.tab1);
        spec.setIndicator("My Cards");
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec("Nearby");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Nearby");

        tabHost.addTab(spec);

        spec = tabHost.newTabSpec("Received");

        spec.setContent(R.id.tab3);
        spec.setIndicator("Received");
        tabHost.addTab(spec);

        //initialize tabs text color
        for(int i=0;i<tabHost.getTabWidget().getChildCount();i++)
        {
            TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(Color.GRAY);
            tv.setAllCaps(false);

            //Change the height of the tabs
            tabHost.getTabWidget().getChildAt(i).getLayoutParams().height = 125;
        }

        tabHost.getTabWidget().getChildAt(0).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.tab_profile_background_selected));
        TextView tvFirstTab = (TextView)tabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title);
        tvFirstTab.setTextColor(Color.BLACK);

        tabHost.getTabWidget().getChildAt(1).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.tab_profile_background_unselected));
        tabHost.getTabWidget().getChildAt(2).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.tab_profile_background_unselected));

        //Set Tab Typeface and size
        TextView tv = (TextView) tabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title);
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/Raleway-Regular.ttf");
        tv.setTypeface(face);
        tv.setTextSize(16);
        tv = (TextView) tabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title);
        tv.setTypeface(face);
        tv.setTextSize(16);
        tv = (TextView) tabHost.getTabWidget().getChildAt(2).findViewById(android.R.id.title);
        tv.setTypeface(face);
        tv.setTextSize(16);

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener(){
            @Override
            public void onTabChanged(String s){

                //Listen to which tab is selected
                if(s.equals("My Cards")) {

                    tabHost.getTabWidget().getChildAt(0).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.tab_profile_background_selected));
                    TextView tv = (TextView) tabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title);
                    tv.setTextColor(Color.BLACK);

                    tabHost.getTabWidget().getChildAt(1)
                            .setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.tab_profile_background_unselected));
                    tv = (TextView) tabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title);
                    tv.setTextColor(Color.GRAY);

                    tabHost.getTabWidget().getChildAt(2)
                            .setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.tab_profile_background_unselected));
                    tv = (TextView) tabHost.getTabWidget().getChildAt(2).findViewById(android.R.id.title);
                    tv.setTextColor(Color.GRAY);


                }
                else if (s.equals("Nearby")) {


                    tabHost.getTabWidget().getChildAt(0)
                            .setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.tab_profile_background_unselected));
                    TextView tv = (TextView) tabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title);
                    tv.setTextColor(Color.GRAY);

                    tabHost.getTabWidget().getChildAt(1).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.tab_profile_background_selected));
                    tv = (TextView) tabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title);
                    tv.setTextColor(Color.BLACK);

                    tabHost.getTabWidget().getChildAt(2)
                            .setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.tab_profile_background_unselected));
                    tv = (TextView) tabHost.getTabWidget().getChildAt(2).findViewById(android.R.id.title);
                    tv.setTextColor(Color.GRAY);

                }
                else if (s.equals("Received")){

                    tabHost.getTabWidget().getChildAt(0)
                            .setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.tab_profile_background_unselected));
                    TextView tv = (TextView) tabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title);
                    tv.setTextColor(Color.GRAY);

                    tabHost.getTabWidget().getChildAt(1).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.tab_profile_background_unselected));
                    tv = (TextView) tabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title);
                    tv.setTextColor(Color.GRAY);

                    tabHost.getTabWidget().getChildAt(2)
                            .setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.tab_profile_background_selected));
                    tv = (TextView) tabHost.getTabWidget().getChildAt(2).findViewById(android.R.id.title);
                    tv.setTextColor(Color.BLACK);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mToggle.onOptionsItemSelected(item)){

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.menu_title){

            Log.i("Menu Title: "," CardSwap");
        }
        if(id == R.id.feedback){

            Log.i("Feedback: "," Feedback");
        }
        if(id == R.id.settings){

            Log.i("Settings: "," Settings");
        }
        if(id == R.id.contactus){

            Log.i("Contact Us: "," Contact Us");
        }

        if(id == R.id.signout){

            Log.i("Signout: "," signout");
            mAuth.signOut();
            toastMessage("Successfully signed out");
            startActivity(new Intent(this, MainActivity.class));
        }
        return false;
    }

    public void showData(DataSnapshot dataSnapshot){
        for(DataSnapshot ds : dataSnapshot.getChildren()){

            for(DataSnapshot dsChildren : ds.child(userID).child("cards").getChildren()){

                Card card = new Card();
                card.setUserKey(userID);
                card.setKey(dsChildren.getKey());
                card.setImgURL(dsChildren.getValue(Card.class).getImgURL());
                card.setName(dsChildren.getValue(Card.class).getName());
                card.setTitle(dsChildren.getValue(Card.class).getTitle());
                card.setCompany(dsChildren.getValue(Card.class).getCompany());
                card.setPhoneNumber(dsChildren.getValue(Card.class).getPhoneNumber());
                card.setEmailAddress(dsChildren.getValue(Card.class).getEmailAddress());
                card.setStreetAddress(dsChildren.getValue(Card.class).getStreetAddress());
                card.setWebsiteAddress(dsChildren.getValue(Card.class).getWebsiteAddress());
                card.setFaxAddress(dsChildren.getValue(Card.class).getFaxAddress());
                list.add(card);
            }

            adapter.notifyDataSetChanged();
        }
    }

    public void showNearbyData(DataSnapshot dataSnapshot){
        for(DataSnapshot ds : dataSnapshot.getChildren()){

            for(DataSnapshot dsChildren : ds.getChildren()){

                if(!dsChildren.getKey().toString().trim().equals(userID.toString().trim())) {

                    for(DataSnapshot dsGrandchildren: dsChildren.child("cards").getChildren()) {
                        Card card = new Card();
                        card.setUserKey(dsChildren.getKey().toString().trim());
                        card.setKey(dsGrandchildren.getKey());
                        card.setImgURL(dsGrandchildren.getValue(Card.class).getImgURL());
                        card.setName(dsGrandchildren.getValue(Card.class).getName());
                        card.setTitle(dsGrandchildren.getValue(Card.class).getTitle());
                        card.setCompany(dsGrandchildren.getValue(Card.class).getCompany());
                        card.setPhoneNumber(dsGrandchildren.getValue(Card.class).getPhoneNumber());
                        card.setEmailAddress(dsGrandchildren.getValue(Card.class).getEmailAddress());
                        card.setStreetAddress(dsGrandchildren.getValue(Card.class).getStreetAddress());
                        card.setWebsiteAddress(dsGrandchildren.getValue(Card.class).getWebsiteAddress());
                        card.setFaxAddress(dsGrandchildren.getValue(Card.class).getFaxAddress());
                        listNearby.add(card);
                    }
                }
            }
            adapterNearby.notifyDataSetChanged();
        }
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

    /*
    * customizable toast
    * @param message
    * */
    private void toastMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
