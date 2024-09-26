package com.example.shovelheroapp.Controllers;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.shovelheroapp.R;

public class JobCompletionConfirmationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_completion_confirmation);

        Button rateShovellerBtn = findViewById(R.id.btnRate);
        Button viewInvoiceBtn = findViewById(R.id.btnInvoice);

        rateShovellerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(JobCompletionConfirmationActivity.this, RateShovellerActivity.class);
                startActivity(intent);
            }
        });

        viewInvoiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(JobCompletionConfirmationActivity.this, YouthAndGuardianInvoiceActivity.class);
                startActivity(intent);
            }
        });
    }
}