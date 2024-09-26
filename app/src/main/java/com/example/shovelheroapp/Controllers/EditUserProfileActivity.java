package com.example.shovelheroapp.Controllers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.shovelheroapp.Models.User;
import com.example.shovelheroapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditUserProfileActivity extends AppCompatActivity {

    private EditText  editFirstname, editLastname, editBirthdate, editEmail, editPhoneNumber;
    private Button updateProfile;
    private String userId;

    private String selectedBirthdate;

    private ImageButton changeProfilePicture;
    private Uri userProfileImageUri;

    private ImageView profileImageView;
    private ActivityResultLauncher<Intent> profileImageLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_profile);

        // Get Views
        editFirstname = findViewById(R.id.etEditFirstname);
        editLastname = findViewById(R.id.etEditLastname);
        editBirthdate = findViewById(R.id.etEditBirthdate);
        editEmail = findViewById(R.id.etEditEmail);
        editPhoneNumber = findViewById(R.id.etEditPhoneNumber);
        updateProfile = findViewById(R.id.btnUpdateProfile);
        changeProfilePicture = findViewById(R.id.btnChangeProfilePicture);
        profileImageView = findViewById(R.id.ivProfileImageView);

        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String firstName = editFirstname.getText().toString();
                String lastName = editLastname.getText().toString();
                String email = editEmail.getText().toString();
                String phone = editPhoneNumber.getText().toString();

                if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty()){
                    Toast.makeText(EditUserProfileActivity.this, "Please fill out all the fields", Toast.LENGTH_SHORT).show();
                } else if (!(android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())) {
                    Toast.makeText(EditUserProfileActivity.this, "Please enter valid email", Toast.LENGTH_SHORT).show();
                } else if (!(android.util.Patterns.PHONE.matcher(phone).matches())) {
                    Toast.makeText(EditUserProfileActivity.this, "Please enter valid phone number", Toast.LENGTH_SHORT).show();
                } else {
                    updateUserProfile();
                }


                /**
                if(!(android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())){
                    Toast.makeText(EditUserProfileActivity.this, "Please enter valid email", Toast.LENGTH_SHORT).show();
                }
                if(!(android.util.Patterns.PHONE.matcher(phone).matches())){
                    Toast.makeText(EditUserProfileActivity.this, "Please enter valid phone number", Toast.LENGTH_SHORT).show();
                }
                 **/
            }
        });
        changeProfilePicture.setOnClickListener(v -> selectProfileImage());

        editBirthdate.setOnClickListener(v -> showBirthYearPicker());

        profileImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        userProfileImageUri = result.getData().getData();

                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), userProfileImageUri);
                            profileImageView.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(EditUserProfileActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        //Retrieve User ID
        userId = getIntent().getStringExtra("USER_ID");
        if(userId == null || userId.isEmpty()){
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            return;
        }
        //Load the User Data
        loadUserProfile();

        /**
        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserProfile();
            }
        });
         **/
    }

    private void selectProfileImage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        profileImageLauncher.launch(intent);
    }

    public void showBirthYearPicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        NumberPicker birthYearPicker = new NumberPicker(this);
        birthYearPicker.setMinValue(1903);
        birthYearPicker.setMaxValue(year);
        birthYearPicker.setValue(year);

        new AlertDialog.Builder(this)
                .setTitle("Select Year")
                .setView(birthYearPicker)
                .setPositiveButton("OK", (dialog, which) -> {
                    int selectedYear = birthYearPicker.getValue();
                    // display date picker after date selection
                    showDatePickerWithBirthYear(selectedYear);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void showDatePickerWithBirthYear(int year) {
        final Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                EditUserProfileActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format date
                    selectedBirthdate =  (selectedMonth + 1) + "-" + selectedDay + "-" + selectedYear;
                    editBirthdate.setText(selectedBirthdate);
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void loadUserProfile(){
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    if(user != null) {
                        editFirstname.setText(user.getFirstName());
                        editLastname.setText(user.getLastName());
                        editBirthdate.setText(user.getBirthdate());
                        editEmail.setText(user.getEmail());
                        editPhoneNumber.setText(user.getPhoneNo());

                        // Load Profile Picture

                        String profileImageUrl = user.getProfilePictureUrl();
                        if(profileImageUrl != null && !profileImageUrl.isEmpty()){
                            Glide.with(EditUserProfileActivity.this)
                                    .load(profileImageUrl).into(profileImageView);

                        }

                    } else {
                        Toast.makeText(EditUserProfileActivity.this, "User data cannot be found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditUserProfileActivity.this, "User doesn't exist in the database", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditUserProfileActivity.this, "Could not load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updateUserProfile() {
        // TODO: input field validation

        if (userProfileImageUri != null) {
            StorageReference fileReference = FirebaseStorage.getInstance().getReference().child("profilePictures/" + userId + ".jpg");
            fileReference.putFile(userProfileImageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();

                                updateFirebaseUserProfile(imageUrl);
                            })
                    )
                    .addOnFailureListener(e -> Toast.makeText(EditUserProfileActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            // Upload profile information if no image is selected
            updateFirebaseUserProfile(null);
        }
    }

    private void updateFirebaseUserProfile(String imageUrl) {
        String newFirstname = editFirstname.getText().toString();
        String newLastname = editLastname.getText().toString();
        String newBirthdate = editBirthdate.getText().toString();
        String newEmail = editEmail.getText().toString();
        String newPhoneNumber = editPhoneNumber.getText().toString();

        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(userId);
        Map<String, Object> updateProfileData = new HashMap<>();
        updateProfileData.put("firstName", newFirstname);
        updateProfileData.put("lastName", newLastname);
        updateProfileData.put("birthdate", newBirthdate);
        updateProfileData.put("email", newEmail);
        updateProfileData.put("phoneNo", newPhoneNumber);
        if (imageUrl != null) {
            updateProfileData.put("profilePictureUrl", imageUrl);
        }

        userReference.updateChildren(updateProfileData)
                .addOnSuccessListener(aVoid -> Toast.makeText(EditUserProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(EditUserProfileActivity.this, "Profile update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}