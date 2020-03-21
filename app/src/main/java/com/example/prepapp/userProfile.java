package com.example.prepapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class userProfile extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "userProfile";
    //google map
    private MapView mMapView;
    private GoogleMap mMap;
    //holds client location
    private FusedLocationProviderClient  flClient;

    //database stuff
    private FirebaseFirestore database;
    private FirebaseUser fbUser;
    private FirebaseAuth mAuth;
    private DocumentReference userRef;

    private User user;
    private GeoPoint userLocation;
    private Request request;
    private TextView userLabel;
    private ArrayList<Request> requestList = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        mAuth = FirebaseAuth.getInstance();

        fbUser = mAuth.getCurrentUser();
        database = FirebaseFirestore.getInstance();
        userRef = database.collection("Users").document(fbUser.getUid());
        flClient = LocationServices.getFusedLocationProviderClient(this);

        userLabel = findViewById(R.id.user_profilepage);


        //load in user's info from firebase
        loadUser();
        Log.d(TAG,"user is " + user.getUsername());



        //map init
        mMapView = findViewById(R.id.user_map);
        initGoogleMap(savedInstanceState);

        //populate requestList
        retrieveRequests();

        //button inits and functions
        Button requestBtn =  findViewById(R.id.requestAid_btn);
        Button signoutBtn =  findViewById(R.id.signout_btn);
        signoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        requestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserRequest();
            }
        });


    }

    //function to retrieve all requests from database
    private void retrieveRequests(){
        CollectionReference requestRef = database.collection("Requests");

        //will get live updates from server for new help messages.
        requestRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                List<Request> list = value.toObjects(Request.class);
                requestList.addAll(list);
                Log.d(TAG, "Retrieving all request onSuccess: " + requestList.toString());
                populateMapRequests();
            }
        });

    }
    
    //used to populate google map with individuals requests
    private void populateMapRequests(){
        if(requestList == null) return;
        //iterate through arraylist and drop markers on map with relevant information provided by request object
        for (Request req:requestList ) {
            addRequestToMap(req);
        }
    }

    private void addRequestToMap(Request req){
        LatLng marklocation = new LatLng(req.getGeoPoint().getLatitude(),req.getGeoPoint().getLongitude());
        String title = req.getRequestCreator().getUsername() + " asks: " + req.getMessage();
        MarkerOptions options = new MarkerOptions().position(marklocation).title(title);
        mMap.addMarker(options);
    }

    //singnouts user out and naviagtes them back to main menu
    private void signOut() {
        if (mAuth != null) {
            mAuth.signOut();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    //create pop-up to get user request text info
    private void getUserRequest(){
        // get prompts.xml view

        LayoutInflater li = LayoutInflater.from(this);
        final View reqView = li.inflate(R.layout.get_request, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Send Request for Help");
        builder.setView(reqView);

        builder.setPositiveButton(R.string.send_request, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText message = reqView.findViewById(R.id.request_popup_editext);
                request.setMessage(message.getText().toString());

                //adds request to database.
                addRequestToServer();
                Log.d(TAG,"added to request: message "+message.getText().toString());
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    //upload request to server
    private void addRequestToServer(){
        if(request == null) return;

        DocumentReference requestRef = database.collection("Requests").document(fbUser.getUid());

        requestRef.set(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    addRequestToMap(request);
                    Log.d(TAG,"Request added to database:" + request.toString());
                }else{
                    Log.d(TAG,"FAILED adding Request to database:");
                }
            }
        });
    }


    //Gets users information from database
    private void loadUser(){
        user = new User();
        request = new Request();
        request.setRequestCreator(user);

        //init user and their location;
        getUserLocation();
        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    String email = documentSnapshot.getString("email");
                    String username = documentSnapshot.getString("username");
                    Log.d(TAG,"user loaded good, " + username);
                    user.setUsername(username);
                    user.setEmail(email);
                    user.setId(fbUser.getUid());

                    userLabel.setText(getString(R.string.greeting) + user.getUsername());
                }
            }
        });
    }


    public void getUserLocation(){
        flClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()){
                    Location location = task.getResult();
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(),location.getLongitude());
                    userLocation = geoPoint;
                    request.setGeoPoint(userLocation);
                    Log.d(TAG,"getlocation:complete Latituted:" + geoPoint.getLatitude() + " Longitude" + geoPoint.getLongitude());
                    //once user location is parsed, move map to center on them
                    setMapViewBoundary();
                }
            }
        });

    }


    private void initGoogleMap(Bundle savedInstanceState){
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle("MapViewBundleKey");
        }

        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
        mMap = map;

    }

    private void setMapViewBoundary(){
        LatLngBounds mapBound = new LatLngBounds(
                new LatLng(userLocation.getLatitude() - 0.1, userLocation.getLongitude() -0.1),
                new LatLng(userLocation.getLatitude() + 0.1, userLocation.getLongitude() +0.1)
        );
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBound,0));
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle("MapViewBundleKey");
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle("MapViewBundleKey", mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}
