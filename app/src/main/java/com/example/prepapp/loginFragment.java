package com.example.prepapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.Executor;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link loginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class loginFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private static final String TAG = "loginFragment";
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private EditText userEdittxt ;
    private EditText passEdittxt ;


    public loginFragment() {
        // Required empty public constructor
    }

    public loginFragment(FirebaseUser newUser, FirebaseAuth auth) {
        // Required empty public constructor
        mAuth = auth;
        user = newUser;
    }


    // TODO: Rename and change types and number of parameters
    public static loginFragment newInstance(String param1, String param2) {
        loginFragment fragment = new loginFragment();
        Bundle args = new Bundle();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);
       // v.setClickable(true);
        Button signinBtn = v.findViewById(R.id.signin_loginpage_btn);
        userEdittxt = v.findViewById(R.id.login_username);
        passEdittxt = v.findViewById(R.id.login_password);

        signinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "user =" + userEdittxt);
               login(userEdittxt.getText().toString(),passEdittxt.getText().toString());
            }
        });

        return v;
    }

    private void login(String username,String password) {

        if (username == null || username.isEmpty()) return;
        if (password == null || password.isEmpty()) return;
        Log.d(TAG, "Starting login with user name " +username +" "+" and pass "+ password);

        mAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener((Activity) getContext(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Do your task in success
                    Log.d(TAG, "SUCCESS");
                    Intent intent = new Intent(getContext(), userProfile.class);
                    startActivity(intent);
                } else {
                    // Do your task in failure
                    Log.d(TAG, "FAIL");
                }
            }

            ;


        });

    }


}
