package com.paulcloward.cardswap;

import com.google.firebase.database.Exclude;

/**
 * Created by Paul on 3/13/2018.
 */

public class Card {

    private String mUserKey;
    private String mKey;
    private String imgURL;
    private String name;
    private String title;
    private String company;
    private String phoneNumber;
    private String emailAddress;
    private String streetAddress;
    private String websiteAddress;
    private String faxAddress;

    public Card(){}


    public Card(String imgURL, String name, String title, String company) {
        this.imgURL = imgURL;
        this.name = name;
        this.title = title;
        this.company = company;
    }

    @Exclude
    public String getUserKey(){
        return mUserKey;
    }

    @Exclude
    public void setUserKey(String userKey){
        mUserKey = userKey;
    }

    @Exclude
    public String getKey(){
        return mKey;
    }

    @Exclude
    public void setKey(String key){
        mKey = key;
    }

    public String getImgURL() {
        return imgURL;
    }

    public void setImgURL(String imgURL) {
        this.imgURL = imgURL;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() { return company; }

    public void setCompany(String company) { this.company = company; }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getWebsiteAddress() {
        return websiteAddress;
    }

    public void setWebsiteAddress(String websiteAddress) {
        this.websiteAddress = websiteAddress;
    }

    public String getFaxAddress() {
        return faxAddress;
    }

    public void setFaxAddress(String faxAddress) {
        this.faxAddress = faxAddress;
    }
}