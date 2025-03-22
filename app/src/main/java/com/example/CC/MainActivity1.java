package com.example.CC;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity1 extends AppCompatActivity {

//    ActivityMain1Binding binding;
    Button mUpdateBtn,mSearchBtn,mOfferBtn;
    ImageButton mHome,mRide,mOffer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
//        binding = ActivityMain1Binding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_main1);

        // Initialize Firebase Auth
//        mAuth = FirebaseAuth.getInstance();
//        currentUser = mAuth.getCurrentUser();

//        FirebaseAuth.getInstance().getFirebaseAuthSettings().setAppVerificationDisabledForTesting(false);

        // Ensure the user session persists
//        FirebaseAuth.getInstance().setPersistence(FirebaseAuth.AuthPersistence.LOCAL);

        // Check if user is logged in
//        if (currentUser == null) {
//            Log.w("FirebaseAuth", "Session expired. Redirecting to login.");
//            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
//            startActivity(new Intent(MainActivity1.this, Login.class));
//            finish();
//            return;
//        }else {
//            Log.d("FirebaseAuth", "User is logged in: " + currentUser.getEmail());
//        }

        mUpdateBtn = findViewById(R.id.upadateProfileBtn);
        mSearchBtn = findViewById(R.id.searchRideBtn);
        mOfferBtn = findViewById(R.id.offerRideBtn);

        mHome = findViewById(R.id.homeButton);
        mRide = findViewById(R.id.rideButton);
        mOffer = findViewById(R.id.offerButton);


        mUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),search.class));
            }
        });
        mOfferBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),offer.class));
            }
        });

        mHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),MainActivity1.class));
            }
        });

        mRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),rideDetails.class));
            }
        });

        mOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),offerDetails.class));
            }
        });

    }

    public void logout(View view){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();
    }
}