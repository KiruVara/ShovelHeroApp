package com.example.shovelheroapp.Controllers;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shovelheroapp.Models.Address;
import com.example.shovelheroapp.Models.Enums.Status;
import com.example.shovelheroapp.Models.User;
import com.example.shovelheroapp.Models.WorkOrder;
import com.example.shovelheroapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class YouthShovelerProfileActivity extends AppCompatActivity {

    private static final String TAG = "YouthShovelerProfileActivity";

    //initialize ShovelHeroDB (Firebase)
    DatabaseReference userTable;


    //Pending Work Order listings
    private RecyclerView pendingWORecyclerView;
    private WorkOrderAdapterForShoveler workOrderAdapter;
    private List<WorkOrder> pendingWorkOrderList;


    private TextView usernameTV;
    private TextView firstNameTV;
    private TextView lastNameTV;
    private TextView emailTV;
    private TextView phoneTV;
    private String userId;
    private String guardianId;


    private Spinner addressSpinner;

    //Navigation
    private BottomNavigationView bottomNavigationView;


    //buttons
    Button btnViewJobs;
    Button btnManagePaymentInfo;
    Button btnManageProfileInfo;
    Button btnAddAddress;
    Button btnEditPassword;
    Button btnViewRatings;
    Button btnMyGuardian;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_youth_shoveler);

        userTable = FirebaseDatabase.getInstance().getReference("users");

        usernameTV = findViewById(R.id.tvUsername);
        firstNameTV = findViewById(R.id.tvFirstName);
        lastNameTV = findViewById(R.id.tvLastname);
        emailTV = findViewById(R.id.tvEmail);
        phoneTV = findViewById(R.id.tvPhone);
        addressSpinner = findViewById(R.id.spinnerAddress);

        btnViewJobs = findViewById(R.id.btnViewJobs);
        btnManagePaymentInfo = findViewById(R.id.btnManagePaymentInfo);
        btnManageProfileInfo = findViewById(R.id.btnManageProfileInfo);
        btnAddAddress = findViewById(R.id.btnAddAddress);
        btnEditPassword = findViewById(R.id.btnEditPassword);
        btnViewRatings = findViewById(R.id.btnViewRatings);

        //**TODO: to remove
        btnMyGuardian = findViewById(R.id.btnViewGuardian);


        //GET USERID FROM LOGIN OR REGISTRATION
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("USER_ID");
            if (userId != null) {
                System.out.println("shoveller ID recieved: " + userId);  //WORKING
                retrieveYouthProfile(userId);
            }
        }

        //initialize recyclerview
        System.out.println("Initializing Pending Orders Recycler");
        pendingWORecyclerView = findViewById(R.id.rvPendingWorkOrders);
        pendingWORecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //initialize Pending Work Order list and Adapter
        pendingWorkOrderList = new ArrayList<>();
        workOrderAdapter = new WorkOrderAdapterForShoveler(this, pendingWorkOrderList, userId);
        pendingWORecyclerView.setAdapter(workOrderAdapter);

        //ADD PENDING WORK ORDERS TO PROFILE
        DatabaseReference workOrderReference = FirebaseDatabase.getInstance().getReference("workorders");
        workOrderReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pendingWorkOrderList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    WorkOrder workOrder = snapshot.getValue(WorkOrder.class);

                    System.out.println("userId = " + userId);
                    System.out.println("shovellerId = " + workOrder.getShovellerId());

                    if(workOrder.getShovellerId() != null &&
                        workOrder.getShovellerId().equals(userId) &&
                        (workOrder.getStatus().equals(Status.Open.toString()) ||
                            workOrder.getStatus().equals(Status.OpenCustom.toString()) ||
                            workOrder.getStatus().equals(Status.PendingGuardianApproval.toString()) ||
                            workOrder.getStatus().equals(Status.Accepted.toString()) ||
                            workOrder.getStatus().equals(Status.Enroute.toString()) ||
                            workOrder.getStatus().equals(Status.InProgress.toString()) ||
                            workOrder.getStatus().equals(Status.Issue.toString()) ) )
                    {
                        pendingWorkOrderList.add(workOrder);
                    }
                    else {
                        Toast.makeText(YouthShovelerProfileActivity.this, "No Open Jobs", Toast.LENGTH_SHORT).show();
                    }
                }
                Log.d("ListAllOpenWorkOrders", "Data size: " + pendingWorkOrderList.size());
                workOrderAdapter.notifyDataSetChanged();
                pendingWORecyclerView.setAdapter(workOrderAdapter);
                Log.d("ListAllOpenWorkOrders", "Adapter notified of data change");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ListAllOpenWorkOrders", "Error fetching data: " + error.getMessage());
                error.toException().printStackTrace(); // Print stack trace for detailed error info
            }
        });

        //updateMenuVisibility();

        //Navigation Bar Activity
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationViewYouthShoveler);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.menu_workorders) {
                startActivity(new Intent(YouthShovelerProfileActivity.this, ListAllOpenWorkOrdersActivity.class));
                return true;
            } else if (itemId == R.id.menu_orderhistory) {
                startActivity(new Intent(YouthShovelerProfileActivity.this, OrderHistoryActivity.class));
                return true;
            } else if (itemId == R.id.menu_logout) {
                startActivity(new Intent(YouthShovelerProfileActivity.this, MainActivity.class));
                finish();
                return true;

                //**TODO: FLEXIBLE NAV BAR
                /**
                 } else if (itemId == R.id.menu_shovellerprofile) {
                 startActivity(new Intent(YouthShovelerProfileActivity.this, YouthShovelerProfileActivity.class));
                 return true;
                 } else if (itemId == R.id.menu_guardianprofile) {
                 startActivity(new Intent(YouthShovelerProfileActivity.this, GuardianProfileActivity.class));
                 return true;
                 } else if (itemId == R.id.menu_customerprofile) {
                 startActivity(new Intent(YouthShovelerProfileActivity.this, CustomerProfileActivity.class));
                 return true;
                 **/
            }
            return false;
        });
    }


    private void retrieveYouthProfile(String userId) {
        System.out.println("Retrieving youth profile! - line 202");
        userTable.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);

                    if (user != null) {
                        //display user profile info
                        usernameTV.setText("Username: " + user.getUsername());
                        System.out.println("Retrieve youth username! - line 212: " + user.getUsername());
                        firstNameTV.setText("First Name: " + user.getFirstName());
                        lastNameTV.setText(" " + user.getLastName());
                        emailTV.setText("Email: " + user.getEmail());
                        phoneTV.setText("Phone Number: " + user.getPhoneNo());

                        // Load profile Image
                        String profileImageUrl = user.getProfilePictureUrl();
                        ImageView profileImageView = findViewById(R.id.imgProfilePicture);
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(YouthShovelerProfileActivity.this)
                                    .load(profileImageUrl).into(profileImageView);
                        }

                        readAddressesFromFirebase(user);

                        if (user.getAddresses() == null) {
                            System.out.println("Please add your address to view local job listings");
                            Toast.makeText(YouthShovelerProfileActivity.this, "Please add your address to view local job listings", Toast.LENGTH_SHORT).show();
                        }


                        //*******
                        //YOUTH SHOVELLER BUTTONS
                        //*******

                        //VIEW JOBS BUTTON
                        btnViewJobs.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intentViewYouthJobs = new Intent(YouthShovelerProfileActivity.this, ListAllOpenWorkOrdersActivity.class);
                                String youthId = user.getUserId();
                                intentViewYouthJobs.putExtra("USER_ID", youthId);
                                startActivity(intentViewYouthJobs);
                                overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                            }
                        });

                        //MANAGE PAYMENT BUTTON
                        btnManagePaymentInfo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(YouthShovelerProfileActivity.this, "Temp msg: Manage Payment activity under construction", Toast.LENGTH_SHORT).show();

                                Intent intentManageYouthPayment = new Intent(YouthShovelerProfileActivity.this, ManagePaymentActivity.class);
                                String youthId = user.getUserId();
                                intentManageYouthPayment.putExtra("USER_ID", youthId);
                                startActivity(intentManageYouthPayment);
                                overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                            }
                        });

                        //MANAGE YOUTH PROFILE BUTTON
                        btnManageProfileInfo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(YouthShovelerProfileActivity.this, "Temp msg: Manage Youth activity under construction", Toast.LENGTH_SHORT).show();
                                Intent intentManageYouthProfile = new Intent(YouthShovelerProfileActivity.this, EditUserProfileActivity.class);
                                String youthId = user.getUserId();
                                intentManageYouthProfile.putExtra("USER_ID", youthId);
                                startActivity(intentManageYouthProfile);
                                overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                            }
                        });

                        //ADD ADDRESS BUTTON
                        btnAddAddress.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intentNewAddress = new Intent(YouthShovelerProfileActivity.this, CreateAddressActivity.class);
                                String customerId = user.getUserId();
                                intentNewAddress.putExtra("USER_ID", customerId);
                                startActivity(intentNewAddress);
                                overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                            }
                        });

                        //EDIT PASSWORD BUTTON
                        btnEditPassword.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intentEditPassword = new Intent(YouthShovelerProfileActivity.this, EditPasswordActivity.class);
                                String youthId = user.getUserId();
                                intentEditPassword.putExtra("USER_ID", youthId);
                                startActivity(intentEditPassword);
                                overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                            }
                        });

                        //VIEW RATINGS BUTTON
                        btnViewRatings.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(YouthShovelerProfileActivity.this, "Temp msg: View ratings activity under construction", Toast.LENGTH_SHORT).show();

                                /**
                                 Intent intentViewRatings = new Intent(YouthShovelerProfileActivity.this, ViewRatingsActivity.class);
                                 String youthId = user.getUserId();
                                 intentViewRatings.putExtra("USER_ID", youthId);
                                 startActivity(intentViewRatings);
                                 overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                                 **/
                            }
                        });
                    } else {
                        //handle no user data error
                    }
                } else {
                    //handle user id does not exist
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //handle error
            }
        });
    }

    private void readAddressesFromFirebase(User user) {
        System.out.println("userid received by read database: " + user);

        userTable.child(user.getUserId()).child("addresses").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Clear the addresses field in the User class
                user.setAddresses(new HashMap<String, Address>());

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    //manually deserialize the HashMap
                    Map<String, Object> addressMap = (Map<String, Object>) snapshot.getValue();

                    // retrieve values from the Hashmap
                    String addressId = (String) addressMap.get("addressId");
                    String address = (String) addressMap.get("address");
                    String city = (String) addressMap.get("city");
                    String province = (String) addressMap.get("province");
                    String postalCode = (String) addressMap.get("postalCode");
                    String country = (String) addressMap.get("country");
                    int drivewaySquareFootage = ((Long) addressMap.get("drivewaySquareFootage")).intValue();

                    // Create new Address object
                    Address addressObject = new Address(addressId, address, city, province, postalCode, country, drivewaySquareFootage);

                    // Add the Address object to the addresses HashMap in User model
                    user.addAddress(addressId, addressObject);

                }
                // Retrieve list of addresses from Hashmap
                List<Address> addresses = new ArrayList<>(user.getAddresses().values());

                // Update the Spinner with addresses
                ArrayAdapter<Address> addressAdapter = new ArrayAdapter<>(YouthShovelerProfileActivity.this, android.R.layout.simple_spinner_item, addresses);
                addressAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                addressSpinner.setAdapter(addressAdapter);

                // Enable or disable the "Create Work Order" button based on the presence of addresses
                btnViewJobs.setEnabled(!addresses.isEmpty());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    // **TODO: FLEXIBLE NAV BARS - Update available navigation menu buttons
    /**
    private void updateMenuVisibility() {
        Menu menu = bottomNavigationView.getMenu();
            menu.findItem(R.id.menu_shovellerprofile).setVisible(true);
            menu.findItem(R.id.menu_guardianprofile).setVisible(false);
            menu.findItem(R.id.menu_customerprofile).setVisible(false);
            menu.findItem(R.id.menu_workorders).setVisible(false);
            menu.findItem(R.id.menu_orderhistory).setVisible(true);
            menu.findItem(R.id.menu_logout).setVisible(true);
    }
     **/
}


