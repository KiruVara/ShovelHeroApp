package com.example.shovelheroapp.Controllers;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.shovelheroapp.Models.User;
import com.example.shovelheroapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CustomerAcceptedWOActivity extends AppCompatActivity {

    DatabaseReference userTable;
    private String userId;
    private TextView shovellerName;
    private TextView shovellerGuardian;
    Button btnViewProfile;
    private CheckBox chkGuardianVerify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_accepted_woactivity);

        userTable = FirebaseDatabase.getInstance().getReference("users");

        shovellerName = findViewById(R.id.txtName);
        shovellerGuardian = findViewById(R.id.txtGuardian);
        btnViewProfile = findViewById(R.id.btnViewShovellerProfile);
        chkGuardianVerify = findViewById(R.id.checkBoxGuardianVerification);

        //GET USERID FROM LOGIN OR REGISTRATION
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("USER_ID");
            if (userId != null) {
                retrieveShovellerProfile(userId);
            }
        }

        Button buttonCancelOrder = findViewById(R.id.btnCancelCustomer);

        buttonCancelOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancelOrderDialog();
            }
        });

    }
    private void showCancelOrderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cancel Order");
        builder.setMessage("Please provide a reason for cancellation:");

        // Add an input field to the dialog
        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the reason entered by the user
                String reason = input.getText().toString();
                dialog.dismiss(); //For now, we are just the dialog box because we haven't implemented our report compliant yet
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
    private void retrieveShovellerProfile(String userId) {
        userTable.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);

                    if (user != null) {
                        //display user profile info
                        shovellerName.setText(user.getFirstName() + " " + user.getLastName());
                        shovellerGuardian.setText(user.getGuardianIdUrl());

                        // Load profile Image
                        String profileImageUrl = user.getProfilePictureUrl();
                        ImageView profileImageView = findViewById(R.id.imgViewShoveller);
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(CustomerAcceptedWOActivity.this)
                                    .load(profileImageUrl).into(profileImageView);
                        }
                        btnViewProfile.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intentViewYouthJobs = new Intent(CustomerAcceptedWOActivity.this, YouthShovelerProfileActivity.class);
                                String youthId = user.getUserId();
                                intentViewYouthJobs.putExtra("USER_ID", youthId);
                                startActivity(intentViewYouthJobs);
                                overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                            }
                        });
                        if(user.getGuardianIdValidated()){
                            chkGuardianVerify.isChecked();
                        }
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
}