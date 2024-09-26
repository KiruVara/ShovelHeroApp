package com.example.shovelheroapp.Controllers;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.shovelheroapp.Models.Enums.Status;
import com.example.shovelheroapp.Models.User;
import com.example.shovelheroapp.Models.WorkOrder;
import com.example.shovelheroapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public class CreateWorkOrderActivity extends AppCompatActivity {

    private static final String TAG = "CreateWorkOrderActivity";


    //initialized database tables
    private DatabaseReference workOrderReference;
    private DatabaseReference userTable;


    private TextView addressTextView;
    private TextView sqFootageTextView;
    private EditText customShovelerEditText;
    private TextView workOrderPriceTextView;


    //Date fields
    String dateTime;
    Calendar calendar;
    SimpleDateFormat simpleDateFormat;
    SimpleDateFormat simpleTimeFormat;
    private TextView requestDate;   //date request was placed
    private EditText requestedDate; //date customer has requested in the future
    //private TextClock requestedTime;  //time customer has requested in the future
    private EditText requestedTime;


    //WO Items and pricing
    private Set<String> itemsRequested;
    private List<CheckBox> itemCheckBoxList;
    private CheckBox drivewayCheckBox;
    private CheckBox walkwayCheckBox;
    private CheckBox sidewalkCheckBox;
    private Double wOPrice = 2.00;  //app overhead cost - maybe more with stripe fee

    private EditText addressNotesEditText;
    TextView dateTimeInLongTextView;


    //Buttons
    private Button btnOrderShovelling;
    private Button btnCancelOrder;


    //instantiated Objects
    private WorkOrder currentWorkOrder;
    private String currentWOId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_order_create);

        addressTextView = findViewById(R.id.tvAddress);
        sqFootageTextView = findViewById(R.id.tvSquareFootage);
        requestDate = findViewById(R.id.tvRequestDate);
        customShovelerEditText = findViewById(R.id.etCustomShoveller);
        addressNotesEditText = findViewById(R.id.etAddressNotes);
        workOrderPriceTextView = findViewById(R.id.tvWorkOrderPrice);


        // Set requestDate to Hidden by default
        //**TODO: revert
        requestDate.setText("");
        //requestDate.findViewById(R.id.tvRequestDateTime);
        requestedDate = findViewById(R.id.etCustomDate);
        //requestedTime = findViewById(R.id.tpCustomTime);
        requestedTime = findViewById(R.id.etCustomTime);

        Button btnScheduleNow = findViewById(R.id.btnScheduleNow);
        btnScheduleNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestedDate.setText("");
                requestedTime.setText("");

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.getDefault());
                requestDate.setText(simpleDateFormat.format(new Date()));
            }
        });

        requestedDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestDate.setText("");

                displayRequestedDate();
            }
        });

        requestedTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestDate.setText("");
                displayRequestedTime();
            }
        });

        //TO VALIDATE THAT A JOB TYPE HAS BEEN SELECTED
        itemsRequested = new HashSet<>();
        drivewayCheckBox = findViewById(R.id.cbDriveway);
        walkwayCheckBox = findViewById(R.id.cbWalkway);
        sidewalkCheckBox = findViewById(R.id.cbSidewalk);

        //FOR AUTOMATIC PRICE UPDATES
        itemCheckBoxList = new ArrayList<>();
        itemCheckBoxList.add(findViewById(R.id.cbDriveway));
        itemCheckBoxList.add(findViewById(R.id.cbSidewalk));
        itemCheckBoxList.add(findViewById(R.id.cbWalkway));

        btnOrderShovelling = findViewById(R.id.btnOrderShovelling);
        btnCancelOrder = findViewById(R.id.btnCancel);

        Intent intent = getIntent();
        if (intent != null) {
            currentWOId = intent.getStringExtra("WORK_ORDER_ID");
            if (currentWOId != null) {
                System.out.println("work order ID recieved: " + currentWOId);  //WORKING
                retrieveWOInfo(currentWOId);
            }
        }
        System.out.println("Work order id intent ok from profile: " + currentWOId);

        //initialize Firebase
        userTable = FirebaseDatabase.getInstance().getReference("users");

        updateBill(currentWOId);

        //Add checkbox listeners - DONE
        for (final CheckBox checkbox : itemCheckBoxList) {
            checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (drivewayCheckBox.isChecked() && !itemsRequested.contains("Driveway")) {
                        itemsRequested.add("Driveway");
                    }
                    if (sidewalkCheckBox.isChecked() && !itemsRequested.contains("Sidewalk")) {
                        itemsRequested.add("Sidewalk");
                    }
                    if (walkwayCheckBox.isChecked() && !itemsRequested.contains("Walkway")) {
                        itemsRequested.add("Walkway");
                    }
                    updateBill(currentWOId);
                }
            });
        }
    }


    private void retrieveWOInfo(String currentWOID) {
        System.out.println("Work order received to retrieve wo info: " + currentWOID + " - line 179");

        workOrderReference = FirebaseDatabase.getInstance().getReference("workorders").child(currentWOId);
        workOrderReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentWorkOrder = snapshot.getValue(WorkOrder.class);

                    if (currentWorkOrder != null) {

                        //display inital WO information
                        readAddressFromFirebase(currentWorkOrder);

                        workOrderPriceTextView.setText("Shovelling Price: $2.00");
                        sqFootageTextView.setText("Job Size: " + currentWorkOrder.getSquareFootage() + " square feet");


                        //*******
                        //WORK ORDER CREATE BUTTONS
                        //*******

                        btnOrderShovelling.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                System.out.println("Order Shovelling button clicked");

                                if (drivewayCheckBox.isChecked() ||
                                        sidewalkCheckBox.isChecked() || walkwayCheckBox.isChecked()) {
                                    String date = requestedDate.getText().toString();
                                    String time = requestedTime.getText().toString();
                                    //String time = requestedTime.getFormat12Hour().toString();
                                    String customShovelerUsername = customShovelerEditText.getText().toString();
                                    System.out.println("Custom Shoveller Username before null decision:" + customShovelerUsername + ". - line 211");

                                    //**TODO: this is when the payment is confirmed and held (full payment at completion)
                                    /**
                                     if (date != null && !isValidDate(date)) {
                                     Toast.makeText(CreateWorkOrderActivity.this, "Please enter valid date in yyyy-MM-dd format", Toast.LENGTH_SHORT).show();
                                     } else if (time != null && !isValidTime(time)) {
                                     Toast.makeText(CreateWorkOrderActivity.this, "Please enter valid time in h:mm format", Toast.LENGTH_SHORT).show();
                                     } else {
                                     releaseWorkOrder(currentWorkOrder);}
                                     **/

                                    if (customShovelerUsername.isEmpty()) {
                                        System.out.println("Custom Shoveller Username = NULL, WO being released if job type selected - line 226: " + customShovelerUsername);
                                        releaseWorkOrder(currentWorkOrder);
                                    } else {
                                        System.out.println("Custom Shoveller Username deemed NOT null, username being sent for validation - line 230: " + customShovelerUsername);
                                        validateUserName(customShovelerUsername, currentWorkOrder);
                                    }
                                } else {
                                    Toast.makeText(CreateWorkOrderActivity.this, "Please add shovelling area", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


                        btnCancelOrder.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelWorkOrder(currentWorkOrder);
                            }
                        });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //handle error
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

                    addressTextView.setText(address + ", " + city + ", " + province);
                    System.out.println("Address retrieved from Firebase: " + address);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //handle error
            }
        });
    }


    //AUTOMATIC PRICING FROM CHECKBOXES - UPDATE BILL - DONE
    private void updateBill(String currentWorkOrderID) {
        System.out.println("updateBill Called - line 291");
        double totalPrice = wOPrice;

        //iterate through checkboxes
        for (CheckBox checkBox : itemCheckBoxList) {
            if (checkBox.isChecked()) {
                totalPrice += getPriceForCheckBoxes(checkBox);

                String formattedPrice = String.format(Locale.getDefault(), "$%.2f", totalPrice);

                workOrderPriceTextView.setText("Shovelling Price: " + formattedPrice);

                workOrderReference = FirebaseDatabase.getInstance().getReference("workorders").child(currentWorkOrderID);
                Map<String, Object> updateWorkOrder = new HashMap<>();
                updateWorkOrder.put("price", totalPrice);
                System.out.println("put this price in firebase: " + totalPrice);

                workOrderReference.updateChildren(updateWorkOrder)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            System.out.println("Price updated in Firebase");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CreateWorkOrderActivity.this, "Could not Update Price", Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        }
    }


    //AUTOMATIC PRICING FROM CHECKBOXES - ADD PRICING - DONE
    private Double getPriceForCheckBoxes(CheckBox checkBox) {
        if (checkBox.getId() == R.id.cbDriveway) {
            return 20.00;
        } else if (checkBox.getId() == R.id.cbSidewalk) {
            return 10.00;
        } else if (checkBox.getId() == R.id.cbWalkway) {
            return 10.00;
        }
        return 0.00;
    }


    public void releaseWorkOrder(WorkOrder currentWorkOrder) {
        System.out.println("Release WO now being called with WO#: " + currentWorkOrder);
        if (drivewayCheckBox.isChecked() || sidewalkCheckBox.isChecked() || walkwayCheckBox.isChecked()) {
            addWorkOrderDetails(currentWorkOrder);
        } else {
            Toast.makeText(CreateWorkOrderActivity.this, "Please add shovelling area", Toast.LENGTH_SHORT).show();
        }
    }

    public void addWorkOrderDetails(WorkOrder currentWorkOrder) {
        System.out.println("Add workOrderDetails method started - line 328 - WO rec'd: " + currentWorkOrder);


        String customerRequestedDate = requestedDate.getText().toString();
        String customerRequestedTime = requestedTime.getText().toString();
        String specialInstructions = addressNotesEditText.getText().toString();
        System.out.println("Date and Time retrieved from UI if they exist in addWorkOrderDetails - line 332: " + customerRequestedDate + " " + customerRequestedTime);

        //String customerRequestedTime = requestedTime.getFormat12Hour().toString();
        String status = Status.Open.toString();

        System.out.println("Test if itemsRequestedList being populated - addWODetails - line 351: ");
        for (String item : itemsRequested) {
            System.out.println(item);
        }

        SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.getDefault());
        String formattedDateTime = "";

        //
        if (!customerRequestedDate.isEmpty() && !customerRequestedTime.isEmpty()) {
            // Combine and format custom date and time
            try {
                Date customDateTime = new SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.getDefault()).parse(customerRequestedDate + " " + customerRequestedTime);
                formattedDateTime = outputDateFormat.format(customDateTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            // Use the Schedule now
            formattedDateTime = outputDateFormat.format(new Date());
        }

        Map<String, Object> updateWorkOrder = new HashMap<>();
        // Check and Add Status for work items
        updateWorkOrder.put("drivewayChecked", drivewayCheckBox.isChecked());
        updateWorkOrder.put("sidewalkChecked", sidewalkCheckBox.isChecked());
        updateWorkOrder.put("walkwayChecked", walkwayCheckBox.isChecked());

        // Other work order details
        updateWorkOrder.put("specialInstructions", specialInstructions);

        updateWorkOrder.put("status", status);
        updateWorkOrder.put("requestedDateTime", formattedDateTime);

        // Set the requestDate to the current date/time
        updateWorkOrder.put("requestDate", outputDateFormat.format(new Date()));

        workOrderReference = FirebaseDatabase.getInstance().getReference("workorders").child(currentWorkOrder.getWorkOrderId());
        workOrderReference.updateChildren(updateWorkOrder)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(CreateWorkOrderActivity.this, "Shovelling Request is now active", Toast.LENGTH_SHORT).show();

                        Intent intentCreateWO = new Intent(CreateWorkOrderActivity.this, CustomerProfileActivity.class);
                        String customerId = currentWorkOrder.getCustomerId();
                        intentCreateWO.putExtra("USER_ID", customerId);
                        startActivity(intentCreateWO);
                        overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CreateWorkOrderActivity.this, "Could not create Shovelling Job", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //CANCEL WORK ORDER UPON CUSTOMER BUTTON CLICK - DONE
    public void cancelWorkOrder(WorkOrder workOrder) {
        System.out.println("Deleting Work order: " + workOrder + ". Confirm in Firebase");

        workOrderReference = FirebaseDatabase.getInstance().getReference("workorders").child(workOrder.getWorkOrderId());
        workOrderReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    WorkOrder currentWO = snapshot.getValue(WorkOrder.class);

                    workOrderReference.removeValue();

                    Toast.makeText(CreateWorkOrderActivity.this, "Shovelling Request Cancelled", Toast.LENGTH_SHORT).show();

                    Intent intentCancelWO = new Intent(CreateWorkOrderActivity.this, CustomerProfileActivity.class);
                    String customerId = currentWO.getCustomerId();
                    intentCancelWO.putExtra("USER_ID", customerId);
                    startActivity(intentCancelWO);
                    overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CreateWorkOrderActivity.this, "Unable to cancel shovelling request", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayRequestedDate() {
        // Retrieve current date
        final Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        // Date Picker dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                requestedDate.setText(selectedDate);
            }
        }, currentYear, currentMonth, currentDay);

        // Ensure user cannot select a past date
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        datePickerDialog.show();
    }

    private void displayRequestedTime() {
        // Initialize to the nearest half hour.
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute;
        if (calendar.get(Calendar.MINUTE) >= 30) {
            minute = 30;
        } else {
            minute = 0;
        }

        // Create a new time picker dialog and handle the time selection
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                // Enforce the half-hour increments
                selectedMinute = (selectedMinute >= 30) ? 30 : 0;

                // Set calendar to selected time and format it
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                calendar.set(Calendar.MINUTE, selectedMinute);
                SimpleDateFormat formatter = new SimpleDateFormat("h:mm a", Locale.getDefault());
                String formattedTime = formatter.format(calendar.getTime());
                requestedTime.setText(formattedTime);
            }
        }, hour, minute, false); // 'false' for 12-hour format

        timePickerDialog.show();
    }

    // Validation function for UserName
    private void validateUserName(String customShovelerUsername, WorkOrder currentWorkOrder) {
        System.out.println("Validating username: " + customShovelerUsername + " for wo: " + currentWorkOrder);
        userTable = FirebaseDatabase.getInstance().getReference("users");
        userTable.orderByChild("username").equalTo(customShovelerUsername)
        .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapShot : snapshot.getChildren()) {
                        User user = userSnapShot.getValue(User.class);

                        if (user.getAccountType().equals("Youth Shoveller") || user.getAccountType().equals("Adult Shoveller"))

                            Toast.makeText(CreateWorkOrderActivity.this, "Username " + customShovelerUsername + " will be notified of your request if they are online.", Toast.LENGTH_SHORT).show();
                        System.out.println("Username: " + customShovelerUsername + " has been requested by customer");

                        String shovellerId = user.getUserId();
                        addCustomShovelerToWorkOrder(user.getUserId(), currentWorkOrder);
                    }
                } else {
                    Toast.makeText(CreateWorkOrderActivity.this, "Username not found. Please try again or leave empty.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //handle error
            }
        });
    }

    public void addCustomShovelerToWorkOrder(String shovellerId, WorkOrder currentWorkOrder) {
        System.out.println("Adding Shoveller ID to WO: " + shovellerId);

        workOrderReference = FirebaseDatabase.getInstance().getReference("workorders").child(currentWorkOrder.getWorkOrderId());
        Map<String, Object> addShoveller = new HashMap<>();
        addShoveller.put("shovellerId", shovellerId);

        workOrderReference.updateChildren(addShoveller)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    releaseWorkOrder(currentWorkOrder);
                    System.out.println("Shoveller ID added to firebase: " + shovellerId);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CreateWorkOrderActivity.this, "Unable to request username. Please try again or leave empty.", Toast.LENGTH_SHORT).show();
                }
            });
    }
}