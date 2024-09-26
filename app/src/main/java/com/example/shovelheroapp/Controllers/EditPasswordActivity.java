package com.example.shovelheroapp.Controllers;

import android.content.Intent;
import android.os.Bundle;
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

public class EditPasswordActivity extends AppCompatActivity {

    private EditText newPasswordEditText;
    private EditText confirmPasswordEditText;

    String currentUserId;

    private DatabaseReference userTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);

        newPasswordEditText = findViewById(R.id.etNewPass);
        confirmPasswordEditText = findViewById(R.id.etConfirmPass);
        Button btnEditPassword = findViewById(R.id.btnEditPassword);

        currentUserId = getIntent().getStringExtra("USER_ID");
        if (currentUserId == null) {
            Toast.makeText(this, "Temp msg: CustomerID is null", Toast.LENGTH_SHORT).show();
        }

        //Instantiate userTable
        userTable = FirebaseDatabase.getInstance().getReference().child("users");


        btnEditPassword.setOnClickListener(v -> updatePassword());

    }

    public void updatePassword(){
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        // Check if the new password meets the criteria
        if(!isPasswordValid(newPassword)){
            Toast.makeText(EditPasswordActivity.this, "Password must contain at least 8 characters and one letter and one digit", Toast.LENGTH_SHORT).show();
            return;
        } if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(EditPasswordActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        userTable.child(currentUserId).child("password").setValue(newPassword);
        Toast.makeText(EditPasswordActivity.this, "Password successfully updated", Toast.LENGTH_SHORT).show();
        saveAndReturnToProfile(currentUserId);

        }

    private boolean isPasswordValid(String password){
        return password.length() >= 8 && password.matches(".*\\d.*") && password.matches(".*[a-zA-Z].*");
    }


    private void saveAndReturnToProfile(String currentUserId){
        DatabaseReference userTable = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
        userTable.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User currentUser = snapshot.getValue(User.class);
                assert currentUser != null;
                String accountType = currentUser.getAccountType();

                //**todo** ADD ADULT SHOEVLLER AND GUARDIAN
                if(accountType != null) {
                    switch (accountType) {
                        case "Youth Shoveller":
                            Intent intentYouth = new Intent(EditPasswordActivity.this, YouthShovelerProfileActivity.class);
                            String youthID = currentUser.getUserId();
                            intentYouth.putExtra("USER_ID", youthID);
                            startActivity(intentYouth);
                            overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                            break;
                        case "Customer":
                            Intent intentCustomer = new Intent(EditPasswordActivity.this, CustomerProfileActivity.class);
                            String customerId = currentUser.getUserId();
                            intentCustomer.putExtra("USER_ID", customerId);
                            startActivity(intentCustomer);
                            overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                            break;
                        case "Guardian":
                            Intent intentGuardian = new Intent(EditPasswordActivity.this, GuardianProfileActivity.class);
                            String guardianId = currentUser.getUserId();
                            intentGuardian.putExtra("USER_ID", guardianId);
                            startActivity(intentGuardian);
                            overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                            break;
                        default:
                            Intent intent = new Intent(EditPasswordActivity.this, UserRegistrationActivity.class);
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