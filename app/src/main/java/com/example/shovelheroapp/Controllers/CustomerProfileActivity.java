package com.example.shovelheroapp.Controllers;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

//THIS IS ANOTHER TEST

public class CustomerProfileActivity extends AppCompatActivity {

    private static final String TAG = "CustomerProfileActivity";

    //initialize ShovelHeroDB userTable(Firebase)
    DatabaseReference userTable;

    //Pending Work Order listings
    private RecyclerView pendingWORecyclerView;
    private WorkOrderAdapterForCustomer workOrderAdapter;
    private List<WorkOrder> pendingWorkOrderList;


    private TextView usernameTV;
    private TextView firstNameTV;
    private TextView lastNameTV;
    private DatePicker birthdateDatePicker;
    private TextView emailTV;
    private TextView phoneTV;
    private Spinner addressSpinner;
    private User user;
    private String userId;



    //buttons
    Button btnAddAddress;
    Button btnOrderShoveling;
    Button btnEditProfile;
    Button btnManagePaymentInfo;
    Button btnEditPassword;
    Button btnViewMyRatings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_customer);

        //Instantiate userTable to current listing
        userTable = FirebaseDatabase.getInstance().getReference("users");

        usernameTV = findViewById(R.id.tvUsername);
        firstNameTV = findViewById(R.id.tvFirstName);
        lastNameTV = findViewById(R.id.tvLastname);
        emailTV = findViewById(R.id.tvEmail);
        phoneTV = findViewById(R.id.tvPhone);
        addressSpinner = findViewById(R.id.spinnerAddress);

        btnOrderShoveling = findViewById(R.id.btnOrderShoveling);
        btnManagePaymentInfo = findViewById(R.id.btnManagePaymentInfo);
        btnAddAddress = findViewById(R.id.btnAddAddress);
        btnEditProfile = findViewById(R.id.btnEditUserInfo);
        btnEditPassword = findViewById(R.id.btnEditPassword);
        btnViewMyRatings = findViewById(R.id.btnViewMyRatings);

        DatabaseReference workOrderReference = FirebaseDatabase.getInstance().getReference("workorders");


        //GET USERID FROM LOGIN OR REGISTRATION
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("USER_ID");
            if (userId != null) {
                System.out.println("customer ID recieved: " + userId);  //WORKING
                retrieveCustomerProfileData(userId);
            }
        }

        //initialize recyclerview
        pendingWORecyclerView = findViewById(R.id.rvPendingWorkOrders);
        pendingWORecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //initialize Pending Work Order list and Adapter
        pendingWorkOrderList = new ArrayList<>();
        workOrderAdapter = new WorkOrderAdapterForCustomer(this, pendingWorkOrderList, userId);
        pendingWORecyclerView.setAdapter(workOrderAdapter);

        //ADD PENDING WORK ORDERS TO PROFILE
        workOrderReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pendingWorkOrderList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    WorkOrder workOrder = snapshot.getValue(WorkOrder.class);
                    if ((workOrder.getStatus().equals(Status.Open.toString()) ||
                            workOrder.getStatus().equals(Status.OpenCustom.toString()) ||
                            workOrder.getStatus().equals(Status.PendingGuardianApproval.toString()) ||
                            workOrder.getStatus().equals(Status.Accepted.toString()) ||
                            workOrder.getStatus().equals(Status.Enroute.toString()) ||
                            workOrder.getStatus().equals(Status.InProgress.toString()) ||
                            workOrder.getStatus().equals(Status.Issue.toString()) )
                        && workOrder.getCustomerId().equals(userId)) {
                        pendingWorkOrderList.add(workOrder);
                    }
                    else {
                        Toast.makeText(CustomerProfileActivity.this, "No Open Jobs", Toast.LENGTH_SHORT).show();
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


        //Navigation Bar Activity
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationViewCustomer);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_workorders) {
                startActivity(new Intent(CustomerProfileActivity.this, ListAllOpenWorkOrdersActivity.class));
                return true;
            } else if (itemId == R.id.menu_orderhistory) {
                startActivity(new Intent(CustomerProfileActivity.this, OrderHistoryActivity.class));
                return true;
            } else if (itemId == R.id.menu_logout) {
                startActivity(new Intent(CustomerProfileActivity.this, MainActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void retrieveCustomerProfileData(String userId) {
        System.out.println("customer ID recieved to retrieve cx profile: " + userId);  //WORKING

        userTable.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    user = snapshot.getValue(User.class);

                    if (user != null) {
                        //display user profile data
                        usernameTV.setText("Username: " + user.getUsername());
                        firstNameTV.setText("Name: " + user.getFirstName());
                        lastNameTV.setText(" " + user.getLastName());
                        emailTV.setText("Email: " + user.getEmail());
                        phoneTV.setText("Phone Number: " + user.getPhoneNo());

                        System.out.println("User data loaded: " + user.getUsername());
                        System.out.println("Sending userid to read addresses: " + user);

                        // Load profile Image
                        String profileImageUrl = user.getProfilePictureUrl();
                        ImageView profileImageView = findViewById(R.id.imgProfilePicture);
                        if(profileImageUrl != null && !profileImageUrl.isEmpty()){
                            Glide.with(CustomerProfileActivity.this)
                                    .load(profileImageUrl).into(profileImageView);
                        }

                        readAddressesFromFirebase(user);


                        //*******
                        //CUSTOMER BUTTONS
                        //*******

                        //ORDER SHOVELLING BUTTON
                        btnOrderShoveling.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v){
                                Address selectedAddress = (Address) addressSpinner.getSelectedItem();
                                createWorkOrder(user.getUserId(), selectedAddress);
                            }
                        });

                        btnEditProfile.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(CustomerProfileActivity.this, "Temp msg: Manage Youth activity under construction", Toast.LENGTH_SHORT).show();
                                Intent intentManageCustomerProfile = new Intent(CustomerProfileActivity.this, EditUserProfileActivity.class);
                                String youthId = user.getUserId();
                                intentManageCustomerProfile.putExtra("USER_ID", youthId);
                                startActivity(intentManageCustomerProfile);
                                overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
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
                                 overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
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
                                overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
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
                                overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
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
                                 overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                                 **/
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
                ArrayAdapter<Address> addressAdapter = new ArrayAdapter<>(CustomerProfileActivity.this, android.R.layout.simple_spinner_item, addresses);
                addressAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                addressSpinner.setAdapter(addressAdapter);

                // Enable or disable the "Create Work Order" button based on the presence of addresses
                btnOrderShoveling.setEnabled(!addresses.isEmpty());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

    }

    public void createWorkOrder(String userId, Address address) {
        if (address.getAddress() == null) {
            Toast.makeText(this, "Please select a valid address from your list", Toast.LENGTH_SHORT).show();
        } else {
            //create WO item in firebase
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference workOrderReference = database.getReference("workorders");

            //work order elements
            String workOrderID = workOrderReference.push().getKey();
            
            // Format current date as String for Firebase (avoid data type mismatch)
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String requestDate = dateFormat.format(Calendar.getInstance().getTime());
            String status = "Started";
            int squareFootage = address.getDrivewaySquareFootage();
            String addressId = address.getAddressId();

            //create new work order
            WorkOrder newWO = new WorkOrder(workOrderID, requestDate, status, squareFootage, userId, addressId);

            assert workOrderID != null;
            workOrderReference.child(workOrderID).setValue(newWO)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(CustomerProfileActivity.this, "New Shovelling Request Created", Toast.LENGTH_SHORT).show();

                        Intent intentCreateWO = new Intent(CustomerProfileActivity.this, CreateWorkOrderActivity.class);
                        intentCreateWO.putExtra("WORK_ORDER_ID", workOrderID);
                        startActivity(intentCreateWO);
                        overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                    })
                    .addOnFailureListener(e -> Toast.makeText(CustomerProfileActivity.this, "Could not create Shovelling Job", Toast.LENGTH_SHORT).show());
        }
    }
}



