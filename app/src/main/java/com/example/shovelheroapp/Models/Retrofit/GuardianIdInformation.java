package com.example.shovelheroapp.Models.Retrofit;

public class GuardianIdInformation {
    private String guardianIDUrl;
    private String userId;

    public GuardianIdInformation() {

    }
    public GuardianIdInformation(String guardianIDUrl, String userId) {
        this.guardianIDUrl = guardianIDUrl;
        this.userId = userId;
    }

    public String getGuardianIDUrl() {
        return guardianIDUrl;
    }

    public void setGuardianIDUrl(String guardianIDUrl) {
        this.guardianIDUrl = guardianIDUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
