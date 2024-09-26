package com.example.shovelheroapp.Controllers;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shovelheroapp.Models.WorkOrder;
import com.example.shovelheroapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListAllOpenWorkOrdersActivity extends AppCompatActivity {

    private RecyclerView workOrderRecyclerView;
    private WorkOrderAdapterForAllOpenOrders adapter;
    private List<WorkOrder> workOrders;
    private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_order_list_all_open);

        //GET USERID FROM LOGIN OR REGISTRATION
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("USER_ID");
            if (userId != null) {
                //**TODO: assess user type and filter as needed
                //retrieveGuardianProfile(userId);
            }
        }

        workOrderRecyclerView = findViewById(R.id.rvWorkOrders);
        workOrderRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        workOrders = new ArrayList<>();
        adapter = new WorkOrderAdapterForAllOpenOrders(this, workOrders, userId);
        //adapter = new WorkOrderAdapterForAllOpenOrders(this, workOrders);
        workOrderRecyclerView.setAdapter(adapter);
        Log.d("ListAllOpenWorkOrders", "onCreate: Open");



        DatabaseReference workOrderReference = FirebaseDatabase.getInstance().getReference("workorders");

        //WORK ORDER RECYCLER CONDITIONS
        workOrderReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                workOrders.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    WorkOrder workOrder = snapshot.getValue(WorkOrder.class);

                    //conditions based on usertype here

                    if (workOrder.getStatus().equals("Open")) {
                        workOrders.add(workOrder);
                    }
                }
                Log.d("ListAllOpenWorkOrders", "Data size: " + workOrders.size());
                adapter.notifyDataSetChanged();
                workOrderRecyclerView.setAdapter(adapter);
                Log.d("ListAllOpenWorkOrders", "Adapter notified of data change");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ListAllOpenWorkOrders", "Error fetching data: " + error.getMessage());
                error.toException().printStackTrace(); // Print stack trace for detailed error info
            }
        });


        //Navigation Bar Activity
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationViewYouthShoveler);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_workorders) {
                startActivity(new Intent(ListAllOpenWorkOrdersActivity.this, ListAllOpenWorkOrdersActivity.class));
                return true;
            } else if (itemId == R.id.menu_orderhistory) {
                startActivity(new Intent(ListAllOpenWorkOrdersActivity.this, OrderHistoryActivity.class));
                return true;
            } else if (itemId == R.id.menu_logout) {
                startActivity(new Intent(ListAllOpenWorkOrdersActivity.this, MainActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    public void filterListByUser(){

    }
}