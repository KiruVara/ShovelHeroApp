package com.example.shovelheroapp.Models;

import android.media.Image;
import android.widget.CalendarView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WorkOrder {

    private String workOrderId;
    private String status;
    private int squareFootage;
    private double price;
    private String requestDate;
    private CalendarView requestDatePicker;
    //private TextClock requestTimePicker;
    private String requestedTime;
    private String requestedDate;
    private boolean isDrivewayChecked;
    private boolean isWalkwayChecked;
    private boolean isSidewalkChecked;
    private List<String> itemsRequested;
    private String specialInstructions;
    private Image arrivalImage;
    private Image completedImage;
    private Image issueImage;

    private String customerId; //to be foreign key
    private String customerAddressId; //to be foreign key
    private String shovellerId;
    private String guardianId;
    private HashMap<String, Transaction> transaction;


    //private Invoice invoiceId; --> is this how we do it? with foreign key?
    //private Transaction paymentId; --> is this how we do it? Transaction? with foreign key?
    //private int shovelerId;


    public WorkOrder(){
        itemsRequested = new ArrayList<>();
        transaction = new HashMap<>();
    }


    //CONSTRUCTOR
    public WorkOrder(String workOrderId, String requestDate, String status, int squareFootage, String customerId, String customerAddressId) {
        this.workOrderId = workOrderId;
        this.requestDate = requestDate;
        this.status = status;
        this.squareFootage = squareFootage;
        this.itemsRequested = new ArrayList<>();
        this.customerId = customerId;
        this.customerAddressId = customerAddressId;
        this.transaction = new HashMap<>();
    }


    //GETTERS AND SETTERS

    public String getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(String workOrderId) {
        this.workOrderId = workOrderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getSquareFootage() {
        return squareFootage;
    }

    public void setSquareFootage(int squareFootage) {
        this.squareFootage = squareFootage;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }

    public CalendarView getRequestDatePicker() {
        return requestDatePicker;
    }

    public void setRequestDatePicker(CalendarView requestDatePicker) {
        this.requestDatePicker = requestDatePicker;
    }

    public String getRequestedTime() {
        return requestedTime;
    }

    public void setRequestedTime(String requestedDate) {
        this.requestedTime = requestedTime;
    }

    public String getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(String requestedDate) {
        this.requestedDate = requestedDate;
    }

    public boolean isDrivewayChecked() {
        return isDrivewayChecked;
    }

    public void setDrivewayChecked(boolean drivewayChecked) {
        isDrivewayChecked = drivewayChecked;
    }

    public boolean isWalkwayChecked() {
        return isWalkwayChecked;
    }

    public void setWalkwayChecked(boolean walkwayChecked) {
        isWalkwayChecked = walkwayChecked;
    }

    public boolean isSidewalkChecked() {
        return isSidewalkChecked;
    }

    public void setSidewalkChecked(boolean sidewalkChecked) {
        isSidewalkChecked = sidewalkChecked;
    }

    public void setItemsRequested(List<String> itemsRequested) {
        this.itemsRequested = itemsRequested;
    }

    public HashMap<String, Transaction> getTransaction() {
        return transaction;
    }

    public void setTransaction(HashMap<String, Transaction> transaction) {
        this.transaction = transaction;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public Image getArrivalImage() {
        return arrivalImage;
    }

    public void setArrivalImage(Image arrivalImage) {
        this.arrivalImage = arrivalImage;
    }

    public Image getCompletedImage() {
        return completedImage;
    }

    public void setCompletedImage(Image completedImage) {
        this.completedImage = completedImage;
    }

    public Image getIssueImage() {
        return issueImage;
    }

    public void setIssueImage(Image issueImage) {
        this.issueImage = issueImage;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerAddressId() {
        return customerAddressId;
    }

    public void setCustomerAddressId(String customerAddressId) {
        this.customerAddressId = customerAddressId;
    }

    public String getShovellerId() {
        return shovellerId;
    }

    public void setShovellerId(String shovellerId) {
        this.shovellerId = shovellerId;
    }

    public String getGuardianId() {
        return guardianId;
    }

    public void setGuardianId(String guardianId) {
        this.guardianId = guardianId; }
        
    public List<String> getItemsRequested() {
        return itemsRequested;
    }
}
