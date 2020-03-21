package com.example.prepapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.prepapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class createuserFragment extends Fragment {

    private static final String TAG = "createUser Fragment";

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore database;

    private EditText usernameField;
    private EditText passwordField1;
    private EditText passwordField2;

    public createuserFragment() {
        // Required empty public constructor
    }

    public createuserFragment(FirebaseUser newUser, FirebaseAuth auth) {
        // Required empty public constructor
        mAuth = auth;
        user = newUser;

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_createuser, container, false);
        Button createBtn = view.findViewById(R.id.createAccount_createAccount_btn);
        usernameField = view.findViewById(R.id.createAccount_username);
        passwordField1 = view.findViewById(R.id.createAccount_password);
        passwordField2 = view.findViewById(R.id.createAccount_passwordconfirmation);

        database = FirebaseFirestore.getInstance();

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(passwordField1.getText().toString().equals(passwordField2.getText().toString())){
                    createUser(usernameField.getText().toString(),passwordField1.getText().toString());
                }else{
                    passwordField2.setError("Passwords do not match.");
                }
            }
        });

        return view;
    }

    private void createUser(final String username,final String password) {

        if(username.length()<5){
            usernameField.setError("Invalid");
            return;
        }

        if(password.length() < 6 ){
            passwordField1.setError("Password must be at least 6 letters long.");
            return;
        }

        mAuth.createUserWithEmailAndPassword(username, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        String message;
                        if (task.isSuccessful()) {
                            User user = new User();
                            user.setEmail(username);
                            user.setUsername(username.substring(0, username.indexOf("@")));
                            user.setId(FirebaseAuth.getInstance().getUid());

                            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
                            database.setFirestoreSettings(settings);

                            DocumentReference newUserRef = database.collection("Users").document(FirebaseAuth.getInstance().getUid());

                            newUserRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        Log.d(TAG, "redirectLoginScreen: redirecting to login screen.");
                                        //assume user is logged in after creating account
                                        Intent intent = new Intent(getContext(), userProfile.class);
                                        startActivity(intent);
                                    }else{
                                        Log.d(TAG, "adding new user to firestore was a failure");
                                    }
                                }
                            });

                            message = "success createUserWithEmailAndPassword";
                        } else {
                            message = "fail createUserWithEmailAndPassword";
                        }
                        Log.d(TAG, message);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            //    mPasswordTextView.setText(e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
