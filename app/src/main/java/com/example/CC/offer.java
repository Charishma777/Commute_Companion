package com.example.CC;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
//import android.os.Message;
//import android.se.omapi.Session;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.auth.FirebaseAuth.AuthPersistence;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

//import java.net.Authenticator;
//import java.net.PasswordAuthentication;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

//import javax.mail.Authenticator;
//import javax.mail.Message;
//import javax.mail.MessagingException;
//import javax.mail.PasswordAuthentication;
//import javax.mail.Session;
//import javax.mail.Transport;
//import javax.mail.internet.InternetAddress;
//import javax.mail.internet.MimeMessage;


public class offer extends AppCompatActivity {

    EditText mSourceAddress, mDestAddress, mDate, mCarNo, mTime, mPhone, mCarType;
    Button mOfferBtn;
    FirebaseFirestore fstore;
    FirebaseUser currentUser;
    FirebaseAuth fAuth;
    Spinner spinnerSeats, spinnerBasePrice;
//    private static final int PICK_IMAGE_REQUEST = 1;
//    private Uri imageUri;  // Store selected image URI
//    private Button btnSelectImage;
//    private StorageReference storageReference;
//    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_offer);

        mDate = findViewById(R.id.dateInput3);
        mSourceAddress = findViewById(R.id.sourceInput);
        mDestAddress = findViewById(R.id.destinationInput);
        mTime = findViewById(R.id.timeInput);
        mCarNo = findViewById(R.id.carNoInput);
        mPhone = findViewById(R.id.phoneInput1);
        mCarType = findViewById(R.id.carTypeInput);
        mOfferBtn = findViewById(R.id.btn_ofr);

        fAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        currentUser = fAuth.getCurrentUser();

//        databaseReference = FirebaseDatabase.getInstance().getReference("offer");


        mOfferBtn.setOnClickListener(view -> offerRide());

        // Set a click listener to show the DatePickerDialog
        mDate.setOnClickListener(v -> {
            // Get the current date
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Show the DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(offer.this, (view, selectedYear, selectedMonth, selectedDay) -> {
                // Set the selected date to the EditText
                String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                mDate.setText(selectedDate);
            },
                    year, month, day
            );

            datePickerDialog.show();
        });

        // Initialize Spinners
        spinnerSeats = findViewById(R.id.spinner_seats);
        spinnerBasePrice = findViewById(R.id.spinner_price);

        // Adapter for No. of Seats
        ArrayAdapter<CharSequence> seatAdapter = ArrayAdapter.createFromResource(this,
                R.array.seat_options, android.R.layout.simple_spinner_item);
        seatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSeats.setAdapter(seatAdapter);

        // Adapter for Base Price
        ArrayAdapter<CharSequence> priceAdapter = ArrayAdapter.createFromResource(this,
                R.array.price_options, android.R.layout.simple_spinner_item);
        priceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBasePrice.setAdapter(priceAdapter);

        // Get selected values when user selects an option
        spinnerSeats.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSeat = parent.getItemAtPosition(position).toString();
                Toast.makeText(getApplicationContext(), "Selected Seats: " + selectedSeat, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        spinnerBasePrice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedPrice = parent.getItemAtPosition(position).toString();
                Toast.makeText(getApplicationContext(), "Base Price: " + selectedPrice, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

    }

    // Open Gallery
//    private void openGallery() {
//        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        intent.setType("image/*");
//        startActivityForResult(intent, PICK_IMAGE_REQUEST);
//    }

    // Handle Image Selection and Upload
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
//            imageUri = data.getData();
//            uploadImageToFirebase();
//        }
//    }

    // Upload Image to Firebase
//    private void uploadImageToFirebase() {
//        if (imageUri != null) {
//            StorageReference fileRef = storageReference.child(System.currentTimeMillis() + ".jpg");
//
//            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
//                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
////                    carImageUrl = uri.toString();
//                    Toast.makeText(this, "Image Uploaded Successfully!", Toast.LENGTH_SHORT).show();
//
//                    // Store imageUrl in Firebase Database (optional)
////                    offerRide(imageUrl);
//                });
//            }).addOnFailureListener(e -> {
//                Toast.makeText(this, "Image Upload Failed!", Toast.LENGTH_SHORT).show();
//            });
//        }
//    }

    private void offerRide() {
        String source = mSourceAddress.getText().toString().trim();
        String destination = mDestAddress.getText().toString().trim();
        String date = mDate.getText().toString().trim();
        String time = mTime.getText().toString().trim();
        String carNo = mCarNo.getText().toString().trim();
        String phoneNo = mPhone.getText().toString().trim();
        String carType = mCarType.getText().toString().trim();
        String selectedSeats = spinnerSeats.getSelectedItem().toString().trim();
        String selectedBasePrice = spinnerBasePrice.getSelectedItem().toString().trim();

        if (TextUtils.isEmpty(source) || TextUtils.isEmpty(destination) || TextUtils.isEmpty(date) || TextUtils.isEmpty(time) || TextUtils.isEmpty(carNo) || TextUtils.isEmpty(phoneNo) || TextUtils.isEmpty(selectedSeats) || TextUtils.isEmpty(selectedBasePrice) || TextUtils.isEmpty(carType)) {
            Toast.makeText(offer.this, "Please fill above all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser == null) {
            Toast.makeText(this, "User session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(offer.this, Login.class));
            finish();
            return;
        }

        String userID = currentUser.getUid();
        Log.d("Auth", "Current User UID: " + userID); // Debugging

            fstore.collection("users").document(userID).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String userEmail = documentSnapshot.getString("email");

                    if (userEmail != null) {
                        Map<String, Object> rideDetails = new HashMap<>();
                        rideDetails.put("driverId", userID);
                        rideDetails.put("source", source);
                        rideDetails.put("destination", destination);
                        rideDetails.put("date", date);
                        rideDetails.put("time", time);
                        rideDetails.put("carNo", carNo);
                        rideDetails.put("phoneNo", phoneNo);
                        rideDetails.put("noOfSeats", selectedSeats);
                        rideDetails.put("basePrice", selectedBasePrice);
                        rideDetails.put("carType", carType);
//                        rideDetails.put("carImage", carImageUrl;


                        fstore.collection("offer").document(userID).set(rideDetails).addOnSuccessListener(aVoid -> {
                            Toast.makeText(offer.this, "Ride Offered", Toast.LENGTH_SHORT).show();

                            // 🔹 Save to SharedPreferences for later use
                            SharedPreferences prefs = getSharedPreferences("OfferDetailsPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("source", source);
                            editor.putString("destination", destination);
                            editor.putString("date", date);
                            editor.putString("time", time);
                            editor.putString("carNo", carNo);
                            editor.putString("phoneNo", phoneNo);
                            editor.putString("noOfSeats", selectedSeats);
                            editor.putString("basePrice", selectedBasePrice);
                            editor.putString("driverId", userID);
                            editor.putString("carType", carType);
//                            editor.putString("carImage", carImageUrl);
                            editor.apply();

                            Intent intent = new Intent(offer.this,offerDetails.class);
                            startActivity(intent);
                        }).addOnFailureListener(e -> {
                            Toast.makeText(offer.this, "Failed To Offer Ride " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("Firestore", "Error adding Document", e);
                        });
                    }
                }
            }).addOnFailureListener(e -> Log.e("Firestore", "Failed to fetch user email", e));

    }



    public void cancel(View view){
//        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), MainActivity1.class));
        finish();
    }
}
