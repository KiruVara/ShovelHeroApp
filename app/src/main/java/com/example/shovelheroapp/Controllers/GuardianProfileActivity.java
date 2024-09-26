package com.example.shovelheroapp.Controllers;

import com.example.shovelheroapp.Models.Retrofit.RetrofitClient;
import com.example.shovelheroapp.Models.Retrofit.CloudFunctionsService;
import com.example.shovelheroapp.Models.Retrofit.GuardianIdInformation;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuardianProfileActivity extends AppCompatActivity {
    private static final String TAG = "GuardianProfileActivity";

    //initialize Firebase
    DatabaseReference userTable;


    //Pending Work Order listings
    private RecyclerView pendingWORecyclerView;
    private WorkOrderAdapterForGuardian workOrderAdapter;
    private List<WorkOrder> pendingWorkOrderList;


    private TextView usernameTV;
    private TextView firstNameTV;
    private TextView lastNameTV;
    private TextView emailTV;
    private TextView phoneTV;


    private Spinner addressSpinner;
    private Spinner linkedYouthSpinner;
    private String userId;


    EditText addYouthET;


    //guardian ID
    private ImageButton btnAddIDPicture;
    private Uri guardianIdImageUri;

    // Current User
    private User currentUser;
    private Bitmap currentBitmap = null;


    //buttons
    Button btnAddYouth;
    Button btnViewYouthProfile;
    Button btnViewRatings;
    Button btnManagePaymentInfo;
    Button btnManageProfileInfo;
    Button btnAddAddress;
    Button btnEditPassword;
    Button btnSendGuardianId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_guardian);

        userTable = FirebaseDatabase.getInstance().getReference("users");

        usernameTV = findViewById(R.id.tvUsername);
        firstNameTV = findViewById(R.id.tvFirstName);
        lastNameTV = findViewById(R.id.tvLastname);
        emailTV = findViewById(R.id.tvEmail);
        phoneTV = findViewById(R.id.tvPhone);

        btnAddIDPicture = findViewById(R.id.btnAddIDPicture);


        addressSpinner = findViewById(R.id.spinnerAddress);

        addYouthET = findViewById(R.id.etAddYouth);
        btnAddYouth = findViewById(R.id.btnAddYouth);
        linkedYouthSpinner = findViewById(R.id.spinnerYouths);

        btnViewYouthProfile = findViewById(R.id.btnViewYouthProfile);
        btnViewRatings = findViewById(R.id.btnViewRatings);

        btnManagePaymentInfo = findViewById(R.id.btnManagePaymentInfo);
        btnManageProfileInfo = findViewById(R.id.btnManageProfileInfo);
        btnAddAddress = findViewById(R.id.btnAddAddress);
        btnEditPassword = findViewById(R.id.btnEditPassword);

        btnSendGuardianId = findViewById(R.id.btnSendGuardianId);
        btnSendGuardianId.setVisibility(View.GONE);

        // Click listener for Guardian ID
        btnSendGuardianId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadIdImage();
            }
        });

        btnAddIDPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        //get Username from registration page or or UserID from Login
        //GET USERID FROM LOGIN OR REGISTRATION
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("USER_ID");
            if (userId != null) {
                retrieveGuardianProfile(userId);
            }
        }

        //LIST OF OPEN ORDERS FOR GUARDIAN
        //initialize recyclerview
        System.out.println("Initializing Pending Orders Recycler");
        pendingWORecyclerView = findViewById(R.id.rvPendingWorkOrders);
        pendingWORecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //initialize Pending Work Order list and Adapter
        pendingWorkOrderList = new ArrayList<>();
        workOrderAdapter = new WorkOrderAdapterForGuardian(this, pendingWorkOrderList, userId);
        pendingWORecyclerView.setAdapter(workOrderAdapter);

        //ADD PENDING WORK ORDERS TO PROFILE
        DatabaseReference workOrderReference = FirebaseDatabase.getInstance().getReference("workorders");
        workOrderReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pendingWorkOrderList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    WorkOrder workOrder = snapshot.getValue(WorkOrder.class);

                    //System.out.println("userId = " + userId);
                    //System.out.println("shovellerId = " + workOrder.getShovellerId());

                    if (workOrder.getGuardianId() != null &&
                            workOrder.getGuardianId().equals(userId) &&
                            (workOrder.getStatus().equals(Status.PendingGuardianApproval.toString()) ||
                                    workOrder.getStatus().equals(Status.Accepted.toString()) ||
                                    workOrder.getStatus().equals(Status.Enroute.toString()) ||
                                    workOrder.getStatus().equals(Status.InProgress.toString()) ||
                                    workOrder.getStatus().equals(Status.Issue.toString()))
                    ) {
                        pendingWorkOrderList.add(workOrder);
                    } else {
                        Toast.makeText(GuardianProfileActivity.this, "No Open Jobs", Toast.LENGTH_SHORT).show();
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
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationViewGuardian);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_workorders) {
                startActivity(new Intent(GuardianProfileActivity.this, ListAllOpenWorkOrdersActivity.class));
                return true;
            } else if (itemId == R.id.menu_orderhistory) {
                startActivity(new Intent(GuardianProfileActivity.this, OrderHistoryActivity.class));
                return true;
            } else if (itemId == R.id.menu_logout) {
                startActivity(new Intent(GuardianProfileActivity.this, MainActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }


    private void retrieveGuardianProfile(String userId) {
        userTable.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUser = snapshot.getValue(User.class);

                    if (currentUser != null) {
                        //display user profile info
                        usernameTV.setText("Username: " + currentUser.getUsername());
                        firstNameTV.setText("First Name: " + currentUser.getFirstName());
                        lastNameTV.setText(" " + currentUser.getLastName());
                        emailTV.setText("Email: " + currentUser.getEmail());
                        phoneTV.setText("Phone Number: " + currentUser.getPhoneNo());


                        // Load profile Image
                        String profileImageUrl = currentUser.getProfilePictureUrl();
                        ImageView profileImageView = findViewById(R.id.imgProfilePicture);
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(GuardianProfileActivity.this)
                                    .load(profileImageUrl).into(profileImageView);
                        }

                        readAddressesFromFirebase(currentUser);

                        if (!currentUser.getGuardianIdValidated()) {
                            System.out.println("Please add your Picture ID to get started");
                            Toast.makeText(GuardianProfileActivity.this, "Valid Photo ID is required to add a Youth Shoveller to your profile", Toast.LENGTH_SHORT).show();
                        } else {
                            readYouthProfilesFromFirebase(currentUser);
                            retrieveLinkedYouths(currentUser);

                        }


                        //ADD YOUTH BUTTON
                        btnAddYouth.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //if(user.getGuardianIdValidated()){
                                System.out.println("this is the guardian being sent to link the youth: " + currentUser.getUsername());
                                linkYouthProfile(currentUser);
                                //} else {
                                //  Toast.makeText(GuardianProfileActivity.this, "Please ensure your ID has been uploaded and validated", Toast.LENGTH_SHORT).show();
                                //}
                            }
                        });

                        //VIEW RATINGS BUTTON
                        btnViewRatings.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(GuardianProfileActivity.this, "Temp msg: View ratings activity under construction", Toast.LENGTH_SHORT).show();

                                /**
                                 Intent intentViewRatings = new Intent(YouthShovelerProfileActivity.this, ViewRatingsActivity.class);
                                 String guardianId = user.getUserId();
                                 intentViewRatings.putExtra("USER_ID", guardianId);
                                 startActivity(intentViewRatings);
                                 overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                                 **/
                            }
                        });

                        //MANAGE PROFILE BUTTON
                        btnManageProfileInfo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(GuardianProfileActivity.this, "Temp msg: Manage user profile under construction", Toast.LENGTH_SHORT).show();
                                Intent intentManageYouthProfile = new Intent(GuardianProfileActivity.this, EditUserProfileActivity.class);
                                String guardianId = currentUser.getUserId();
                                intentManageYouthProfile.putExtra("USER_ID", guardianId);
                                startActivity(intentManageYouthProfile);
                                overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                            }
                        });

                        //MANAGE PAYMENT BUTTON
                        btnManagePaymentInfo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(GuardianProfileActivity.this, "Temp msg: Manage Payment activity under construction", Toast.LENGTH_SHORT).show();
                                Intent intentManageYouthPayment = new Intent(GuardianProfileActivity.this, ManagePaymentActivity.class);
                                String guardianId = currentUser.getUserId();
                                intentManageYouthPayment.putExtra("USER_ID", guardianId);
                                startActivity(intentManageYouthPayment);
                                overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                            }
                        });

                        //ADD ADDRESS BUTTON
                        btnAddAddress.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intentNewAddress = new Intent(GuardianProfileActivity.this, CreateAddressActivity.class);
                                String guardianId = currentUser.getUserId();
                                intentNewAddress.putExtra("USER_ID", guardianId);
                                startActivity(intentNewAddress);
                                overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                            }
                        });

                        //EDIT PASSWORD BUTTON
                        btnEditPassword.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intentEditPassword = new Intent(GuardianProfileActivity.this, EditPasswordActivity.class);
                                String guardianId = currentUser.getUserId();
                                intentEditPassword.putExtra("USER_ID", guardianId);
                                startActivity(intentEditPassword);
                                overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
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
                ArrayAdapter<Address> addressAdapter = new ArrayAdapter<>(GuardianProfileActivity.this, android.R.layout.simple_spinner_item, addresses);
                addressAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                addressSpinner.setAdapter(addressAdapter);

                // Enable or disable the "Create Work Order" button based on the presence of addresses
                //btnOrderShoveling.setEnabled(!addresses.isEmpty());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        getImage.launch(intent);
    }

    ActivityResultLauncher<Intent> getImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
        if(result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            assert data != null;
            guardianIdImageUri = data.getData();
            setImageButtonBackground(guardianIdImageUri);

            // Make send ID button visible
            btnSendGuardianId.setVisibility(View.VISIBLE);
        }
    });

    private void setImageButtonBackground(Uri imageUri) {
        try {
            Bitmap newImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

            // Recycle the old Bitmap (clean memory)
            if (currentBitmap != null && !currentBitmap.isRecycled()) {
                currentBitmap.recycle();
            }

            // Set the new Bitmap and keep a reference
            btnAddIDPicture.setImageBitmap(newImage);
            currentBitmap = newImage;

            btnAddIDPicture.setVisibility(View.VISIBLE);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load ID", Toast.LENGTH_SHORT).show();
        }
    }
    private void uploadIdImage() {
        if (currentUser == null) {
            Toast.makeText(this, "User data is not available", Toast.LENGTH_SHORT).show();
            return;
        }

        //Validation
        // Check if there is an existing ID URL, but the ID is not validated (ID under review)
        boolean idUnderReview = currentUser.getGuardianIdUrl() != null && !currentUser.getGuardianIdValidated();

        // Check if there is an existing ID URL and the ID is validated (User is authorized, ID not removed)
        boolean isAlreadyValidatedWithID = currentUser.getGuardianIdUrl() != null && currentUser.getGuardianIdValidated();

        // Check if there is NOT an existing URL (e.g., if we remove the ID after a specified time because of a data retention policy),
        // but the User is validated (User is authorized)

        boolean isAlreadyValidatedWithoutID = currentUser.getGuardianIdUrl() == null && currentUser.getGuardianIdValidated();


        if(idUnderReview) {
            Toast.makeText(this, "Your ID is currently being reviewed. You cannot upload another ID", Toast.LENGTH_SHORT).show();
        }

        // If the ID
        else if (isAlreadyValidatedWithID) {
            Toast.makeText(this, "You are already validated and cannot upload an ID.", Toast.LENGTH_SHORT).show();
        }

        else if (isAlreadyValidatedWithoutID) {
            Toast.makeText(this, "You are already validated and cannot upload an ID.", Toast.LENGTH_SHORT).show();
        }
        else {
            if(guardianIdImageUri == null) {
                Toast.makeText(this, "Please select an ID image to upload.", Toast.LENGTH_SHORT).show();
            }
            uploadIdImageToFirebaseStorage(guardianIdImageUri);
        }
    }

    private void uploadIdImageToFirebaseStorage(Uri imageUri) {

        if (imageUri == null) {
            Toast.makeText(this, "No image selected. Please select an image to upload.", Toast.LENGTH_SHORT).show();
            return;
        }
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference idReference = storageReference.child("guardianIds/" + currentUser.getUserId() + ".jpg");

        idReference.putFile(imageUri).addOnSuccessListener(uri -> {
            uri.getStorage().getDownloadUrl().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    String imageUrl = task.getResult().toString();
                    currentUser.setGuardianIdUrl(imageUrl);
                    currentUser.setGuardianIdValidated(false);

                    updateUserWithIdUrl(imageUrl);
                } else {
                    Toast.makeText(GuardianProfileActivity.this, "Failed to get ID download URL", Toast.LENGTH_SHORT).show();
                }
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(GuardianProfileActivity.this, "Failed to upload ID", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateUserWithIdUrl(String imageUrl) {
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUserId());
        Map<String, Object> updates = new HashMap<>();
        updates.put("guardianIdUrl", imageUrl);

        userReference.updateChildren(updates).addOnSuccessListener(aVoid -> {
            // Notify the user of successful upload
            Toast.makeText(GuardianProfileActivity.this, "ID uploaded and pending approval", Toast.LENGTH_SHORT).show();
            // Call method to send ID for validation
            sendIdForValidation(imageUrl, currentUser.getUserId());
        }).addOnFailureListener(e -> {
            // Reset local currentUser object if Firebase update fails
            currentUser.setGuardianIdUrl(null);
            currentUser.setGuardianIdValidated(false);
            Toast.makeText(GuardianProfileActivity.this, "Failed to update profile with ID URL", Toast.LENGTH_SHORT).show();
        });
    }

    private void sendIdForValidation(String guardianIDUrl, String userId) {

        // Get retrofit instance from singleton Class
        Retrofit retrofit = RetrofitClient.getClient();
        CloudFunctionsService service = retrofit.create(CloudFunctionsService.class);

        // Create GuardianIdInformation instance
        GuardianIdInformation data = new GuardianIdInformation(guardianIDUrl, userId);

        // Make network request
        Call<Void> call = service.sendIdForValidation(data);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.isSuccessful()) {
                    Toast.makeText(GuardianProfileActivity.this, "ID validation request sent", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(GuardianProfileActivity.this, "Failed to send ID validation request", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(GuardianProfileActivity.this, "Error" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void linkYouthProfile(User guardian){

        // Ensure Guardian had valid ID before linking youth

        if (!guardian.getGuardianIdValidated()) {
            Toast.makeText(GuardianProfileActivity.this, "ID Validation is required before linking a youth", Toast.LENGTH_SHORT).show();
            return;
        }

        String youthUsername = addYouthET.getText().toString().trim();


        // Query youth in Firebase
        userTable.orderByChild("username").equalTo(youthUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot youthSnapshot : snapshot.getChildren()) {
                        User youthUser = youthSnapshot.getValue(User.class);
                        if (youthUser != null && "Youth Shoveller".equals(youthUser.getAccountType())){
                            // Link youth to guardian account
                            addYouthToGuardianAccount(youthUser, guardian);
                            // Link guardian to Youth account
                            addGuardianToYouthAccount(youthUser, guardian);
                        } else {
                            Toast.makeText(GuardianProfileActivity.this, "Youth username not found",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(GuardianProfileActivity.this, "Username does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GuardianProfileActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void addGuardianToYouthAccount(User youthUser, User guardianUser) {
        System.out.println("Adding guardian: :" + guardianUser.getUsername() + " to" + youthUser.getUsername());

        DatabaseReference linkedYouthUserReference = userTable.child(youthUser.getUserId()).child("linkedUsers");
        Map<String, Object> guardianUserInfo = new HashMap<>();

        guardianUserInfo.put(guardianUser.getUserId(), new HashMap<String, Object>() {{
            put("userId", guardianUser.getUserId());
            put("username", guardianUser.getUsername());
            put("firstName", guardianUser.getFirstName());
            put("lastName", guardianUser.getLastName());
            put("email", guardianUser.getEmail());
            put("phoneNo", guardianUser.getPhoneNo());
            put("guardianIdValidated", guardianUser.getGuardianIdValidated());

        }});

        linkedYouthUserReference.updateChildren(guardianUserInfo).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Toast.makeText(GuardianProfileActivity.this, "Guardian successfully linked", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(GuardianProfileActivity.this, "Failed to link guardian to youth account: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addYouthToGuardianAccount(User youthUser, User guardianUser) {
        System.out.println("Adding guardian: :" + guardianUser.getUsername() + " to" + youthUser.getUsername());

        DatabaseReference linkedGuardianUserReference = userTable.child(guardianUser.getUserId()).child("linkedUsers");
        Map<String, Object> youthUserInfo = new HashMap<>();
        youthUserInfo.put(youthUser.getUserId(), new HashMap<String, Object>(){{
            put("userId", youthUser.getUserId());
            put("username", youthUser.getUsername());
            put("firstName", youthUser.getFirstName());
            put("lastName", youthUser.getEmail());
            put("email", youthUser.getEmail());
            put("phoneNo", youthUser.getPhoneNo());

        }});

        linkedGuardianUserReference.updateChildren(youthUserInfo).addOnCompleteListener(task -> {
           if(task.isSuccessful()) {
               Toast.makeText(GuardianProfileActivity.this, "Youth successfully linked", Toast.LENGTH_SHORT).show();
           } else {
               Toast.makeText(GuardianProfileActivity.this, "Failed to link youth to guardian account: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
           }
        });
    }

    //**TODO**
    private void readYouthProfilesFromFirebase(User guardian) {
        System.out.println("userid received by read database: " + guardian);

        userTable.child(guardian.getUserId()).child("linkedUsers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Clear linked users in User Class
                guardian.setLinkedUsers(new HashMap<String, User>());

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                     //Deserialize user object from snapshot
                    User linkedUser = snapshot.getValue(User.class);
                    if (linkedUser != null) {
                        String userId = snapshot.getKey();
                        guardian.addLinkedUser(userId, linkedUser);
                    }
                }

                // Get list of linked users
                List<User> linkedUsers = new ArrayList<>(guardian.getLinkedUsers().values());

                // Update spinner with users
                ArrayAdapter<User> linkedYouthAdapter = new ArrayAdapter<>(GuardianProfileActivity.this, android.R.layout.simple_spinner_item, linkedUsers);
                linkedYouthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                linkedYouthSpinner.setAdapter(linkedYouthAdapter);

                // Enable or disable spinner based on linked users' presence
                linkedYouthSpinner.setEnabled(!linkedUsers.isEmpty());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Log.e(TAG, "Error reading linked users: " + databaseError.getMessage());
            }
        });
    }
    private void retrieveLinkedYouths (User guardianUser){
        List<String> youthUsers = new ArrayList<>();
        for(User youth : guardianUser.getLinkedUsers().values()) {
            youthUsers.add(youth.getUsername());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, youthUsers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        linkedYouthSpinner.setAdapter(adapter);
    }
}
