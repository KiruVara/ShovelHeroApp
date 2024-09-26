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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shovelheroapp.Models.Enums.Status;
import com.example.shovelheroapp.Models.WorkOrder;
import com.example.shovelheroapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkOrderAdapterForGuardian extends RecyclerView.Adapter<WorkOrderAdapterForGuardian.ViewHolder> {
    private List<WorkOrder> workOrders;
    private Context context;
    private String userId;

    public WorkOrderAdapterForGuardian(Context context, List<WorkOrder> workOrders, String userId) {
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
        public Button btnApprove;
        public Button btnReject;

        public ViewHolder(View view) {
            super(view);
            //addressImage = view.findViewById(R.id.imgPropertyImage);
            requestDateTV = view.findViewById(R.id.tvRequestDate);
            //distanceTV = view.findViewById(R.id.tvDistance);
            sqFootageTV = view.findViewById(R.id.tvSquareFootage);
            statusTV = view.findViewById(R.id.tvStatus);

            btnView = itemView.findViewById(R.id.btnView);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.work_order_item_guardian_profile, parent, false);
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

        holder.btnApprove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Shoveller clicked cancel. WO returned to open status.");

                approveOrder(workOrder.getWorkOrderId());

                //TODO: notify shoveller and customer of approval
                //TODO: update recycler list of WO's
            }
        });

        holder.btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Shoveller clicked cancel. WO returned to open status.");

                rejectOrder(workOrder.getWorkOrderId());

                //TODO: notify shoveller of reject
                //TODO: update recycler list of orders
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

    public void approveOrder(String wOID){
        DatabaseReference workOrderRef = FirebaseDatabase.getInstance().getReference("workorders").child(wOID);
        Map<String, Object> wOMap = new HashMap<>();
        wOMap.put("status", Status.Accepted.toString());

        //TODO: notify customer of approval
        //TODO: notify shoveller customer of approval

        workOrderRef.updateChildren(wOMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        System.out.println("Job Successfully rejected");
                        Toast.makeText(context, "Shovelling job approved. Please arrive onsite with 2 hours of requested time.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Could not approve job", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void rejectOrder(String wOID){
        DatabaseReference workOrderRef = FirebaseDatabase.getInstance().getReference("workorders").child(wOID);
        Map<String, Object> wOMap = new HashMap<>();
        wOMap.put("status", Status.Open.toString());
        wOMap.put("guardianId", "");
        wOMap.put("shovellerId", "");

        //TODO: notify customer of rejection
        //TODO: notify shoveller customer of rejection

        workOrderRef.updateChildren(wOMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        System.out.println("Job Successfully rejected");
                        Toast.makeText(context, "Shovelling job rejected", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Could not reject job", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

