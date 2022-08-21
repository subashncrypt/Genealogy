//package com.company;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class MediaTag {

    private String tag;
    private int dataBaseId;

    ArrayList<FileIdentifier> taggedFiles = new ArrayList<>();

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public ArrayList<FileIdentifier> getTaggedFiles() {
        return taggedFiles;
    }

    public void setTaggedFiles(ArrayList<FileIdentifier> taggedFiles) {
        this.taggedFiles = taggedFiles;
    }

    public int getDatabaseId() {
        return dataBaseId;
    }

    public void setDatabaseId(int databaseId) {
        this.dataBaseId = databaseId;
    }

    public void addDataBaseId(Statement statement) {

        int id = 0;

        ResultSet mediaTagId = null;
        try{
            mediaTagId = statement.executeQuery("select max(idmediaTag) as id  from mediatag;");
            while (mediaTagId.next()){
                id = Integer.parseInt(mediaTagId.getString("id"));
            }

        }
        catch (SQLException sqlException){

        }

        this.dataBaseId = id;
    }

}
