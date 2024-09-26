package com.example.shovelheroapp.Models.Enums;

public enum Status {

    Open, PendingGuardianApproval, Accepted, Enroute, InProgress, Issue, Completed, CancelledByCustomer, CancelledByShoveller, Closed, OpenCustom;

    //*********************************************
    //*****WO STATUS CHEAT SHEET FOR DEVELOPERS****
    //*********************************************

    //Open = viewable by all shovellers
    //Pending = Requested by youth, not approved yet (take out of open WO list for __ mins)
    //Accepted = Take out of open WO list (can still be cancelled) - ping notification
    //Enroute = out of WO list, can no longer be cancelled by customer - ping notification
    //InProgress = out of WO list, can no longer be cancelled by customer - ping notification
    //Issue = Onsite, but there is an obstacle - notify customer + app support
    //CancelledByCustomer = cancel WO + notify shoveller + add status to order history
    //CancelledByShoveller = Return to Open WO's + notify customer + add status to shoveller order history
    //Closed = WO completed + add to order history + send invoice and images
    //OpenCustom = Specific shoveller request 0 open WO viewable only by requested shoveller for __ hours, then put into open WO list
}
