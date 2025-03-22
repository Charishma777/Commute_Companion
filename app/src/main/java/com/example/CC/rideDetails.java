package com.example.CC;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


public class rideDetails extends AppCompatActivity {

    private TextView tvRideDetails;
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "RideDetailsPrefs";
    private static final String KEY_RIDE_DETAILS = "rideDetails";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ride_details);

        tvRideDetails = findViewById(R.id.tvRideDetails);
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        FirebaseUser currentUser = fAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String savedDetails = sharedPreferences.getString(KEY_RIDE_DETAILS + "_" + userId, null);

            if (savedDetails != null) {
                tvRideDetails.setText(savedDetails);
            }
        }

        // Get the booking ID from the Intent
        String bookingId = getIntent().getStringExtra("BOOKING_ID");
        if (bookingId != null) {
            Toast.makeText(this, "Received BOOKING_ID: " + bookingId, Toast.LENGTH_LONG).show();
            fetchBookingDetails(bookingId);
//            fetchBookingDetailsForCurrentUser();
        } else {
            Toast.makeText(this, "No booking ID found!", Toast.LENGTH_SHORT).show();
        }

    }

    private void fetchBookingDetails(String bookingId) {

        // Fetch the document from 'bookings' collection
        fStore.collection("bookings")
                .document(bookingId)
                .get()
                .addOnSuccessListener(bookingDoc -> {
                    if (bookingDoc.exists()) {
                        String driverId = bookingDoc.getString("driverId");
                        String destination = bookingDoc.getString("destination");
                        String pickup = bookingDoc.getString("pickupLocation");
                        if (driverId == null || driverId.isEmpty()) {
                            tvRideDetails.setText("Booking data incomplete: missing driver Id.");
                            return;
                        }

                        // 2. Fetch the driver's contact details from the 'users' collection
                        fStore.collection("users")
                                .document(driverId)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        String userEmail = userDoc.getString("email");
                                        String userPhone = userDoc.getString("phone");

                                        // 3. Fetch the driver's offer details from the 'offer' collection using the same userId
                                        fStore.collection("offer")
                                                .document(driverId)
                                                .get()
                                                .addOnSuccessListener(offerDoc -> {
                                                    if (offerDoc.exists()) {
                                                        String carNo   = offerDoc.getString("carNo");
                                                        String source  = offerDoc.getString("source");
                                                        String date    = offerDoc.getString("date");
                                                        String time    = offerDoc.getString("time");
                                                        String noOfSeats = offerDoc.getString("noOfSeats");
                                                        String basePrice = offerDoc.getString("basePrice");
                                                        String carType = offerDoc.getString("carType");

                                                        // 4. Combine and display the details
                                                        String driverDetails =
                                                                "Driver Email: " + (userEmail != null ? userEmail : "N/A") + "\n" +
                                                                        "Phone Number: " + (userPhone != null ? userPhone : "N/A") + "\n\n" +
                                                                        "Car Number: "   + (carNo   != null ? carNo   : "N/A") + "\n" +
                                                                        "Source: "       + (source  != null ? source  : "N/A") + "\n" +
                                                                        "Destination: " + (destination!= null ? destination : "N/A") +"\n"+
                                                                        "Date: "         + (date    != null ? date    : "N/A") + "\n" +
                                                                        "Time: "         + (time    != null ? time    : "N/A")+ "\n"+
                                                                        "Number Of Seats: "         + (noOfSeats    != null ? noOfSeats    : "N/A")+ "\n"+
                                                                        "Base Price: "         + (basePrice    != null ? basePrice    : "N/A")+ "\n"+
                                                                        "Driver Car Model: "         + (carType    != null ? carType    : "N/A")+ "\n"+
                                                                        "Your PickupLocation: " + (pickup != null ? pickup : "N/A")+ "\n"+"\n"+
                                                                        "Contact driver for information";

                                                        tvRideDetails.setText(driverDetails);
                                                        // Save these details locally for reuse
                                                        saveRideDetailsLocally(driverDetails);
                                                    } else {
                                                        tvRideDetails.setText("Offer details not found.");
                                                    }
                                                })
                                                .addOnFailureListener(e ->
                                                        Toast.makeText(this, "Error fetching offer details: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                                );
                                    } else {
                                        tvRideDetails.setText("Driver details not found in users collection.");
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Error fetching user details: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                    } else {
                        tvRideDetails.setText("Booking not found.");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void saveRideDetailsLocally(String details) {
        FirebaseUser currentUser = fAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_RIDE_DETAILS + "_" + userId, details); // Save per user
            editor.apply();
        }
    }

    public void cancel(View view){
//        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), MainActivity1.class));
        finish();
    }
}