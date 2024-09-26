package com.example.shovelheroapp.Models;

public class Transaction {

    String TransactionId;
    String TransactionType;
    String TransactionDate;
    String TransactionAmount;
    String TransactionStatus;
    String CustomerId;
    String CustomerPaymentId;
    String ShovellerId;
    String ShovellerPaymentId;


    public Transaction(){}

    public Transaction(String transactionId, String transactionType, String transactionDate, String transactionAmount, String transactionStatus, String customerId, String customerPaymentId) {
        TransactionId = transactionId;
        TransactionType = transactionType;
        TransactionDate = transactionDate;
        TransactionAmount = transactionAmount;
        TransactionStatus = transactionStatus;
        CustomerId = customerId;
        CustomerPaymentId = customerPaymentId;
    }

    public String getTransactionId() {
        return TransactionId;
    }

    public void setTransactionId(String transactionId) {
        TransactionId = transactionId;
    }

    public String getTransactionType() {
        return TransactionType;
    }

    public void setTransactionType(String transactionType) {
        TransactionType = transactionType;
    }

    public String getTransactionDate() {
        return TransactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        TransactionDate = transactionDate;
    }

    public String getTransactionAmount() {
        return TransactionAmount;
    }

    public void setTransactionAmount(String transactionAmount) {
        TransactionAmount = transactionAmount;
    }

    public String getTransactionStatus() {
        return TransactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        TransactionStatus = transactionStatus;
    }

    public String getCustomerId() {
        return CustomerId;
    }

    public void setCustomerId(String customerId) {
        CustomerId = customerId;
    }

    public String getCustomerPaymentId() {
        return CustomerPaymentId;
    }

    public void setCustomerPaymentId(String customerPaymentId) {
        CustomerPaymentId = customerPaymentId;
    }

    public String getShovellerId() {
        return ShovellerId;
    }

    public void setShovellerId(String shovellerId) {
        ShovellerId = shovellerId;
    }

    public String getShovellerPaymentId() {
        return ShovellerPaymentId;
    }

    public void setShovellerPaymentId(String shovellerPaymentId) {
        ShovellerPaymentId = shovellerPaymentId;
    }
}
