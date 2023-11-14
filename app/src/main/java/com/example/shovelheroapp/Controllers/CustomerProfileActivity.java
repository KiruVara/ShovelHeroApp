package com.example.shovelheroapp.Controllers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.shovelheroapp.Models.Address;
import com.example.shovelheroapp.Models.User;
import com.example.shovelheroapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CustomerProfileActivity extends AppCompatActivity {

    private static final String TAG = "CustomerProfileActivity";

    //initialize ShovelHeroDB userTable(Firebase)
    DatabaseReference userTable;


    private TextView usernameTV;
    private TextView passwordTV;
    private TextView firstNameTV;
    private TextView lastNameTV;
    private DatePicker birthdateDatePicker;
    private TextView emailTV;
    private TextView phoneTV;
    private User currentUser;
    private String currentCustomerId;

    //address list
    //private ListView addressListView;
    //private ArrayAdapter<String> addressAdapter;
    //private List<String> addressList;

    //AddressList setup
    private RecyclerView addressRecyclerView;

    AddressAdapter adapter;
    List<Address> addressList;


    //buttons
    Button btnAddAddress;
    Button btnOrderShoveling;
    Button btnManagePaymentInfo;
    Button btnEditPassword;
    Button btnViewMyRatings;
    Button btnLogout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_customer);

        //Instantiate userTable to current listing
        userTable = FirebaseDatabase.getInstance().getReference("users");

        usernameTV = findViewById(R.id.tvUsername);
        passwordTV = findViewById(R.id.tvPassword);
        firstNameTV = findViewById(R.id.tvFirstName);
        lastNameTV = findViewById(R.id.tvLastname);
        emailTV = findViewById(R.id.tvEmail);
        phoneTV = findViewById(R.id.tvPhone);

        //instantiate addressList + adapter
        btnAddAddress = findViewById(R.id.btnAddAddress);
        addressRecyclerView = findViewById(R.id.addressRecyclerView);
        addressList = new ArrayList<>();
        adapter = new AddressAdapter(addressList);
        addressRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        addressRecyclerView.setAdapter(adapter);

        btnOrderShoveling = findViewById(R.id.btnOrderShoveling);
        btnManagePaymentInfo = findViewById(R.id.btnManagePaymentInfo);
        btnEditPassword = findViewById(R.id.btnEditPassword);
        btnViewMyRatings = findViewById(R.id.btnViewMyRatings);
        btnLogout = findViewById(R.id.btnLogout);


        //GET USERID FROM LOGIN OR REGISTRATION
        Intent intent = getIntent();
        if (intent != null) {
            currentCustomerId = intent.getStringExtra("USER_ID");
            if (currentCustomerId != null) {
                retrieveCustomerProfileData(currentCustomerId);
            }
        }
    }

    private void retrieveCustomerProfileData(String customerId) {
        userTable.child(customerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);

                    if (user != null) {
                        //display customer profile data
                        usernameTV.setText("Username: " + user.getUsername());
                        passwordTV.setText("Password: " + user.getPassword());
                        firstNameTV.setText("First Name: " + user.getFirstName());
                        lastNameTV.setText("Last Name: " + user.getLastName());
                        emailTV.setText("Email: " + user.getEmail());
                        phoneTV.setText("Phone Number: " + user.getPhoneNo());

                        //readAddressesFromFirebase();
                        retrieveAddressesFromFirebase();

                        //*******
                        //BUTTONS
                        //*******

                        //ORDER SHOVELLING BUTTON
                        btnOrderShoveling.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intentNewWO = new Intent(CustomerProfileActivity.this, CreateWorkOrderActivity.class);
                                String customerId = user.getUserId();
                                intentNewWO.putExtra("USER_ID", customerId);
                                startActivity(intentNewWO);
                            }
                        });

                        //MANAGE PAYMENT BUTTON
                        btnManagePaymentInfo.setOnClickListener(new View.OnClickListener() {
                            @Override

                            public void onClick(View view) {
                                /**
                                 Intent intentManagePayment = new Intent(CustomerProfileActivity.this, ManagePayemntActivity.class);
                                 String customerId = user.getUserId();
                                 intentManagePayment.putExtra("USER_ID", customerId);
                                 startActivity(intentManagePayment);
                                 **/
                            }
                        });

                        //ADD ADDRESS BUTTON
                        btnAddAddress.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intentNewAddress = new Intent(CustomerProfileActivity.this, CreateAddressActivity.class);
                                String customerId = user.getUserId();
                                intentNewAddress.putExtra("USER_ID", customerId);
                                startActivity(intentNewAddress);
                            }
                        });

                        //EDIT PASSWORD BUTTON
                        btnEditPassword.setOnClickListener(new View.OnClickListener() {
                            @Override

                            public void onClick(View view) {
                                 Intent intentEditPassword = new Intent(CustomerProfileActivity.this, EditPasswordActivity.class);
                                 String customerId = user.getUserId();
                                 intentEditPassword.putExtra("USER_ID", customerId);
                                 startActivity(intentEditPassword);
                            }
                        });

                        //VIEW RATINGS BUTTON
                        btnViewMyRatings.setOnClickListener(new View.OnClickListener() {
                            @Override

                            public void onClick(View view) {
                                /**
                                 Intent intentViewRatings = new Intent(CustomerProfileActivity.this, ViewRatingsActivity.class);
                                 String customerId = user.getUserId();
                                 intentViewRatings.putExtra("USER_ID", customerId);
                                 startActivity(intentViewRatings);
                                 **/
                            }
                        });

                        //Logout BUTTON
                        btnLogout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intentLogout = new Intent(CustomerProfileActivity.this, MainActivity.class);
                                startActivity(intentLogout);
                            }
                        });

                    } else {
                        //handle no user data
                    }
                } else {
                    //handle userid does not exist
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CustomerProfileActivity.this, "Could not create user. Please try again", Toast.LENGTH_SHORT).show();
            }
        });

    }


    /**
    private void readAddressesFromFirebase() {
        userTable.child("addresses").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                addressList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Address address = snapshot.getValue(Address.class);
                    if (address != null) {
                        addressList.add(address);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

    }
    **/

    private void retrieveAddressesFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("addresses");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check if the data is a list
                if (dataSnapshot.exists() && dataSnapshot.hasChildren() && dataSnapshot.getChildrenCount() > 1) {
                    List<Address> addressList = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Address address = snapshot.getValue(Address.class);
                        if (address != null) {
                            addressList.add(address);
                        }
                    }
                    // Handle the list of addresses
                    handleAddressList(addressList);
                } else if (dataSnapshot.exists() && dataSnapshot.hasChildren() && dataSnapshot.getChildrenCount() == 1) {
                    // Check if the data is a single item (HashMap)
                    Address address = dataSnapshot.child("uniqueKey").getValue(Address.class);
                    if (address != null) {
                        // Handle the single address
                        handleSingleAddress(address);
                    }
                } else {
                    // Handle the case when there is no data
                    handleNoData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    private void handleAddressList(List<Address> addressList) {
        // Your logic for handling a list of addresses
        System.out.println("I guess there is a list of addresses here");
    }

    private void handleSingleAddress(Address address) {
        // Your logic for handling a single address
        System.out.println("I guess there is only 1 address here");
    }

    private void handleNoData() {
        // Your logic for handling the case when there is no data
        System.out.println("I guess there were no addresses listed here");
    }

    // Other methods and code in your activity...
}



