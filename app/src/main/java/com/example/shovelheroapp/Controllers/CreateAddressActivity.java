package com.example.shovelheroapp.Controllers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.shovelheroapp.Models.Address;
import com.example.shovelheroapp.Models.User;
import com.example.shovelheroapp.R;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CreateAddressActivity extends AppCompatActivity {
    private static final String TAG = "CreateAddressActivity";

    private ImageView customerAddressImage;
    private EditText addressEditText;
    //private EditText cityEditText;
    //private EditText provinceEditText;
    //private EditText postalCodeEditText;
    //private Spinner countrySpinner;
    //private EditText addressNotesEditText;
    private List<String> itemsRequestedList;

    private EditText drivewaySquareFootageEditText;
    private User currentUser;
    private String currentUserId;
    private Button btnCreateAddress;
    private DatabaseReference userTable;

    private String streetAddress = "";
    private String city = "";
    private String postalCode = "";
    private String country = "";
    private String province = "";

    private ActivityResultLauncher<Intent> autocompleteResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_create);

        //inialize itemsRequestedList
        itemsRequestedList = new ArrayList<>();

        //get text input fields
        customerAddressImage = findViewById(R.id.imgPropertyImage);
        addressEditText = findViewById(R.id.etAddress);
//        cityEditText = findViewById(R.id.etCity);
//        provinceEditText = findViewById(R.id.etProvince);
//        postalCodeEditText = findViewById(R.id.etPostalCode);
//        countrySpinner = findViewById(R.id.spCountry);
        //addressNotesEditText = findViewById(R.id.etAddressNotes);
        drivewaySquareFootageEditText = findViewById(R.id.etSqFoot);

        btnCreateAddress = findViewById(R.id.btnCreateAddress);

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyCuavjoxYxefk2HmHyJPM6yeVgGQDs0yTs");
        }

        //get userID

        currentUserId = getIntent().getStringExtra("USER_ID");
        if (currentUserId == null) {
            Toast.makeText(this, "Temp msg: CustomerID is null", Toast.LENGTH_SHORT).show();
        }

        //Instantiate userTable
        userTable = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);

        autocompleteResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());

                    String streetNumber = "";
                    String route = "";

                    city = "";
                    province = "";
                    postalCode = "";
                    country = "";

                    if (place.getAddressComponents() != null) {
                        for (AddressComponent component : place.getAddressComponents().asList()) {
                            for (String type : component.getTypes()) {
                                if (type.equals("postal_code")) {
                                    postalCode = component.getName();
                                } else if (type.equals("locality") || type.equals("sublocality")) {
                                    city = component.getName();
                                } else if (type.equals("administrative_area_level_1")) {
                                    province = component.getName();
                                } else if (type.equals("country")) {
                                    country = component.getName();
                                } else if (type.equals("route")) {
                                    route = component.getName();
                                } else if (type.equals("street_number")) {
                                    streetNumber = component.getName();
                                }
                            }
                        }
                    }

                    streetAddress = streetNumber + " " + route;

                    // Create full address by concatenated components
                    String fullAddress = streetAddress + ", " + city + ", " + province + ", " + postalCode + ", " + country;

                    // Set full address in address edit text
                    if (addressEditText != null) {
                        addressEditText.setText(fullAddress.trim());
                    }

                } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR) {
                    Status status = Autocomplete.getStatusFromIntent(result.getData());
                    Log.e(TAG, "Error: " + status.getStatusMessage());
                }
            }
        });

        // Click Listener for Address Edit Text
        addressEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAutocompleteActivity();
            }
        });

        btnCreateAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnCreateAddress.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String address = addressEditText.getText().toString().trim();
                        String drivewaySquareFootage = drivewaySquareFootageEditText.getText().toString().trim();

                        if (address.isEmpty() || drivewaySquareFootage.isEmpty()) {
                            Toast.makeText(CreateAddressActivity.this, "Please fill out the address and square footage", Toast.LENGTH_SHORT).show();
                        }

                        else if (!drivewaySquareFootage.matches("^[0-9]+$")) {
                            Toast.makeText(CreateAddressActivity.this, "Please enter a valid square footage (numbers only)", Toast.LENGTH_SHORT).show();
                        }
                        else {

                            createAddress();
                            saveAndReturnToProfile(currentUserId);
                        }
                    }
                });
            }
        });
    }
    public void createAddress() {
        String addressId = userTable.child("addresses").push().getKey();
        String address = addressEditText.getText().toString();
        //String city = cityEditText.getText().toString();
        //String province = provinceEditText.getText().toString();
        //String postalCode = postalCodeEditText.getText().toString();
        //String country = countrySpinner.getSelectedItem().toString();
        String sqFootageStr = drivewaySquareFootageEditText.getText().toString();
        int sqFootage = sqFootageStr.isEmpty() ? 0 : Integer.parseInt(sqFootageStr);


        //CREATE ADDRESS OBJECT (WITHIN USER) THEN RESET FIELDS FOR NEW ENTRY
        if (!address.isEmpty() && !city.isEmpty() && !province.isEmpty() && !postalCode.isEmpty() && !country.isEmpty() && !sqFootageStr.isEmpty()) {
            Address newAddress = new Address(addressId, address, city, province, postalCode, country, sqFootage);

            System.out.println("New address created: " + newAddress.getAddress());

            //save new address and reset input form
            if (addressId != null) {
                userTable.child("addresses").child(addressId).setValue(newAddress);
                System.out.println("New address added to Firebase under user: " + newAddress.getAddress());

                addressEditText.setText("");
                //cityEditText.setText("");
                //provinceEditText.setText("");
                //postalCodeEditText.setText("");
                //countrySpinner.setAdapter(null);
            }
        }
        else {
            System.out.println("Missing address info, please retry");
        }
    }
    private void startAutocompleteActivity() {
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,
                Place.Field.LAT_LNG, Place.Field.ADDRESS_COMPONENTS);

        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setCountries(Collections.singletonList("CA"))
                .build(this);

        autocompleteResultLauncher.launch(intent);
    }

    private void saveAndReturnToProfile(String currentUserId){
        DatabaseReference userTable = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
        userTable.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUser = snapshot.getValue(User.class);
                String accountType = currentUser.getAccountType();

                //**todo** ADD ADULT SHOVELLER AND GUARDIAN
                if(accountType != null) {
                    switch (accountType) {
                        case "Youth Shoveller":
                            Intent intentYouth = new Intent(CreateAddressActivity.this, YouthShovelerProfileActivity.class);
                            String youthID = currentUser.getUserId();
                            intentYouth.putExtra("USER_ID", youthID);
                            startActivity(intentYouth);
                            overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                            break;
                        case "Customer":
                            Intent intentCustomer = new Intent(CreateAddressActivity.this, CustomerProfileActivity.class);
                            String customerId = currentUser.getUserId();
                            intentCustomer.putExtra("USER_ID", customerId);
                            startActivity(intentCustomer);
                            overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                            break;
                        case "Guardian":
                            Intent intentGuardian = new Intent(CreateAddressActivity.this, GuardianProfileActivity.class);
                            String guardianId = currentUser.getUserId();
                            intentGuardian.putExtra("USER_ID", guardianId);
                            startActivity(intentGuardian);
                            overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                            break;
                        default:
                            Intent intent = new Intent(CreateAddressActivity.this, UserRegistrationActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                            break;
                    }
                } else{
                    System.out.println("Account Type is Null");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("There is a database error");
            }
        });
    }
}


