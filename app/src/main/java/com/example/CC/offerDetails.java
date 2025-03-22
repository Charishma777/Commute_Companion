package com.example.CC;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class offerDetails extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RideOfferAdapter rideAdapter;
    private List<RideModel> rideList;
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private EditText editSeats;
    private Button btnUpdateSeats;
    private String userId;
    private DocumentReference offerRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_offer_details);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        editSeats = findViewById(R.id.editSeats);
        btnUpdateSeats = findViewById(R.id.btnUpdateSeats);

        rideList = new ArrayList<>();
        rideAdapter = new RideOfferAdapter(rideList);
        recyclerView.setAdapter(rideAdapter);

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = fAuth.getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
            fetchRideDetails();
        }

        btnUpdateSeats.setOnClickListener(v -> updateSeatCount());

    }

    private void fetchRideDetails() {
        FirebaseUser currentUser = fAuth.getCurrentUser();
        if (currentUser == null) {
            // 🔹 If user is logged out, load from SharedPreferences
            SharedPreferences prefs = getSharedPreferences("OfferDetailsPrefs", MODE_PRIVATE);
            String source = prefs.getString("source", "N/A");
            String destination = prefs.getString("destination", "N/A");
            String date = prefs.getString("date", "N/A");
            String time = prefs.getString("time", "N/A");
            String carNo = prefs.getString("carNo", "N/A");
            String phone = prefs.getString("phoneNo", "N/A");
            String noOfSeats = prefs.getString("noOfSeats", "N/A");
            String basePrice = prefs.getString("basePrice", "N/A");
            String carType = prefs.getString("carType","N/A");

            // Add stored ride details to the list
            if (!source.equals("N/A")) {
                rideList.add(new RideModel(source, destination, date, time, carNo, phone, noOfSeats, basePrice, carType));
                rideAdapter.notifyDataSetChanged();
            }
            return;
        }
        String userId = currentUser.getUid();

        fStore.collection("offer").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                rideList.clear();
                String source = documentSnapshot.getString("source");
                String destination = documentSnapshot.getString("destination");
                String date = documentSnapshot.getString("date");
                String time = documentSnapshot.getString("time");
                String carNo = documentSnapshot.getString("carNo");
                String phone = documentSnapshot.getString("phoneNo");
                String noOfSeats = documentSnapshot.getString("noOfSeats");
                String basePrice = documentSnapshot.getString("basePrice");
                String carType = documentSnapshot.getString("carType");

                RideModel ride = new RideModel(source, destination, date, time, carNo, phone, noOfSeats, basePrice, carType);
                rideList.add(ride);
                rideAdapter.notifyDataSetChanged();

                editSeats.setText(noOfSeats);
            } else {
                Toast.makeText(this, "No ride details found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error fetching ride details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("Firestore", "Error fetching ride details", e);
        });
    }

    private void updateSeatCount() {
        String newSeats = editSeats.getText().toString().trim();

        if (newSeats.isEmpty()) {
            Toast.makeText(this, "Enter available seats", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update seats in Firestore
        DocumentReference offerRef = fStore.collection("offer").document(userId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("noOfSeats", newSeats);

        offerRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Seats updated successfully!", Toast.LENGTH_SHORT).show();
                    fetchRideDetails(); // Refresh ride details
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update seats: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Failed to update seats", e);
                });
    }

}