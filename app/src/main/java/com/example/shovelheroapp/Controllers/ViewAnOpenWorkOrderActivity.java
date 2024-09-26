package com.example.shovelheroapp.Controllers;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.shovelheroapp.Models.User;
import com.example.shovelheroapp.Models.WorkOrder;
import com.example.shovelheroapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;


public class ViewAnOpenWorkOrderActivity extends AppCompatActivity {

    private static final String TAG = "ViewAnOpenWorkOrderActivity";

    //initialize Firebase
    DatabaseReference workOrderTable;
    DatabaseReference userTable;

    private TextView tvStatus;
    private TextView tvSquareFootage;
    private TextView tvCustomerUsername;
    private TextView tvShovellerUsername;
    private TextView tvAddress;
    private TextView tvRequestDateTime;
    private TextView tvRequestedDate;
    private TextView tvRequestedTime;
    private TextView tvItemsRequested;
    private TextView tvSpecialInstructions;

    //work order references
    private String woId;    //from intent
    private WorkOrder currentWorkOrder;
    private User currentUser;
    private String customerId;
    private String shovellerId;

    //add arrival image
    //add completion image
    //add issue image


    //Buttons
    Button btnDirections;
    Button btnTakeIt;
    Button btnCancel;
    Button btnEnroute;
    Button btnLate;
    Button btnArrived;
    Button btnCompleted;
    Button btnIssueReported;
    Button btnApprove;
    Button btnReject;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_order_view_an_open);

        //instantiate Firebase
        workOrderTable = FirebaseDatabase.getInstance().getReference("workorders");
        userTable = FirebaseDatabase.getInstance().getReference("users");

       tvCustomerUsername = findViewById(R.id.tvCustomerUsername);
       tvShovellerUsername = findViewById(R.id.tvShovellerUsername);
       tvStatus = findViewById(R.id.tvStatus);
       //**TODO: address image here
        tvAddress = findViewById(R.id.tvAddress);
        tvItemsRequested = findViewById(R.id.tvItemsRequested);
        tvSpecialInstructions = findViewById(R.id.tvSpecialNotes);
        tvCustomerUsername = findViewById(R.id.tvCustomerUsername);
        tvShovellerUsername = findViewById(R.id.tvShovellerUsername);
        tvSquareFootage = findViewById(R.id.tvSquareFootage);
        tvRequestDateTime = findViewById(R.id.tvRequestDateTime);
        tvRequestedDate = findViewById(R.id.tvRequestedDate);
        tvRequestedTime = findViewById(R.id.tvRequestedTime);

        btnDirections = findViewById(R.id.btnDirections);
        /**
        btnTakeIt = findViewById(R.id.btnTakeIt);
        btnCancel = findViewById(R.id.btnCancel);
        btnEnroute = findViewById(R.id.btnEnroute);
        btnLate = findViewById(R.id.btnLate);
        btnArrived = findViewById(R.id.btnArrived);
        btnCompleted = findViewById(R.id.btnCompleted);
        btnIssueReported = findViewById(R.id.btnIssueReport);
         **/


        //GET WOID FROM WORK ORDER LIST
        Intent intent = getIntent();
        if (intent != null) {
            woId = intent.getStringExtra("WO_ID");
            if (woId != null) {
                System.out.println("work order ID recieved: " + woId);  //WORKING
                retrieveWorkOrderData(woId);
            }
        }

        //Navigation Bar Activity
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationViewCustomer);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_workorders) {
                startActivity(new Intent(ViewAnOpenWorkOrderActivity.this, ListAllOpenWorkOrdersActivity.class));
                return true;
            } else if (itemId == R.id.menu_orderhistory) {
                startActivity(new Intent(ViewAnOpenWorkOrderActivity.this, OrderHistoryActivity.class));
                return true;
            } else if (itemId == R.id.menu_logout) {
                startActivity(new Intent(ViewAnOpenWorkOrderActivity.this, MainActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void retrieveWorkOrderData(String woID) {
        System.out.println("work order ID received to retrieve wo info: " + woID);

        workOrderTable.child(woID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentWorkOrder = snapshot.getValue(WorkOrder.class);
                    if (currentWorkOrder != null) {
                        tvStatus.setText(currentWorkOrder.getStatus());
                        String squareFeet = String.valueOf(currentWorkOrder.getSquareFootage());
                        tvSquareFootage.setText(squareFeet + " Square Feet");
                        tvRequestDateTime.setText(currentWorkOrder.getRequestDate());
                        tvRequestedDate.setText(currentWorkOrder.getRequestedDate());
                        tvRequestedTime.setText(currentWorkOrder.getRequestedTime());
                        //**TODO: tvItemsRequested.setText(currentWorkOrder.getItemsRequested);
                        tvSpecialInstructions.setText(currentWorkOrder.getSpecialInstructions());

                        readAddressFromFirebase(currentWorkOrder);
                        getCustomerUsername(currentWorkOrder);

                        if(currentWorkOrder.getShovellerId() != null) {
                            getShovellerUsername(currentWorkOrder);
                        }


                        // **TODO:Load profile Image
                        /**
                        //**TODO: String addressImageUrl = currentWorkOrder.getAddressImageUrl;
                        ImageView addressImageView = findViewById(R.id.imgPropertyImage);
                        if(addressImageUrl != null && !addressImageUrl.isEmpty()){
                            Glide.with(CustomerProfileActivity.this)
                                    .load(addressImageUrl).into(addressImageView);
                        }
                         **/



                        //*******************************************************************
                        //TODO:BUTTONS   ****PLEASE DO NOT DELETE EXTRA CODE: ADDED FOE REUSE
                        /**

                        //GPS DIRECTIONS
                        btnDirections.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(ViewAnOpenWorkOrderActivity.this, "Getting Directions", Toast.LENGTH_SHORT).show();
                                //**TODO: Add GPS API to link directions
                            }
                        });

                        //"TAKE IT" REQUEST FROM SHOVELLER
                        btnTakeIt.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(ViewAnOpenWorkOrderActivity.this, "Shovelling job has been sent for approval from your Guardian!", Toast.LENGTH_SHORT).show();
                                currentWorkOrder.setStatus(Status.PendingGuardianApproval.toString());
                                currentWorkOrder.setShovellerId(currentUser.getUserId());
                                //**TODO: requestGuardianApproval(currentWorkOrder.getShovellerId());
                            }
                        });



                        //CANCEL JOB FROM SHOVELLER - PUT BACK INTO OPEN QUEUE & REMOVE SHOVELLER ID
                        btnCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                currentWorkOrder.setStatus(Status.Open.toString());
                                currentWorkOrder.setShovellerId(null);
                                Toast.makeText(ViewAnOpenWorkOrderActivity.this, "Work Order released back into main list", Toast.LENGTH_SHORT).show();
                            }
                        });



                        btnApprove.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                currentWorkOrder.setStatus(Status.Accepted.toString());
                                //**TODO: notify youth
                                //**TODO: notify customer
                                //**TODO: currentWorkOrder.setShovellerId(wo linked you id);
                                //**TODO: currentWorkOrder.setGuardianId(linked guardian id);
                                Toast.makeText(ViewAnOpenWorkOrderActivity.this, "Shovelling job has now been approved. Please plan to arrive onsite at the specified time.", Toast.LENGTH_SHORT).show();
                            }
                        });


                        btnReject.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                currentWorkOrder.setStatus(Status.Open.toString());
                                //**TODO: notify youth
                                //**TODO: block youth from requesting again
                                Toast.makeText(ViewAnOpenWorkOrderActivity.this, "This shovelling job has been rejected. Work order has been re-added to the main list.", Toast.LENGTH_SHORT).show();

                            }
                        });

                        btnEnroute.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                currentWorkOrder.setStatus(Status.Enroute.toString());
                                //**TODO: notify customer
                                //**TODO: remove customer option to cancel order
                                Toast.makeText(ViewAnOpenWorkOrderActivity.this, "The customer has been notified that you will arrive within 20 minutes", Toast.LENGTH_SHORT).show();
                            }
                        });

                        btnArrived.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                currentWorkOrder.setStatus(Status.InProgress.toString());
                                //**TODO: notify customer
                                Toast.makeText(ViewAnOpenWorkOrderActivity.this, "Welcome to the job! Please ensure you shovel at a comfortable pace and stay safe!", Toast.LENGTH_SHORT).show();
                            }
                        });

                        btnIssueReported.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //**TODO: open menu for type of issue
                                //**TODO: guardian live chat to app if needed?
                                //**TODO: able to take images and video
                                //**TODO: decision to keep WO active/cancelled, refund/no refund
                                Toast.makeText(ViewAnOpenWorkOrderActivity.this, "An issue has been reported", Toast.LENGTH_SHORT).show();
                            }
                        });
                        **/


                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    //READ ADDRESS FROM REALTIMEDB - DONE
    private void readAddressFromFirebase(WorkOrder workOrder) {
        System.out.println("workOrderId received to retrieve address: " + workOrder);

        DatabaseReference addressReference = userTable.child(workOrder.getCustomerId()).child("addresses").child(workOrder.getCustomerAddressId());
        System.out.println("addressed referenced for address, ID: " + addressReference.getKey()); //RETRIEVES OK

        addressReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    HashMap<String, Object> addressMap = (HashMap<String, Object>) snapshot.getValue();

                    //retrieve values from hashmap
                    String address = (String) addressMap.get("address");
                    String city = (String) addressMap.get("city");
                    String province = (String) addressMap.get("province");

                    tvAddress.setText(address + ", " + city + ", " + province);
                    System.out.println("Address retrieved from Firebase: " + address);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //handle error
            }
        });
    }

    // Validation function for UserName
    private void getCustomerUsername(WorkOrder currentWorkOrder) {
        System.out.println("Adding Customer Usernames for WO: " + currentWorkOrder.getWorkOrderId());
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("users").child(currentWorkOrder.getCustomerId());

        customerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    HashMap<String, Object> userMap = (HashMap<String, Object>) snapshot.getValue();

                    //retrieve values from hashmap
                    String customerUsername = (String) userMap.get("username");

                    tvCustomerUsername.setText(customerUsername);
                    System.out.println("Customer Username retrieved from Firebase: " + customerUsername);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //handle error
            }
        });
    }

    // Validation function for UserName
    private void getShovellerUsername(WorkOrder currentWorkOrder) {
        System.out.println("Adding Shoveller Usernames for WO: " + currentWorkOrder.getWorkOrderId());
        DatabaseReference shovellerRef = FirebaseDatabase.getInstance().getReference("users").child(currentWorkOrder.getShovellerId());
        shovellerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.exists()) {
                        HashMap<String, Object> userMap = (HashMap<String, Object>) snapshot.getValue();

                        //retrieve values from hashmap
                        String ShovellerUsername = (String) userMap.get("username");
                        tvShovellerUsername.setText(ShovellerUsername);
                        System.out.println("Customer Username retrieved from Firebase: " + ShovellerUsername);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //handle error
            }
        });
    }
}