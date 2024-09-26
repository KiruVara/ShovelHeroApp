package com.example.shovelheroapp.Controllers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shovelheroapp.Models.Enums.Status;
import com.example.shovelheroapp.Models.WorkOrder;
import com.example.shovelheroapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkOrderAdapterForAllOpenOrders extends RecyclerView.Adapter<WorkOrderAdapterForAllOpenOrders.ViewHolder> {
    private List<WorkOrder> workOrders;
    private Context context;
    private String userId;

    public WorkOrderAdapterForAllOpenOrders(Context context, List<WorkOrder> workOrders, String userId) {
        this.context = context;
        this.workOrders = workOrders;
        this.userId = userId;
    }

    //ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        //public ImageView addressImage;
        public TextView requestDateTV;
        //public TextView distanceTV;
        public TextView sqFootageTV;
        public TextView statusTV;

        public Button btnView;
        public Button btnTakeIt;


        public ViewHolder(View view) {
            super(view);
            //addressImage = view.findViewById(R.id.imgPropertyImage);
            requestDateTV = view.findViewById(R.id.tvRequestDate);
            //distanceTV = view.findViewById(R.id.tvDistance);
            sqFootageTV = view.findViewById(R.id.tvSquareFootage);
            statusTV = view.findViewById(R.id.tvStatus);

            btnView = itemView.findViewById(R.id.btnOpen);
            btnTakeIt = itemView.findViewById(R.id.btnTakeIt);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.work_order_item_open_orders, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkOrder workOrder = workOrders.get(position);

        Log.d("WorkOrderAdapter", "onBindViewHolder: " + workOrder.getStatus());

        //TODO: holder.addressImage.setImageBitmap(workOrder.getAddressImage);
        holder.requestDateTV.setText("Requested: " + String.valueOf(workOrder.getRequestDate()));
        //TODO: calculate distance from shoveller address and add here
        //holder.distanceTV.setText(workOrder.getDistance);

        holder.sqFootageTV.setText("Job size: " + String.valueOf(workOrder.getSquareFootage()) + "square feet");
        holder.statusTV.setText("Job Status: " + workOrder.getStatus());


        holder.btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String wOID = workOrder.getWorkOrderId();
                Context context = holder.itemView.getContext();
                Intent wOIntent = new Intent(context, ViewAnOpenWorkOrderActivity.class);
                wOIntent.putExtra("WO_ID", wOID);
                context.startActivity(wOIntent);
                ((Activity) context).overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
            }
        });

        holder.btnTakeIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Shoveller clicked to send the WO to their guardian");

                findGuardians(workOrder.getWorkOrderId());

                String userID = userId;
                Context context = holder.itemView.getContext();
                Intent wOIntent = new Intent(context, YouthShovelerProfileActivity.class);
                wOIntent.putExtra("USER_ID", userID);
                context.startActivity(wOIntent);
                ((Activity) context).overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
            }
        });


    }

    @Override
    public int getItemCount() {
        return workOrders.size();
    }

    public void updateData(List<WorkOrder> newWorkOrders) {
        workOrders.clear();
        workOrders.addAll(newWorkOrders);
        notifyDataSetChanged();
    }

    public void findGuardians(String workOrderId){
        System.out.println("Getting guardian ID");

        DatabaseReference linkedGuardian = FirebaseDatabase.getInstance().getReference("users").child(userId).child("linkedUsers");
        linkedGuardian.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot linkedGuardianSnapshot : snapshot.getChildren()) {
                        String linkedGuardianId = linkedGuardianSnapshot.getKey();

                        requestWorkOrder(linkedGuardianId, workOrderId);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void requestWorkOrder(String guardianID, String workOrderId){

        DatabaseReference wORef = FirebaseDatabase.getInstance().getReference("workorders").child(workOrderId);
        Map<String, Object> updateWorkOrder = new HashMap<>();
        updateWorkOrder.put("guardianId", guardianID);
        updateWorkOrder.put("shovellerId", userId);
        updateWorkOrder.put("status", Status.PendingGuardianApproval.toString());

        wORef.updateChildren(updateWorkOrder);
        //TODO: notify guardian
    }
}

