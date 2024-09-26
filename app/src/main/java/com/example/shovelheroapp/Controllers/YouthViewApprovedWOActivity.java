package com.example.shovelheroapp.Controllers;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shovelheroapp.R;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class YouthViewApprovedWOActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youth_view_approved_woactivity);

        Button buttonCancelOrder = findViewById(R.id.btnCancelYouth);

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
}