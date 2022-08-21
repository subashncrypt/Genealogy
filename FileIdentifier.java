//package com.company;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

public class FileIdentifier {

    private int dataBaseId;
    private String fileLocation;
    private Date date;
    private String place;
    private String city;
    private String province;
    private String country;

    private ArrayList<MediaTag> tags = new ArrayList<>();

    private ArrayList<PersonIdentity> peopleInMedia = new ArrayList<>();

    public int getDataBaseId() {
        return dataBaseId;
    }

    public void setDataBaseId(int dataBaseId) {
        this.dataBaseId = dataBaseId;
    }

    public FileIdentifier(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public ArrayList<MediaTag> getTags() {
        return tags;
    }

    public void setTags(ArrayList<MediaTag> tags) {
        this.tags = tags;
    }

    public ArrayList<PersonIdentity> getPeopleInMedia() {
        return peopleInMedia;
    }

    public void setPeopleInMedia(PersonIdentity peopleInMedia) {
            this.peopleInMedia.add(peopleInMedia);
    }

    public void addDataBaseId(Statement statement) {

        int id = 0;

        ResultSet mediaId = null;
        try{
            mediaId = statement.executeQuery("select max(idmediaFile) as id  from mediafile;");
            while (mediaId.next()){
                id = Integer.parseInt(mediaId.getString("id"));
            }

        }
        catch (SQLException sqlException){

        }

        this.dataBaseId = id;
    }

}
