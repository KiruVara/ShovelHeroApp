package com.example.shovelheroapp.Controllers;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.shovelheroapp.Models.Retrofit.CloudFunctionsService;
import com.example.shovelheroapp.Models.Retrofit.GuardianIdInformation;
import com.example.shovelheroapp.Models.Retrofit.RetrofitClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.shovelheroapp.Models.User;
import com.example.shovelheroapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Retrofit;

public class UserRegistrationActivity extends AppCompatActivity {

    private static final String TAG = "UserRegistrationActivity";
    private String userId;
    private Spinner spinnerAccountType;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private TextView birthdateText;
    private String selectedBirthdate;
    private EditText emailEditText;
    private EditText phoneEditText;
    private ImageButton uploadIdImage;
    private TextView uploadIdCardTextView;
    private Button createAccountButton;
    private Uri selectedIdUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);

        //select account type
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.account_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerAccountType = findViewById(R.id.spAccountType);

        //get text input fields
        usernameEditText = findViewById(R.id.etUsername);
        passwordEditText = findViewById(R.id.etPassword);
        confirmPasswordEditText = findViewById(R.id.etCoPassword);
        firstNameEditText = findViewById(R.id.etFirstname);
        lastNameEditText = findViewById(R.id.etLastname);
        emailEditText = findViewById(R.id.etEmail);
        phoneEditText = findViewById(R.id.etPhone);
        birthdateText = findViewById(R.id.btnBirthdate);
        uploadIdCardTextView = findViewById(R.id.tvUploadIdCard);
        uploadIdImage = findViewById(R.id.imgUploadIdCard);
        createAccountButton = findViewById(R.id.btnCreateAccount);

        // Set visibility of ID card button and add ID text to hidden
        uploadIdImage.setVisibility(View.GONE);
        uploadIdCardTextView.setVisibility(View.GONE);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String confirmPassword = confirmPasswordEditText.getText().toString();
                String firstName = firstNameEditText.getText().toString();
                String lastName = lastNameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String phone = phoneEditText.getText().toString();

                if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty()){
                    Toast.makeText(UserRegistrationActivity.this, "Please fill out all the fields", Toast.LENGTH_SHORT).show();
                } else if (username.length() <= 3 || username.length() >= 20) {
                    Toast.makeText(UserRegistrationActivity.this, "Please enter valid username", Toast.LENGTH_SHORT).show();
                } else if (!(password.length() >= 8 && password.matches(".*\\d.*") && password.matches(".*[a-zA-Z].*"))) {
                    Toast.makeText(UserRegistrationActivity.this, "Password must contain at least 8 characters and one letter and one digit", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirmPassword)) {
                    Toast.makeText(UserRegistrationActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                } else if (!(android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())) {
                    Toast.makeText(UserRegistrationActivity.this, "Please enter valid email", Toast.LENGTH_SHORT).show();
                } else if (!(android.util.Patterns.PHONE.matcher(phone).matches())) {
                    Toast.makeText(UserRegistrationActivity.this, "Please enter valid phone number", Toast.LENGTH_SHORT).show();
                } else{
                    createUser(view);
                }
            }
        });


        birthdateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBirthYearPicker();
            }
        });


        //image button listener
        uploadIdImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        // Method for displaying the ID functionality just for the Guardian
        spinnerAccountType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedUser = spinnerAccountType.getSelectedItem().toString();
                if(selectedUser.equals("Guardian")){
                    uploadIdImage.setVisibility(View.VISIBLE);
                    uploadIdCardTextView.setVisibility(View.VISIBLE);

                } else {
                    uploadIdImage.setVisibility(View.GONE);
                    uploadIdCardTextView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Required method. No implementation required for now

            }
        });
    }

    public void createUser(View view) {

        //initialize ShovelHeroDB
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userReference = database.getReference("users");

        // save fields to proper input type

        userId = userReference.push().getKey();
        String accountType = spinnerAccountType.getSelectedItem().toString();
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String phone = phoneEditText.getText().toString();
        String birthdate = selectedBirthdate;

        //create new user
        User newUser = new User(userId, accountType, username, password, firstName, lastName, birthdate, email, phone);

        //add to shovelHeroDB
        userReference.child(userId).setValue(newUser)

                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(UserRegistrationActivity.this, "User created successfully", Toast.LENGTH_SHORT).show();

                        if ("Guardian".equals(accountType) && selectedIdUri != null) {
                            uploadIdImage();
                        }

                        //TO ADD FUNDRAISER AND ADULT SHOVELLER IN LATER ITERATION
                        switch (accountType) {
                            case "Youth Shoveller":
                                Intent intentCreateYouth = new Intent(UserRegistrationActivity.this, YouthShovelerProfileActivity.class);
                                String youthID = userId;
                                intentCreateYouth.putExtra("USER_ID", youthID);
                                startActivity(intentCreateYouth);
                                overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                                break;
                            case "Customer":
                                Intent intentCreateCustomer = new Intent(UserRegistrationActivity.this, CustomerProfileActivity.class);
                                String customerId = userId;
                                intentCreateCustomer.putExtra("USER_ID", customerId);
                                startActivity(intentCreateCustomer);
                                overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                                break;
                            case "Guardian":
                                Intent intentCreateGuardian = new Intent(UserRegistrationActivity.this, GuardianProfileActivity.class);
                                String guardianId = userId;
                                intentCreateGuardian.putExtra("USER_ID", guardianId);
                                startActivity(intentCreateGuardian);
                                overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                                break;
                            default:
                                Intent intent = new Intent(UserRegistrationActivity.this, UserRegistrationActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserRegistrationActivity.this, "Could not create user. Please try again", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void showBirthYearPicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        NumberPicker birthYearPicker = new NumberPicker(this);
        birthYearPicker.setMinValue(1900);
        birthYearPicker.setMaxValue(year);
        birthYearPicker.setValue(year);

        new AlertDialog.Builder(this)
                .setTitle("Select Year")
                .setView(birthYearPicker)
                .setPositiveButton("OK", (dialog, which) -> {
                    int selectedYear = birthYearPicker.getValue();
                    // show date picker after birth year selected
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
                UserRegistrationActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {

                    selectedBirthdate =  (selectedMonth + 1) + "-" + selectedDay + "-" + selectedYear;
                    birthdateText.setText(selectedBirthdate);
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void selectImage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        getImage.launch(intent);
    }
    ActivityResultLauncher<Intent> getImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    assert data != null;
                    selectedIdUri = data.getData();
                    setImageButtonBackground(selectedIdUri);
                }
            });

    private void setImageButtonBackground(Uri imageUri) {
        try {
            // Load the selected image from the URI
            Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            // Set the image as the background of the ImageButton
            uploadIdImage.setImageBitmap(image);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }
    private void uploadIdImage() {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference idReference = storageReference.child("guardianIds/" + userId + ".jpg");

        idReference.putFile(selectedIdUri).addOnSuccessListener(taskSnapshot -> {
            // get download URL
            idReference.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                // update user's profile with image url
                updateUserWithIDUrl(imageUrl);
            }).addOnFailureListener(e -> {

                Toast.makeText(UserRegistrationActivity.this, "Failed to get ID download URL", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(UserRegistrationActivity.this, "Failed to upload ID" + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateUserWithIDUrl(String imageUrl) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userReference = database.getReference("users").child(userId);

        // Create Map with image Url
        Map<String, Object> updates = new HashMap<>();
        updates.put("guardianIdUrl", imageUrl);

        // Update guardian's profile with ID url
        userReference.updateChildren(updates).addOnSuccessListener(aVoid -> {
            Toast.makeText(UserRegistrationActivity.this, "ID linked with Guardian profile", Toast.LENGTH_SHORT).show();
            sendIdForValidation(imageUrl, userId);

        }).addOnFailureListener(e -> {
            Toast.makeText(UserRegistrationActivity.this, "Failed to link ID with Guardian profile", Toast.LENGTH_SHORT).show();
        });
    }
    private void sendIdForValidation(String guardianIDUrl, String userId) {

        // Get retrofit instance from singleton class
        Retrofit retrofit = RetrofitClient.getClient();
        CloudFunctionsService service = retrofit.create(CloudFunctionsService.class);

        // Create instance of GuardianInformation
        GuardianIdInformation data = new GuardianIdInformation(guardianIDUrl, userId);

        // Make network request
        Call<Void> call = service.sendIdForValidation(data);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.isSuccessful()) {
                    // Handle success
                    Toast.makeText(UserRegistrationActivity.this, "ID validation request sent", Toast.LENGTH_SHORT).show();
                } else {
                    // Handle Failure
                    Toast.makeText(UserRegistrationActivity.this, "Failed to send ID validation request", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Handle error
                Toast.makeText(UserRegistrationActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}