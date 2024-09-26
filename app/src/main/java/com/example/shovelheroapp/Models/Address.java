package com.example.shovelheroapp.Models;

public class Address {
    private String addressId;
    //private Image customerAddressImage;
    private String address;
    private String city;
    private String province;
    private String postalCode;
    private String country;
    private String addressNotes;
    private int drivewaySquareFootage;
    private String accessible;
    private String shovelAvailable;


    public Address() {}

    //CONSTRUCTOR
    public Address(String addressId, String address, String city, String province, String postalCode, String country, int drivewaySquareFootage) {
        this.addressId = addressId;
        this.address = address;
        this.city = city;
        this.province = province;
        this.postalCode = postalCode;
        this.country = country;
        this.drivewaySquareFootage = drivewaySquareFootage;
    }

    //CONSTRUCTORS
    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAddressNotes() {
        return addressNotes;
    }

    public void setAddressNotes(String addressNotes) {
        this.addressNotes = addressNotes;
    }

    public int getDrivewaySquareFootage() {
        return drivewaySquareFootage;
    }

    public void setDrivewaySquareFootage(int drivewaySquareFootage) {
        this.drivewaySquareFootage = drivewaySquareFootage;
    }

    public String getAccessible() {
        return accessible;
    }

    public void setAccessible(String accessible) {
        this.accessible = accessible;
    }

    public String getShovelAvailable() {
        return shovelAvailable;
    }

    public void setShovelAvailable(String shovelAvailable) {
        this.shovelAvailable = shovelAvailable;
    }

    @Override
    public String toString(){
        return address + ", " + city + ", " + province;
    }
}
