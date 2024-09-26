package com.example.shovelheroapp.Controllers;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.shovelheroapp.Models.User;
import com.example.shovelheroapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {

    private DatabaseReference userReference;
    private EditText usernameEditText, passwordEditText;

    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        overridePendingTransition(R.anim.zoom_in, 0);

        userReference = FirebaseDatabase.getInstance().getReference("users");
        System.out.println("Firebase connected");

        usernameEditText = findViewById(R.id.etUsername);
        passwordEditText = findViewById(R.id.etPassword);

        Button loginButton = findViewById(R.id.btnLogin);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser(view);
            }
        });
    }

        public void loginUser(View view) {
            final String username = usernameEditText.getText().toString().trim();
            final String password = passwordEditText.getText().toString().trim();

            //Check if username exists
            userReference.orderByChild("username").equalTo(username)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                for (DataSnapshot userSnapShot : snapshot.getChildren()) {
                                    User user = userSnapShot.getValue(User.class);

                                    if (user != null && user.getPassword().equals(password) && !user.getPassword().isEmpty() && !user.getUsername().isEmpty()){
                                        System.out.println("Username and password ok");

                                        String accountType = user.getAccountType();

                                        System.out.println("Account Type confirmed: " + accountType);

                                        //valid username and password
                                        Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                                        System.out.println("login success");

                                        //****todo***TO ADD FUNDRAISER AND ADULT SHOVELLER
                                        switch (accountType) {
                                            case "Youth Shoveller":
                                                Intent intentLoginYouth = new Intent(MainActivity.this, YouthShovelerProfileActivity.class);
                                                String youthID = user.getUserId();
                                                intentLoginYouth.putExtra("USER_ID", youthID);
                                                startActivity(intentLoginYouth);
                                                overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                                                finish();
                                                break;
                                            case "Customer":
                                                Intent intentLoginCustomer = new Intent(MainActivity.this, CustomerProfileActivity.class);
                                                String customerId = user.getUserId();
                                                intentLoginCustomer.putExtra("USER_ID", customerId);
                                                startActivity(intentLoginCustomer);
                                                overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                                                finish();
                                                break;
                                            case "Guardian":
                                                Intent intentLoginGuardian = new Intent(MainActivity.this, GuardianProfileActivity.class);
                                                String guardianId = user.getUserId();
                                                intentLoginGuardian.putExtra("USER_ID", guardianId);
                                                startActivity(intentLoginGuardian);
                                                overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                                                finish();
                                                break;
                                            default:
                                                Intent intent = new Intent(MainActivity.this, UserRegistrationActivity.class);
                                                startActivity(intent);
                                                overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                                                break;
                                        }
                                    }
                                    else {
                                        Toast.makeText(MainActivity.this, "invalid password or account type", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "User not found. Please try again or create a new account.", Toast.LENGTH_SHORT).show();
;                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            //handle error
                        }
                    });
        }

        public void createNewUser(View view){
            Intent intent = new Intent(MainActivity.this, UserRegistrationActivity.class);
            startActivity(intent);
        }
    }
