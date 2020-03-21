package com.example.prepapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private FirebaseUser user;
    private loginFragment loginFrag;
    private Button loginBtn;
    private Button createBtn;
    private ImageView imageView;

    private createuserFragment createUserFrag;
    private FrameLayout fragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //activate connection with firebase database
        FirebaseApp.initializeApp(this);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        loginFrag =  new loginFragment(user,mAuth);
        createUserFrag = new createuserFragment(user,mAuth);

        loginBtn = findViewById(R.id.signin_btn);
        createBtn = findViewById(R.id.createAccount_btn);
      //  imageView = findViewById(R.id.imgview_mainlogo);

        getPermission();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginFragment();
                hideBtns();
            }
        });

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCreateFragment();
                hideBtns();
            }
        });

    }

    private void openLoginFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.addToBackStack(null);
        transaction.add(R.id.fragment_container, loginFrag, "Login_FRAGMENT").commit();
    }

    private void openCreateFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.addToBackStack(null);
        transaction.add(R.id.fragment_container, createUserFrag, "CreateUser_FRAGMENT").commit();
    }

    private void hideBtns(){
        loginBtn.setVisibility(View.GONE);
        createBtn.setVisibility(View.GONE);
     //   imageView.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        uiCheck(user);
    }

    private void uiCheck(FirebaseUser user){
        //check if user is logged in, if so continue to their profile.
        if(user != null){
            Intent intent = new Intent(this, userProfile.class);
            startActivity(intent);
        }
    }

    //hacky way of ensuring users can't click sign in button from main activity inside fragment.
    //clickable tag and background setting are not blocking it so this will have to do for now.
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        loginBtn.setVisibility(View.VISIBLE);
        createBtn.setVisibility(View.VISIBLE);
        //imageView.setVisibility(View.VISIBLE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
       // if(loginBtn != null) loginBtn.setVisibility(View.VISIBLE);
        return super.onCreateView(name, context, attrs);
    }

    private void getPermission(){
        //Request location permission, so that we can get the location of the
        // device.
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},9001);
        }

    }

}
