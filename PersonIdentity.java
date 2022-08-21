//package com.company;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class PersonIdentity {

    private int dataBaseId;
    private String name;
    private String gender;
    private String occupation;
    private Date birthDate;
    private String birthLocation;
    private Date deathDate;
    private String deathLocation;

    ArrayList<String> referencesNotes = new ArrayList<>();
    ArrayList<FileIdentifier> mediaFiles= new ArrayList();

    Set<PersonIdentity> children = new HashSet<PersonIdentity>();
    Set<PersonIdentity> parents = new HashSet<>();

    PersonIdentity relation = null;

    public PersonIdentity(String name) {
        this.name = name;
    }

    public PersonIdentity() {
        this("");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getBirthLocation() {
        return birthLocation;
    }

    public void setBirthLocation(String birthLocation) {
        this.birthLocation = birthLocation;
    }

    public Date getDeathDate() {
        return deathDate;
    }

    public void setDeathDate(Date deathDate) {
        this.deathDate = deathDate;
    }

    public String getDeathLocation() {
        return deathLocation;
    }

    public void setDeathLocation(String deathLocation) {
        this.deathLocation = deathLocation;
    }

    public ArrayList<String> getReferencesNotes() {
        return referencesNotes;
    }

    public void setReferencesNotes(String referencesNotes) {

        this.referencesNotes.add(referencesNotes);
    }

    public ArrayList<FileIdentifier> getMediaFiles() {
        return mediaFiles;
    }

    public void setMediaFiles(FileIdentifier mediaFiles) {
        this.mediaFiles.add(mediaFiles);
    }

    public Set<PersonIdentity> getChildren() {
        return children;
    }

    public void setChildren(PersonIdentity child) {

        if(!this.children.contains(child)){
            this.children.add(child);
        }
    }

    public PersonIdentity getRelation() {
        return relation;
    }

    public void setRelation(PersonIdentity relation) {

        if(this.relation == null){
        this.relation = relation;
        }
    }

    public Set<PersonIdentity> getParents() {
        return parents;
    }

    public void setParents(PersonIdentity parent) {
        this.parents.add(parent);
    }

//    public int returnDatabaseId(Statement statement) {
//        int id = 0;
//
//        ResultSet personId = null;
//        try{
//            personId = statement.executeQuery("select idpersonIdentity as id, personName as personName \n" +
//                                                  "from personidentity\n" +
//                                                  "where personName = "+ this.name +";");
//            while (personId.next()){
//                id = Integer.parseInt(personId.getString("id"));
//            }
//
//        }
//        catch (SQLException sqlException){
//
//        }
//    return id;
//    }

    public int getDataBaseId() {
        return dataBaseId;
    }

    public void setDataBaseId(int id) {
    this.dataBaseId = id;
    }

    public void addDataBaseId(Statement statement) {

        int id = 0;

        ResultSet personId = null;
        try{
            personId = statement.executeQuery("select max(idpersonIdentity) as id  from personidentity;");
            while (personId.next()){
                id = Integer.parseInt(personId.getString("id"));
            }

        }
        catch (SQLException sqlException){

        }

        this.dataBaseId = id;
    }

}
