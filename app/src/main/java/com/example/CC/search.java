package com.example.CC;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

public class search extends AppCompatActivity {

    private EditText destinationInput;
    private Button searchButton, bookRidebtn;
    private RadioGroup rideResultsGroup;
    private FirebaseFirestore fstore;
    private TextView eMap;
    private FirebaseAuth fAuth;
    private RequestQueue requestQueue;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    private TextView locationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        fAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        requestQueue = Volley.newRequestQueue(this);
        destinationInput = findViewById(R.id.destinationSearch);
        searchButton = findViewById(R.id.searchInSearch);
        rideResultsGroup = findViewById(R.id.rideResultsGroup);
        bookRidebtn = findViewById(R.id.bookRide_btn);
        eMap = findViewById(R.id.map);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String destination = destinationInput.getText().toString().trim();

                if (destination.isEmpty()) {
                    Toast.makeText(search.this, "Please Enter a Destination", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Fetch and display rides
                fetchAndDisplayRides(destination);
            }
        });
        Places.initialize(getApplicationContext(), "AIzaSyDv-ArHoQExhHDY9uSnxhBBfAKvpL_kPP8");

        Button pickLocationButton = findViewById(R.id.pickupLocation);
        locationTextView = findViewById(R.id.map);

        pickLocationButton.setOnClickListener(v -> openPlacePicker());

        bookRidebtn.setOnClickListener(view -> saveBookingDetails());
        storeFCMToken();

    }

    private void openPlacePicker() {
        // Set allowed fields for Place Picker
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);

        // Build intent for Autocomplete widget
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setCountry("IN") // Change to "IN" for India or remove to allow all
                .build(this);

        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                locationTextView.setText("Selected Location: " + place.getName() + "\n" + place.getAddress());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                locationTextView.setText("Error: " + status.getStatusMessage());
            }
        }
    }

    private void fetchAndDisplayRides(String userDestination) {
        fstore.collection("offer")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Ride> rides = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String destination = document.getString("destination");
                            String source = document.getString("source");
                            String date = document.getString("date");
                            String time = document.getString("time");
                            String phone = document.getString("phoneNo");
                            String carNumber = document.getString("carNo");
                            String noOfSeats = document.getString("noOfSeats");
                            String basePrice = document.getString("basePrice");
                            String driverId = document.getString("driverId");
                            String carType = document.getString("carType");
//                            String carImageUrl = document.getString("carImage");

                            // Add ride to the list
                            rides.add(new Ride(source, destination, date, time, carNumber, noOfSeats, basePrice, driverId, phone, carType));
                        }

                        // Apply KNN algorithm
                        List<Ride> filteredRides = knnSearch(userDestination, rides);

                        // Display results
                        displayRides(filteredRides);
                    } else {
                        Log.e("Firestore", "Error fetching rides", task.getException());
                        Toast.makeText(search.this, "Failed to fetch rides.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private List<Ride> knnSearch(String userDestination, List<Ride> rides) {
        List<Ride> filteredRides = new ArrayList<>();

        // Simple string matching for now (you can add geolocation-based filtering here)
        for (Ride ride : rides) {
            if (ride.getDestination().equalsIgnoreCase(userDestination)) {
                filteredRides.add(ride);
            }
        }

        return filteredRides;
    }

    private void displayRides(List<Ride> rides) {
        rideResultsGroup.removeAllViews();

        if (rides.isEmpty()) {
            Toast.makeText(this, "No rides available for the entered destination.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Ride ride : rides) {
            // Display ride details
            String rideDetails = "CAR NO: " + ride.getCarNumber()
                    + " - SOURCE: " + ride.getSource()
                    + " - DESTINATION: " + ride.getDestination()
                    + " - DATE: " + ride.getDate()
                    + " - TIME: " + ride.getTime()
                    + " - SEATS: " + ride.getNoOfSeats()
                    + " - BASEPRICE: " + ride.getBasePrice()
                    + " - PHONE: "+ ride.getPhone()
                    + " - CARTYPE: "+ ride.getCarType();

            // Add radio button for each ride
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(rideDetails);
            radioButton.setTag(ride.getDriverId());
            rideResultsGroup.addView(radioButton);
        }
    }


    private static class Ride {
        private String source;
        private String destination;
        private String date;
        private String time;
        private String carNo;
        private String noOfSeats;
        private String basePrice;
        private String driverId;
        private String phone;
        private String carType;

        public Ride(String source, String destination, String date, String time, String carNumber, String noOfSeats, String basePrice, String driverId,String phone, String carType) {
            this.source = source;
            this.destination = destination;
            this.date = date;
            this.time = time;
            this.carNo = carNumber;
            this.noOfSeats = noOfSeats;
            this.basePrice = basePrice;
            this.driverId = driverId;
            this.phone = phone;
            this.carType = carType;
        }

        public String getSource() {
            return source;
        }

        public String getDestination() {
            return destination;
        }

        public String getDate() {
            return date;
        }

        public String getTime() {
            return time;
        }

        public String getCarNumber() {
            return carNo;
        }

        public String getNoOfSeats() {
            return noOfSeats;
        }

        public String getBasePrice() {
            return basePrice;
        }
        public String getDriverId() {
            return driverId;
        }
        public String getPhone(){ return phone;}
        public String getCarType(){return carType;}
//        public String getCarImage() { return carImageUrl; }
    }


    private void saveBookingDetails() {
        FirebaseUser currentUser = fAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedRadioButtonId = rideResultsGroup.getCheckedRadioButtonId();
        if (selectedRadioButtonId == -1) {
            Toast.makeText(this, "Please select a ride!", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRideButton = findViewById(selectedRadioButtonId);
        if (selectedRideButton.getTag() == null) {
            Toast.makeText(this, "Error: Ride data missing!", Toast.LENGTH_SHORT).show();
            return;
        }
        String rideDetails = selectedRideButton.getText().toString();
        String driverId = selectedRideButton.getTag().toString(); // Get driver ID from tag

        String destination = destinationInput.getText().toString().trim();
        String pickupLocation = eMap.getText().toString().trim();
        String bookerId = currentUser.getUid();

        if (destination.isEmpty() || pickupLocation.isEmpty()) {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch booker's phone number
        fstore.collection("users").document(bookerId).get().addOnSuccessListener(bookerDoc -> {
            if (!bookerDoc.exists()) {
                Toast.makeText(this, "User data not found!", Toast.LENGTH_SHORT).show();
                return;
            }
            String bookerFCMToken = bookerDoc.getString("fcmToken");
            String bookerPhone = bookerDoc.getString("phone");
            if (bookerPhone == null) {
                Toast.makeText(this, "Booker phone number not found!", Toast.LENGTH_SHORT).show();
                return;
            }


            // Fetch driver's phone number
            fstore.collection("offer")
                    .whereEqualTo("driverId", driverId)
                    .limit(1) // Assuming one offer per driver
                    .get().addOnSuccessListener(querySnapshot -> {
                if (querySnapshot.isEmpty()) {
                    Toast.makeText(this, "Error: No offer found for driver!", Toast.LENGTH_SHORT).show();
                    return;
                }

                DocumentSnapshot offerDoc = querySnapshot.getDocuments().get(0);
                String driverPhone = offerDoc.getString("phoneNo");
                String driverFCMToken = offerDoc.getString("fcmToken");

                if (driverPhone == null || bookerPhone == null) {
                    Toast.makeText(this, "Error: Missing phone details!", Toast.LENGTH_SHORT).show();
                    return;
                }


                Map<String, Object> bookingData = new HashMap<>();
                bookingData.put("destination", destination);
                bookingData.put("pickupLocation", pickupLocation);
                bookingData.put("bookerId", bookerId);
                bookingData.put("driverId", driverId);

                // Save booking in Firestore
                fstore.collection("bookings")
                        .add(bookingData)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(this, "Ride booked successfully!", Toast.LENGTH_SHORT).show();
                            String bookingId = documentReference.getId();

                            // Send FCM notifications
                                    if (driverFCMToken != null) {
                                        sendNotification(driverPhone, "New Ride Booking!", "A user booked your ride to " + destination);
                                    }
                                    if (bookerFCMToken != null) {
                                        sendNotification(bookerPhone, "Ride Booking Confirmed!", "Your ride to " + destination + " is confirmed.");
                                    }


                            // Navigate to ride details page
                            Intent intent = new Intent(search.this, rideDetails.class);
                            intent.putExtra("BOOKING_ID", bookingId);
                            startActivity(intent);

                            // Send SMS notifications
//                            sendNotificationToPhone(driverPhone, "New Ride Booking!", "A user booked your ride to " + destination);
//                            sendNotificationToPhone(bookerPhone, "Ride Booking Confirmed!", "Your ride to " + destination + " is confirmed.");
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error booking ride: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            });
        });
    }

    private void sendNotification(String phoneNumber, String title, String message) {

        // Find user token by phone number
        fstore.collection("users").whereEqualTo("phone", phoneNumber).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                String token = doc.getString("fcmToken");

                if (token != null) {
                    JSONObject notification = new JSONObject();
                    JSONObject notifBody = new JSONObject();

                    try {
                        notifBody.put("title", title);
                        notifBody.put("body", message);
                        notification.put("to", token);
                        notification.put("notification", notifBody);

                        sendFCMMessage(notification);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("FCM Error", "No FCM token found for " + phoneNumber);
                }
            }
        }).addOnFailureListener(e -> Log.e("FCM Error", "Error fetching user token", e));
    }
    // Function to send FCM request
    private void sendFCMMessage(JSONObject notification) {
        String FCM_API = "https://fcm.googleapis.com/fcm/send";
        String SERVER_KEY = "d5896ea629c531b572c4d5e90d116cdef75d5b7e";  // Replace with your Firebase server key
        String CONTENT_TYPE = "application/json";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, FCM_API, notification,
                response -> Log.d("FCM", "Notification sent successfully: " + response.toString()),
                error -> {
                    Log.e("FCM Error", "Failed to send notification: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e("FCM Response", "Response Code: " + error.networkResponse.statusCode);
                        Log.e("FCM Response", "Response Data: " + new String(error.networkResponse.data));
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "key=" + SERVER_KEY);
                headers.put("Content-Type", CONTENT_TYPE);
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

//    private void sendFCMNotification(String fcmToken, String title, String message) {
//        try {
//            JSONObject notification = new JSONObject();
//            notification.put("title", title);
//            notification.put("body", message);
//
//            JSONObject data = new JSONObject();
//            data.put("click_action", "FLUTTER_NOTIFICATION_CLICK");
//
//            JSONObject payload = new JSONObject();
//            payload.put("to", fcmToken);
//            payload.put("notification", notification);
//            payload.put("data", data);
//
//            String FCM_URL = "https://fcm.googleapis.com/fcm/send";
//
//            JsonObjectRequest request = new JsonObjectRequest(FCM_URL, payload,
//                    response -> Log.d("FCM", "Notification sent successfully!"),
//                    error -> Log.e("FCM Error", "Failed to send notification: " + error.getMessage())) {
//
//                @Override
//                public Map<String, String> getHeaders() {
//                    Map<String, String> headers = new HashMap<>();
//                    headers.put("Authorization", "key=d5896ea629c531b572c4d5e90d116cdef75d5b7e");
//                    headers.put("Content-Type", "application/json");
//                    return headers;
//                }
//            };
//
//            requestQueue.add(request);
//        } catch (JSONException e) {
//            Log.e("FCM Error", "JSON Exception: " + e.getMessage());
//        }
//    }


    private void storeFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                String userId = fAuth.getCurrentUser().getUid();
                Log.d("FCM Token", "User ID: " + userId + ", Token: " + token);
                fstore.collection("users").document(userId).update("fcmToken", token)
                        .addOnSuccessListener(aVoid -> Log.d("FCM", "Token stored successfully"))
                        .addOnFailureListener(e -> Log.e("FCM", "Error storing token", e));
            }
        });
    }


public void cancel(View view){
//        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), MainActivity1.class));
        finish();
    }
}