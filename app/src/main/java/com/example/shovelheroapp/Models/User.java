package com.example.shovelheroapp.Models;

import java.util.HashMap;

public class User {

    //MANDATORY FIELDS
    private String userId;
    private String accountType;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String birthdate; // For Firebase data compatibility
    private int age;
    private String email;
    private String phoneNo;



    //private int paymentId;  // -->Foreign key - to choose from List of properties

    private String profilePictureUrl;
    private String  guardianIdUrl; //on Guardian view only
    private boolean guardianIdValidated; // --> only available to app team // I don't think Firebase works with complex Android UI (boolean)?
    private int shovellerRadius; // --> how far is shoveller willing to walk


    //PROFILE LISTS
    //private List<Address> addresses;
    private HashMap<String, Address> addresses;
    //private Map<String, Map<String, Object>> addresses;
    private HashMap<String, User> linkedUsers; // --> on Guardian view only  List<String> ? Is this correct?



    public User(){
        addresses = new HashMap<>();
        linkedUsers = new HashMap<>();
    }

    //CONSTRUCTOR
    public User(String userId, String accountType, String username, String password, String firstName, String lastName, String birthdate, String email, String phoneNo) {
        this.userId = userId;
        this.accountType = accountType;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthdate = birthdate;
        this.email = email;
        this.phoneNo = phoneNo;
        this.addresses = new HashMap<>();
        this.linkedUsers = new HashMap<>();
    }


    //GETTERS AND SETTERS
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }


    public String getGuardianIdUrl() {
        return guardianIdUrl;
    }

    public void setGuardianIdUrl(String guardianIdUrl) {
        this.guardianIdUrl = guardianIdUrl;
    }

    public boolean getGuardianIdValidated() {
        return guardianIdValidated;
    }

    public void setGuardianIdValidated(boolean guardianIdValidated) {
        this.guardianIdValidated = guardianIdValidated;
    }

    public int getShovellerRadius() {
        return shovellerRadius;
    }

    public void setShovellerRadius(int shovellerRadius) {
        this.shovellerRadius = shovellerRadius;
    }

    public void addAddress(String id, Address address) {
        addresses.put(id, address);
    }


    public HashMap<String, Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(HashMap<String, Address> addresses) {
        this.addresses = addresses;
    }


    public void addLinkedUser(String id, User user) {
        linkedUsers.put(id, user);
    }

    public HashMap<String, User> getLinkedUsers() {
        return linkedUsers;
    }

    public void setLinkedUsers(HashMap<String, User> linkedUsers) {
        this.linkedUsers = linkedUsers;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
    @Override
    public String toString() { // Display username in spinner (Guardian Profile)???
        return username;
    }
}
