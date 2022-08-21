//package com.company;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The class that encapsulated all the people in a biological relation and related media files,
 * associated tags.
 */
public class Genealogy {

    // Private variables to store people and media
    private ArrayList<PersonIdentity> people = new ArrayList<>();           // Variable that stores all the PersonIdentity objects in the System
    private ArrayList<FileIdentifier> mediaFiles = new ArrayList<>();       // Variable that stores all media files
    private ArrayList<MediaTag> mediaTags = new ArrayList<>();              // Variable that stores Media tags.

    // connection variables
    private Connection connect = null;                    //  connection object that stores connection instance
    private Statement statement = null;                   // statement object used to execute statements
    private Properties prop = new Properties();           // properties object which connects to dataBase

    // constructor
    public Genealogy() {
        // set up dataBase connection
        setUpDatabaseConnection();

        // load data and build a family tree from the database
        loadDataBase();
    }


    // Add a person into the system and database

    /**
     * Add person function stores all the people and inserts the same into the date base
     * @param name
     * @return
     */
    public PersonIdentity addPerson( String name ){

        //local variable
    PersonIdentity newPerson = new PersonIdentity();

    // check if the name is valid
    if(name == null || name==""){
        throw new IllegalArgumentException("name cannot be null or empty");
    }

         // Adding the person into the array list of people
         newPerson.setName(name);
         people.add(newPerson);

         // insert into database
         // insert statement to set personidentity table with new person
         String query = "insert  into personidentity(idpersonIdentity,personName)\n" +
                 "values (?,?); ";

         try{
             // create the mysql insert prepared statement
             PreparedStatement preparedStmt = connect.prepareStatement(query);
             preparedStmt.setNull(1,Types.NULL);
             preparedStmt.setString    (2, name);

             // execute the prepared statement
             preparedStmt.execute();

             // get database id into the system
             newPerson.addDataBaseId((this.statement));
         }
         catch (SQLException sqlException){
            sqlException.printStackTrace();
         }
    return newPerson;
    }


    // Record attributes of a person

    /**
     * Records all the attributes of a person into the system and inserts it into the data base
     * @param person  PersonIdentity
     * @param attributes Map<String, String>
     * @return Boolean
     */
    public Boolean recordAttributes( PersonIdentity person, Map<String, String> attributes ) {

        // Validate person
        if(!validatePerson(person)){
            System.out.println("Person does not exists in the data base");
            return false;
        }

        // Status variables
        Boolean status = false;                 // Status that returns false
        String statusMessage = "";              // Status Message
        String statusattribute = "Please provide the below attributes only\n" +
                " 1) gender\n" +
                " 2) occupation\n" +
                " 3) birthlocation\n" +
                " 4) deathlocation\n" +
                " 5) birthdate\n" +
                " 6) deathdate\n";

        // check if the gender attribute exists in the map
        if(attributes.containsKey("gender")){
            person.setGender(attributes.get("gender"));
            status = true;
            statusMessage += "gender-";
        }

        // check if the occupation attribute exists in the map
        if(attributes.containsKey("occupation")){
            person.setOccupation(attributes.get("occupation"));
            status = true;
            statusMessage += "occupation-";
        }

        // check if the birthlocation attribute exists in the map
        if(attributes.containsKey("birthlocation")){
            person.setBirthLocation(attributes.get("birthlocation"));
            status = true;
            statusMessage += "birthlocation-";
        }

        // check if the deathlocation attribute exists in the map
        if(attributes.containsKey("deathlocation")){
            person.setDeathLocation(attributes.get("deathlocation"));
            status = true;
            statusMessage += "deathlocation-";
        }

        // check if the birthdate attribute exists in the map
        if(attributes.containsKey("birthdate")){
            Date date1 = getValidDate(attributes.get("birthdate"));
            person.setBirthDate(date1);
            status = true;
            statusMessage += "birthdate-";
        }

        // check if the deathdate attribute exists in the map
        if(attributes.containsKey("deathdate")){
            Date date1= getValidDate(attributes.get("deathdate"));
            person.setDeathDate(date1);
            status = true;
            statusMessage += "deathdate-\n";
        }

        // if no attribute exists return false
        if(!status){
            System.out.println(statusattribute);
            return false;
        }

        try {
            // Query to retrieve data
            ResultSet attributeResultSet;
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Integer id = person.getDataBaseId();

            String attributeQuery = "select * from personattributes where personIdentity_id = " + id.toString() + ";";

            attributeResultSet = this.statement.executeQuery(attributeQuery);

            // No data exists then Insert the attributes else update it
            if (!attributeResultSet.next()) {

                // insert attributes for a person
                String query = "insert into personattributes(idpersonAttributes,birthLocation,deathLocation,gender,occupation,birthDate,deathDate,personIdentity_id)\n" +
                        "values (?,?,?,?,?,?,?,?); ";
                try {
                    // create the mysql insert prepared statement
                    PreparedStatement preparedStmt = connect.prepareStatement(query);
                    preparedStmt.setNull(1, Types.NULL);
                    preparedStmt.setString(2, person.getBirthLocation());
                    preparedStmt.setString(3, person.getDeathLocation());
                    preparedStmt.setString(4, person.getGender());
                    preparedStmt.setString(5, person.getOccupation());

                    if (attributes.get("birthdate") == null) {
                        preparedStmt.setDate(6, null);
                    } else {
                        preparedStmt.setDate(6, java.sql.Date.valueOf(dateFormat.format(person.getBirthDate())));
                    }

                    if (attributes.get("deathdate") == null) {
                        preparedStmt.setDate(7, null);
                    } else {
                        preparedStmt.setDate(7, java.sql.Date.valueOf(dateFormat.format(person.getDeathDate())));
                    }

                    preparedStmt.setInt(8, person.getDataBaseId());

                    // execute the prepared statement
                    preparedStmt.execute();
                } catch (SQLException sqlException) {
                    System.out.println("Exception happeining");
                    return false;
                }
                statusMessage += "The above attributes has been successfully stored";
                System.out.println(statusMessage);
            } else {

                // insert attributes for a person
                String query = "update personattributes set birthLocation = ?,deathLocation = ?, gender =?, occupation =?,birthDate = ?, deathDate = ?\n" +
                        "where  idpersonAttributes = ?;";

                try {
                    // create the mysql insert prepared statement
                    PreparedStatement preparedStmt = connect.prepareStatement(query);
                    preparedStmt.setString(1, person.getBirthLocation());
                    preparedStmt.setString(2, person.getDeathLocation());
                    preparedStmt.setString(3, person.getGender());
                    preparedStmt.setString(4, person.getOccupation());

                    if (attributes.get("birthdate") == null) {
                        preparedStmt.setDate(5, null);
                    } else {
                        preparedStmt.setDate(5, java.sql.Date.valueOf(dateFormat.format(person.getBirthDate())));
                    }

                    if (attributes.get("deathdate") == null) {
                        preparedStmt.setDate(6, null);
                    } else {
                        preparedStmt.setDate(6, java.sql.Date.valueOf(dateFormat.format(person.getDeathDate())));
                    }

                    preparedStmt.setInt(7, person.getDataBaseId());

                    // execute the prepared statement
                    preparedStmt.execute();

                } catch (SQLException sqlException) {
                    sqlException.printStackTrace();
                }

                statusMessage += "The above attributes has been successfully stored";
                System.out.println(statusMessage);

            }
        }catch(SQLException sqlException){

            }

        return status;
    }

    /**
     * Record reference for a person
     * @param person person
     * @param reference reference
     * @return Boolean
     */
    public Boolean recordReference( PersonIdentity person, String reference ){

        // Check if the person exists in the system
        if(person == null || !validatePerson(person)){
            throw new IllegalArgumentException("PersonIdentity cannot be null");
        }

        // Check if the reference is Null
        if(reference == null || reference == ""){
            throw new IllegalArgumentException("Reference cannot be null");
        }

        // Sets both notes and reference
        person.setReferencesNotes(reference);

        // insert attributes for a person
        String query = "insert into personnotereference(idpersonNote,noteAndReference,personIdentity_id)\n" +
                "values (?,?,?); ";
        try{
            // create the mysql insert prepared statement
            PreparedStatement preparedStmt = connect.prepareStatement(query);
            preparedStmt.setNull(1,Types.NULL);
            preparedStmt.setString    (2,reference );
            preparedStmt.setInt   (3, person.getDataBaseId());

            // execute the prepared statement
            preparedStmt.execute();
        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
        }

        return true;
    }

    /**
     * Record notes for person
     * @param person PersonIdentity
     * @param note note
     * @return Boolean
     */
    public Boolean recordNote( PersonIdentity person, String note ){
        return recordReference(person,note);
    }

    /**
     * Record child and parent relation
     * @param parent PersonIdentity
     * @param child PersonIdentity
     * @return Boolean
     */
    public Boolean recordChild( PersonIdentity parent, PersonIdentity child ){

        // Local variable
        Boolean status = false;         // Status variable return parameter

        // validate of the people are valid
        // check if the person are not the same
        if(!validatePerson(parent) || !validatePerson(child) ||
                (parent.getDataBaseId() == child.getDataBaseId()) ){
            return false;
        }

        // validate if the child already exists
        if(parent.getChildren().contains(child)){
            System.err.println(" Child already exists in the data base for this person");
            return false;
        }
        try{
            // insert attributes for a person
            String query = "insert into personchild(parent_id,personIdentity_idChild)\n" +
                    "values (?,?); ";

            // create the mysql insert prepared statement
            PreparedStatement preparedStmt = connect.prepareStatement(query);

            if(!parent.getChildren().contains(child)){
                // Record children
                parent.setChildren(child);

                // set child parents both the current one and his relation
                child.setParents(parent);

                preparedStmt.setInt(1,parent.getDataBaseId());
                preparedStmt.setInt(2,child.getDataBaseId());

                // execute the prepared statement
                preparedStmt.execute();

                status = true;
            }

            // if a relation exists for a particular person
            // add child for that person
            if(parent.getRelation() != null &&
             !parent.getRelation().getChildren().contains(child)){

                parent.getRelation().setChildren(child);
                child.setParents(parent.getRelation());

                //record child for relation
                preparedStmt = connect.prepareStatement(query);
                preparedStmt.setInt(1,parent.getRelation().getDataBaseId());
                preparedStmt.setInt(2,child.getDataBaseId());

                // execute the prepared statement
                preparedStmt.execute();

                status = true;
            }
        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
        }

        return status;
    }

    /**
     * recordPartnering function that records relation between 2 people
     * @param partner1 PersonIdentity
     * @param partner2 PersonIdentity
     * @return Boolean
     */
    public Boolean recordPartnering( PersonIdentity partner1, PersonIdentity partner2 ){

        // local variable
        Boolean status = false; // return variable

        // validate if the people are valid
        if(!validatePerson(partner1) || !validatePerson(partner2) ||
        (partner1.getDataBaseId() == partner2.getDataBaseId())){
            return false;
        }

        // return false if a relation exists between people
        if(partner1.getRelation() != null ||
        partner2.getRelation() != null){
            return false;
        }

        // Assign relation between people and insert into the data base
        if(partner1.getRelation() == null && partner2.getRelation() == null){

            partner1.setRelation(partner2);
            partner2.setRelation(partner1);

            try{
                // insert attributes for a person
                String query = "insert into personrelationship(personIdentity_idpersonIdentity,personIdentity_id2)\n" +
                        "values (?,?); ";

                // create the mysql insert prepared statement
                PreparedStatement preparedStmt = connect.prepareStatement(query);
                preparedStmt.setInt(1,partner1.getDataBaseId());
                preparedStmt.setInt(2,partner2.getDataBaseId());

                // execute the prepared statement
                preparedStmt.execute();

                // Record the other way round relation in the database
                preparedStmt = connect.prepareStatement(query);

                preparedStmt.setInt(1,partner2.getDataBaseId());
                preparedStmt.setInt(2,partner1.getDataBaseId());

                // execute the prepared statement
                preparedStmt.execute();

                status =true;

            }
            catch (SQLException sqlException){
                System.out.println("Exception Occurred");
            }
        }else {
            throw new IllegalArgumentException("Relation already exists for the person");
        }

        return status;
    }

    /**
     * Function records dissolutions between 2 people
     * @param partner1
     * @param partner2
     * @return Boolean
     */
    public Boolean recordDissolution( PersonIdentity partner1, PersonIdentity partner2 ){

        // validate if peoples exist and are in a relationship with each other
        if(!validatePerson(partner1) || !validatePerson(partner1)
        || partner1.getRelation() != partner2 || partner2.getRelation()!= partner1){
            return false;
        }

        // record dissolutions
        partner1.setRelation(null);
        partner2.setRelation(null);

        Integer databaseId = partner1.getDataBaseId();
        String p1 = databaseId.toString();

        // table delete
        try{
            String DeleteinTable = "DELETE FROM personrelationship pr\n" +
                    "WHERE pr.personIdentity_idpersonIdentity = "+ p1 +" or pr.personIdentity_id2 = "+ p1 +";";

            statement.executeUpdate(DeleteinTable);

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    // Manage media archive

    /**
     * Add Media files based on filelocation
     * @param fileLocation
     * @return FileIdentifier
     */
    public FileIdentifier addMediaFile( String fileLocation ){

        // Validate if the file location already exists in the system
        if(!validateMediaName(fileLocation)){
            throw new IllegalArgumentException("File location already exists in the system");
        }

        // Validate if the incoming parameter is null or empty
        if(fileLocation == null || fileLocation==""){
            throw new IllegalArgumentException("File location cannot be empty");
        }

        // local variable
        FileIdentifier newFile = new FileIdentifier(fileLocation);

        // Adding the Media file
        newFile.setFileLocation(fileLocation);
        this.mediaFiles.add(newFile);

        // insert into database
        // insert statement to set personidentity table with new person
        String query = "insert  into mediafile(idmediaFile,fileLocation)\n" +
                "values (?,?); ";

        try{
            // create the mysql insert prepared statement
            PreparedStatement preparedStmt = connect.prepareStatement(query);
            preparedStmt.setNull(1,Types.NULL);
            preparedStmt.setString    (2, fileLocation);

            // execute the prepared statement
            preparedStmt.execute();

            // get database id into the system
            newFile.addDataBaseId(this.statement);
        }
        catch (SQLException sqlException){

        }

        return newFile;
    }

    /**
     * recordMediaAttributes is a function that records attributes of a file
     * @param fileIdentifier FileIdentifier
     * @param attributes Map<String, String>
     * @return Boolean
     */
    public Boolean recordMediaAttributes( FileIdentifier fileIdentifier, Map<String, String> attributes ){

        // Local variable return parameter
        Boolean status = false;

        // validate file if it exists in the data base
        if(!validateMedia(fileIdentifier)){
            System.out.println("File does not exists in the data base");
            return false;
        }

        String statusMessage = "Please provide the below attributes only\n" +
                " 1) date\n" +
                " 2) location\n" +
                " 3) city\n" +
                " 4) province\n" +
                " 5) country\n";

        if(attributes.containsKey("location")){
            fileIdentifier.setPlace(attributes.get("location"));
            status = true;
            statusMessage += "location-";
        }

        if(attributes.containsKey("city")){
            fileIdentifier.setCity(attributes.get("city"));
            status = true;
            statusMessage += "city-";
        }

        if(attributes.containsKey("province")){
            fileIdentifier.setProvince(attributes.get("province"));
            status = true;
            statusMessage += "province-";
        }

        if(attributes.containsKey("country")){
            fileIdentifier.setCountry(attributes.get("country"));
            status = true;
            statusMessage += "country-";
        }

        if(attributes.containsKey("date")){
            Date date1 = getValidDate(attributes.get("date"));
            fileIdentifier.setDate(date1);
            status = true;
            statusMessage += "date-\n";
        }

        // Exists then update else insert
        try{
            // Result set to store the data retrieved
            ResultSet attributeResultSet;
            // Format to parese the date
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            // stores data base id
            Integer id = fileIdentifier.getDataBaseId();
            // Query to retrieve data
            String attributeQuery = "select * from mediaattribute where mediaFile_idmediaFile = "+ id.toString() +";";

            attributeResultSet = this.statement.executeQuery(attributeQuery);

            if(!attributeResultSet.next()){

                // insert attributes for a person
                String query = "insert into mediaattribute(idmediaAttribute,date,place,city,province,country,mediaFile_idmediaFile)\n" +
                        "values (?,?,?,?,?,?,?); ";
                try{
                    // create the mysql insert prepared statement
                    PreparedStatement preparedStmt = connect.prepareStatement(query);
                    preparedStmt.setNull(1,Types.NULL);

                    if (attributes.get("date") == null){
                        preparedStmt.setDate    (2, null);
                    }else{
                        preparedStmt.setDate    (2, java.sql.Date.valueOf(dateFormat.format(fileIdentifier.getDate())));
                    }
                    preparedStmt.setString    (3, fileIdentifier.getPlace());
                    preparedStmt.setString    (4, fileIdentifier.getCity());
                    preparedStmt.setString    (5, fileIdentifier.getProvince());
                    preparedStmt.setString    (6, fileIdentifier.getCountry());
                    preparedStmt.setInt    (7, fileIdentifier.getDataBaseId());

                    // execute the prepared statement
                    preparedStmt.execute();
                }
                catch (SQLException sqlException){
                    System.out.println("Exception happens");
                    return false;
                }

                statusMessage += "The above attributes has been successfully stored";

                System.out.println(statusMessage);

            }
            // Update the row in the dataBase
            else {
                String query = "update mediaattribute set date = ? ,place = ?, city = ?,  province=?, country = ?\n" +
                                "where mediaFile_idmediaFile = ?;";

                try{
                    // create the mysql insert prepared statement
                    PreparedStatement preparedStmt = connect.prepareStatement(query);

                    if (attributes.get("date") == null){
                        preparedStmt.setDate    (1, null);
                    }else{
                        preparedStmt.setDate    (1, java.sql.Date.valueOf(dateFormat.format(fileIdentifier.getDate())));
                    }
                    preparedStmt.setString    (2, fileIdentifier.getPlace());
                    preparedStmt.setString    (3, fileIdentifier.getCity());
                    preparedStmt.setString    (4, fileIdentifier.getProvince());
                    preparedStmt.setString    (5, fileIdentifier.getCountry());
                    preparedStmt.setInt    (6, fileIdentifier.getDataBaseId());

                    // execute the prepared statement
                    preparedStmt.execute();
                }
                catch (SQLException sqlException){
                    System.out.println("Exception happens");
                    return false;
                }

            }

        }catch (SQLException sqlException){
            sqlException.printStackTrace();
        }


        return status;
    }

    /**
     * Function sets people in the media
     * @param fileIdentifier FileIdentifier
     * @param people List<PersonIdentity>
     * @return  Boolean
     */
    public Boolean peopleInMedia( FileIdentifier fileIdentifier, List<PersonIdentity> people ){

        Boolean status = false;

        if(validateMedia(fileIdentifier)){
            for (int i = 0; i < people.size(); i++) {

                // validate if the person exists in the system
                if(validatePerson(people.get(i))){
                    fileIdentifier.setPeopleInMedia(people.get(i));
                    people.get(i).setMediaFiles(fileIdentifier);

                    // insert attributes for a person
                    String query = "insert into mediapeople(personIdentity_idpersonIdentity,mediaFile_idmediaFile)\n" +
                            "values (?,?); ";
                    try {
                        // create the mysql insert prepared statement
                        PreparedStatement preparedStmt = connect.prepareStatement(query);
                        preparedStmt.setInt(1,people.get(i).getDataBaseId() );
                        preparedStmt.setInt(2,fileIdentifier.getDataBaseId());

                        preparedStmt.execute();
                    }
                    catch (SQLException sqlException){
                        System.out.println("Exception while inserting media people table data");
                    }
                        status = true;
                }
            }
        }
    return status;
    }

    /**
     * tagMedia is a function that stores file identifier
     * @param fileIdentifier
     * @param tag
     * @return Boolean
     */
    public Boolean tagMedia( FileIdentifier fileIdentifier, String tag ){

        // validate if its null
        if(tag == null || tag==""){
            throw new IllegalArgumentException("File location cannot be empty");
        }

        // validate fileIdentifier if it exists
        if(!validateMedia(fileIdentifier)){
            throw new IllegalArgumentException("Media file does not exists");
        }

        MediaTag newTag = validateMediaTag(tag);

        if(newTag == null) {
            // create a new tag add it to media tag dataBase
            newTag = new MediaTag();

            newTag.setTag(tag);

            // add to the list if media tags
            this.mediaTags.add(newTag);

            // insert attributes for a person
            String query = "insert into mediatag(idmediaTag,tag)\n" +
                    "values (?,?); ";

            try {
                // create the mysql insert prepared statement
                PreparedStatement preparedStmt = connect.prepareStatement(query);
                preparedStmt.setNull(1, Types.NULL);
                preparedStmt.setString(2, newTag.getTag());
            } catch (SQLException sqlException) {
                System.out.println("Exception while inserting mediaTag table data");
            }

            newTag.addDataBaseId(this.statement);

        }

            // add media file to the tag object
            newTag.getTaggedFiles().add(fileIdentifier);

            // add tag object to the media files
            fileIdentifier.getTags().add(newTag);

            // add tag media relation to the tags table in the database.
            String query = "insert into tags(mediaFile_idmediaFile,mediaTag_idmediaTag)\n" +
                    "values (?,?); ";

            try {
                // create the mysql insert prepared statement
                PreparedStatement preparedStmt = connect.prepareStatement(query);
                preparedStmt.setInt(1,newTag.getDatabaseId());
                preparedStmt.setInt(2,fileIdentifier.getDataBaseId());
            }
            catch (SQLException sqlException){
                System.out.println("Exception while inserting mediaTag table data");
            }

    return true;
    }

    // Reporting functions

    /**
     * findPerson function gets the name of the function and sends out a list of all the
     * people with the name
     * @param name
     * @return List<PersonIdentity>
     */
    public List<PersonIdentity> findPerson( String name ){

        List<PersonIdentity> peopleByName = new ArrayList<>();

        for (int i = 0; i < people.size(); i++) {
            if(people.get(i).getName().equals(name)){
                peopleByName.add(people.get(i));
            }
        }
        return peopleByName;
    }


    /**
     * findMediaFile function that get's a Media file based on the name
     * @param name
     * @return FileIdentifier
     */
    FileIdentifier findMediaFile( String name ){

        for (int i = 0; i < this.mediaFiles.size(); i++) {
            if(mediaFiles.get(i).getFileLocation().equals(name)){
                return mediaFiles.get(i);
            }
        }

        return null;
    }

    /**
     * findName is a function that returns a name of a person
     * @param id
     * @return String
     */
    String findName( PersonIdentity id ){

        if(validatePerson(id)){
            return id.getName();
        }
        else {
            return null;
        }
    }

    /**
     * findMediaFile is a function that returns a location of a fileIdentifier
     * @param fileId
     * @return String
     */
    String findMediaFile( FileIdentifier fileId ){
        if(validateMedia(fileId)){
            return fileId.getFileLocation();
        }
        else {
            return null;
        }
    }

    /**
     * findRelation finds a relation between 2 people in a family tree
     * @param person1
     * @param person2
     * @return BiologicalRelation
     */
    BiologicalRelation findRelation( PersonIdentity person1, PersonIdentity person2 ){
        BiologicalRelation object = new BiologicalRelation();

        HashMap<PersonIdentity, Integer> person1ancestors = new HashMap<PersonIdentity,Integer>();
        HashMap<PersonIdentity, Integer> person2ancestors = new HashMap<PersonIdentity,Integer>();

        Integer person1depth = Integer.MAX_VALUE;
        Integer person2depth = Integer.MAX_VALUE;

        Integer degreeOfCousinship;
        Integer degreeOfRemoval;

        // function that return hashmap of all ancestors of a person
        person1ancestors = personAncestors(person1,0);

        // function that return hashmap of all ancestors of a person
        person2ancestors = personAncestors(person2,0);


        System.out.println("Commmon ");
        for (PersonIdentity p1: person1ancestors.keySet()) {
            for ( PersonIdentity p2: person2ancestors.keySet()) {
                if (p1.getDataBaseId() == p2.getDataBaseId()){
                    System.out.println(p1.getName());
                    if(person1ancestors.get(p1) <= person1depth
                    && person2ancestors.get(p2) <= person2depth){
                        person1depth = person1ancestors.get(p1);
                        person2depth = person2ancestors.get(p2);
                    }
                }
            }
        }


        if(person1depth == Integer.MAX_VALUE ||
          person2depth == Integer.MAX_VALUE){
            System.out.println(" Not related biologically");
            return null;
        }else {

            degreeOfCousinship = Integer.min(person1depth,person2depth)-1;
            degreeOfRemoval = Math.abs(person1depth-person2depth);
            object.setDegreeOfCousinship(degreeOfCousinship);
            object.setDegreeOfRemoval(degreeOfRemoval);
        }

        return object;
    }

    /**
     * Function that returns ancestor with the offset from the person
     * @param person PersonIdentity
     * @param current Integer
     * @return HashMap<PersonIdentity, Integer>
     */
    HashMap<PersonIdentity, Integer> personAncestors(PersonIdentity person,Integer current){
        HashMap<PersonIdentity, Integer> ancestors = new HashMap<PersonIdentity,Integer>();

        ancestors.put(person,current);

        for (PersonIdentity p: person.getParents()) {
            ancestors.putAll(personAncestors(p,current+1));
        }

        return ancestors;
    }

    /**
     * decendents function that prints
     * @param person  PersonIdentity
     * @param generations Integer
     * @return  Set<PersonIdentity>
     */
    Set<PersonIdentity> descendents( PersonIdentity person, Integer generations ){
        Set<PersonIdentity> nDecendent = new HashSet<>();

        if(generations > 0 && validatePerson(person)){
            getdescendents(person,generations,0,nDecendent);
        }

        return nDecendent;
    }

    /**
     * Function the calculates descendants
     * @param person PersonIdentity
     * @param generations Integer
     * @param currentgeneration Integer
     * @param nDecendent Set<PersonIdentity>
     * @return  Set<PersonIdentity>
     */
    Set<PersonIdentity> getdescendents(PersonIdentity person,Integer generations, Integer currentgeneration,Set<PersonIdentity> nDecendent){

        if(generations == currentgeneration){
            return nDecendent;
        }
        else {
            for (PersonIdentity per : person.getChildren()) {
                nDecendent.add(per);
                nDecendent.addAll(getdescendents(per,generations,currentgeneration+1,nDecendent));
            }
        }

        return nDecendent;
    }

    /**
     * Function that returns ancestors
     * @param person PersonIdentity
     * @param generations Integer
     * @return Set<PersonIdentity>
     */
    Set<PersonIdentity> ancestores( PersonIdentity person, Integer generations ){
        Set<PersonIdentity> nAncestors = new HashSet<>();

        if(generations > 0 && validatePerson(person)){

            getancestores(person,generations,0,nAncestors);

        }

        return nAncestors;
    }

    /**
     * Function to get Notes and reference
     * @param person
     * @param generations
     * @param currentgeneration
     * @param nAncestors
     * @return  Set<PersonIdentity>
     */
    Set<PersonIdentity> getancestores(PersonIdentity person,Integer generations, Integer currentgeneration,Set<PersonIdentity> nAncestors){

        if(generations == currentgeneration){
            return nAncestors;
        }
        else {
            for (PersonIdentity per : person.getParents()) {
                nAncestors.add(per);
                nAncestors.addAll(getancestores(per,generations,currentgeneration+1,nAncestors));
            }
        }
        return nAncestors;
    }


    /**
     * Function that returns List<String>
     * @param person PersonIdentity
     * @return List<String>
     */
    List<String> notesAndReferences( PersonIdentity person ){

        if(validatePerson(person)){
            return person.getReferencesNotes();
        }else {
            return null;
        }

    }

    /**
     * findMediaByTag
     * @param tag String
     * @param startDate  String
     * @param endDate String
     * @return  Set<FileIdentifier>
     */
    Set<FileIdentifier> findMediaByTag( String tag , String startDate, String endDate) {

        Set<FileIdentifier> media = new HashSet<>();
        FileIdentifier file;
        ResultSet TagResult;
        int idMedia;
        ArrayList<FileIdentifier> meidafiles = new ArrayList<>();
        Date sDate = getValidDate(startDate);
        Date eDate = getValidDate(endDate);
        String dateString = null;
        SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd");


        if(sDate.compareTo(eDate) > 0){
            throw new IllegalArgumentException("Start cannot be greater than end date");
        }

        if(sDate != null || eDate != null){

            if(sDate != null && eDate != null){
                startDate = newFormat.format(sDate);
                endDate   = newFormat.format(eDate);
                dateString = "and ma.date between "+ startDate +"and" + endDate+"\n";
            }else if(sDate == null){
                endDate   = newFormat.format(eDate);
                dateString = "and ma.date < "+endDate +"\n";
            }else if(eDate == null){
                startDate = newFormat.format(sDate);
                dateString = "and ma.date > "+ startDate +"\n";
            }

        }else {
            dateString ="\n";
        }


        try{
            // query to get all the Tagged files
            String mediaQuery = "select mp.mediaFile_idmediaFile as mediaFile \n" +
                    "from mediatag mp join mediaattribute ma on mp.idmediaTag = ma.mediaFile_idmediaFile\n" +
                    "join tags ta on ta.mediaFile_idmediaFile = ma.mediaFile_idmediaFile \n" +
                    "and ta.mediaTag_idmediaTag = mp.idmediaTag\n" +
                    "where mp.tag = "+ tag +"\n" +
                    dateString +
                    "order by ma.date is NUll, ma.date ;";

            // execute the query
            TagResult = statement.executeQuery(mediaQuery);

            // get all
            while (TagResult.next()){
                idMedia = Integer.parseInt(TagResult.getString("mediaFile"));
                file = returnMediaId(idMedia);

                if(file != null){
                    media.add(file);
                }
            }
        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
        }

        return media;
    }

    /**
     * findMediaByLocation  retunrs set of media files based on location
     * @param location String
     * @param startDate String
     * @param endDate String
     * @return Set<FileIdentifier>
     */
    Set<FileIdentifier> findMediaByLocation( String location, String startDate, String endDate){

        Set<FileIdentifier> media = new HashSet<>();
        ResultSet mediaResult;
        FileIdentifier file;
        int idMedia;
        Date sDate = getValidDate(startDate);
        Date eDate = getValidDate(endDate);
        String dateString = null;

        SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd");

        if(sDate.compareTo(eDate) > 0){
            throw new IllegalArgumentException("Start cannot be greater than end date");
        }

        if(sDate != null || eDate != null){

            if(sDate != null && eDate != null){
                startDate = newFormat.format(sDate);
                endDate   = newFormat.format(eDate);
                dateString = "and ma.date between "+ startDate +"and" + endDate+"\n";
            }else if(sDate == null){
                endDate   = newFormat.format(eDate);
                dateString = "and ma.date < "+endDate +"\n";
            }else if(eDate == null){
                startDate = newFormat.format(sDate);
                dateString = "and ma.date > "+ startDate +"\n";
            }

        }else {
            dateString ="\n";
        }

        try{
            // query to get all the Media files by location
            String mediaQuery = "select distinct ma.mediaFile_idmediaFile\n" +
                    "from mediaattribute ma\n" +
                    "where ma.place = "+location+"\n" +
                    dateString +
                    "order by ma.date is NUll, ma.date ;";

            // execute the query
            mediaResult = statement.executeQuery(mediaQuery);

            // get all the parameters like total cost, tax percentage from the data base
            while (mediaResult.next()){
                idMedia = Integer.parseInt(mediaResult.getString("mediaFile_idmediaFile"));
                file = returnMediaId(idMedia);

                if(file != null){
                    media.add(file);
                }

            }
        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
        }


        return media;
    }

    /**
     * findIndividualsMedia based on people present in set
     * @param people Set<PersonIdentity>
     * @param startDate String
     * @param endDate String
     * @return List<FileIdentifier>
     */
    List<FileIdentifier> findIndividualsMedia( Set<PersonIdentity> people, String startDate, String endDate){

        List<FileIdentifier> mediaFiles= new ArrayList<FileIdentifier>();
        Set<FileIdentifier> meidaFilesSet = new HashSet<>();
        FileIdentifier file;
        ResultSet bioMedia = null;
        SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = null;
        int idMedia;

        Date sDate = getValidDate(startDate);
        Date eDate = getValidDate(endDate);

        if(sDate.compareTo(eDate) > 0){
            throw new IllegalArgumentException("Start cannot be greater than end date");
        }

        if(sDate != null || eDate != null){

            if(sDate != null && eDate != null){
                startDate = newFormat.format(sDate);
                endDate   = newFormat.format(eDate);
                dateString = "and ma.date between "+ startDate +"and" + endDate+"\n";
            }else if(sDate == null){
                endDate   = newFormat.format(eDate);
                dateString = "and ma.date <= "+ endDate +"\n";
            }else if(eDate == null){
                startDate = newFormat.format(sDate);
                dateString = "and ma.date >= "+ startDate +"\n";
            }

        }else {
            dateString ="\n";
        }

        for (PersonIdentity p: people) {
            if(validatePerson(p)){
                    try{
                        // query to get all the details from the data base
                        String bioMediaQuery = "select distinct mf.idmediaFile as mediaId\n" +
                                "\tfrom mediapeople mp join mediafile mf on mp.mediaFile_idmediaFile = mf.idmediaFile\n" +
                                "    join mediaattribute ma on ma.mediaFile_idmediaFile = mf.idmediaFile\n" +
                                "    where mp.personIdentity_idpersonIdentity ="+ p.getDataBaseId()+"\n" +
                                dateString +
                                "    order by ma.date is NUll, ma.date;";

                        // execute the query
                        bioMedia = statement.executeQuery(bioMediaQuery);

                        // get all the parameters like total cost, tax percentage from the data base
                        while (bioMedia.next()){
                            idMedia = Integer.parseInt(bioMedia.getString("mediaId"));
                            file = returnMediaId(idMedia);
                            if(file != null){
                                meidaFilesSet.add(file);
                            }
                        }
                    }
                    catch (SQLException sqlException){
                        sqlException.printStackTrace();
                    }

            }
        }

        mediaFiles = meidaFilesSet.stream().toList();

    return mediaFiles;
    }

    /**
     * person findBiologicalFamilyMedia retuens list of FileIdentifier
     * @param person
     * @return  List<FileIdentifier>
     */
    List<FileIdentifier> findBiologicalFamilyMedia(PersonIdentity person){

        List<FileIdentifier> mediaFiles= new ArrayList<FileIdentifier>();
        ResultSet bioMedia = null;
        FileIdentifier File;
        String personId;
        int idMedia;

        if(validatePerson(person)){
            personId = String.valueOf(person.getDataBaseId());
            try{

                // query to get all the details from the data base
                String bioMediaQuery = "select distinct mf.idmediaFile as mediaId\n" +
                        "from mediapeople mp \n" +
                        "join mediafile mf on mp.mediaFile_idmediaFile = mf.idmediaFile\n" +
                        "join mediaattribute ma on ma.mediaFile_idmediaFile = mf.idmediaFile\n" +
                        "where mp.personIdentity_idpersonIdentity \n" +
                        "in ( select pc.personIdentity_idChild \n" +
                        "\t from personidentity per join personchild pc on pc.parent_id = per.idpersonIdentity\n" +
                        "     where per.idpersonIdentity = "+ personId +")\n" +
                        "order by ma.date is NUll, ma.date ;";

                // execute the query
                bioMedia = statement.executeQuery(bioMediaQuery);

                // get all the parameters like total cost, tax percentage from the data base
                while (bioMedia.next()){
                    idMedia = Integer.parseInt(bioMedia.getString("mediaId"));

                    File = returnMediaId(idMedia);

                    if(File != null){
                        mediaFiles.add(File);
                    }
                }
            }
            catch (SQLException sqlException){
                sqlException.printStackTrace();
            }


        }

        return mediaFiles;
    }

    /**
     *  Function that setUpDatabaseConnection
     */
    private void setUpDatabaseConnection(){

        // Step 1 :  Connect to the dataBase
        try (InputStream input = new FileInputStream( "config_database_Connection.properties")){

            // load the properties file into the system
            this.prop.load(input);

            // connect to the data base
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connect = DriverManager.getConnection(prop.getProperty("connectionURL"), prop.getProperty("user"), prop.getProperty("password"));
            this.statement = connect.createStatement();

        }
        catch (IOException | ClassNotFoundException | SQLException ioException)
        {
            ioException.printStackTrace();
        }
    }

    /**
     * Load from the data and create objetcs
     */
    private void loadDataBase(){

        // Load people from the dataBase
        String peopleQuery = "select idpersonIdentity as id, personName as personName from personidentity;";
        ResultSet person;

        try {
            person = this.statement.executeQuery(peopleQuery);

            while (person.next()) {
                PersonIdentity dataPerson = new PersonIdentity();

                dataPerson.setName(person.getString("personName"));
                dataPerson.setDataBaseId(Integer.parseInt(person.getString("id")));
                this.people.add(dataPerson);
            }
        }
        catch(SQLException sqlException){
            sqlException.printStackTrace();
        }

        // Load people attributes from the dataBase
        String peopleattributeQuery = "select personIdentity_id,birthLocation,deathLocation,gender,occupation,birthDate,deathDate\n" +
                "from personattributes;";
        ResultSet personAttribute;

        try {
            personAttribute = this.statement.executeQuery(peopleattributeQuery);
            PersonIdentity dataPerson;
            while (personAttribute.next()) {
                dataPerson = returnPerson(Integer.parseInt(personAttribute.getString("personIdentity_id")));
                dataPerson.setBirthLocation(personAttribute.getString("birthLocation"));
                dataPerson.setDeathLocation(personAttribute.getString("deathLocation"));
                dataPerson.setGender(personAttribute.getString("gender"));
                dataPerson.setOccupation(personAttribute.getString("occupation"));

                if(personAttribute.getString("birthDate") != null &&
                        personAttribute.getString("birthDate") != ""){
                    Date date= new SimpleDateFormat("yyyy-MM-dd").parse(personAttribute.getString("birthDate"));
                    dataPerson.setBirthDate(date);
                }

                if(personAttribute.getString("deathDate") != null &&
                        personAttribute.getString("deathDate") != ""){

                    Date date= new SimpleDateFormat("yyyy-MM-dd").parse(personAttribute.getString("deathDate"));
                    dataPerson.setDeathDate(date);
                }

            }
        }
        catch(SQLException sqlException){
            sqlException.printStackTrace();
        }catch (ParseException parseException){
            parseException.printStackTrace();
        }

        // Load people relationships
        String peopleRelationQuery= "select personIdentity_idpersonIdentity, personIdentity_id2 from personrelationship;";
        ResultSet personRelation;

        try {
            personRelation = this.statement.executeQuery(peopleRelationQuery);
            PersonIdentity dataPerson;
            while (personRelation.next()) {
                dataPerson = returnPerson(Integer.parseInt(personRelation.getString("personIdentity_idpersonIdentity")));
                dataPerson.setRelation(returnPerson(Integer.parseInt(personRelation.getString("personIdentity_id2"))));
            }
        }
        catch(SQLException sqlException){
            sqlException.printStackTrace();
        }

        // Load people children
        String peopleChildren= "select parent_id, personIdentity_idChild from personchild;";
        ResultSet personChildren;

        try {
            personChildren = this.statement.executeQuery(peopleChildren);
            PersonIdentity Parent;
            PersonIdentity child;
            while (personChildren.next()) {
                Parent = returnPerson(Integer.parseInt(personChildren.getString("parent_id")));
                child = returnPerson(Integer.parseInt(personChildren.getString("personIdentity_idChild")));

                Parent.setChildren(child);
                child.setParents(Parent);
            }
        }
        catch(SQLException sqlException){
            sqlException.printStackTrace();
        }


        // Load people notes and reference
        String peopleNotesRef= "select personIdentity_id,noteAndReference from personnotereference\n" +
                "order by idpersonNote;";
        ResultSet personNotesReference;

        try {
            personNotesReference = this.statement.executeQuery(peopleNotesRef);
            PersonIdentity personNote;
            while (personNotesReference.next()) {
                personNote = returnPerson(Integer.parseInt(personNotesReference.getString("personIdentity_id")));
                personNote.setReferencesNotes(personNotesReference.getString("personIdentity_idChild"));

            }
        }
        catch(SQLException sqlException){
            sqlException.printStackTrace();
        }


        // Load MediaFiles from the dataBase
        String mediaQuery = "select idmediaFile as id ,fileLocation as filename from mediafile;";
        ResultSet media;

        try {
            media = this.statement.executeQuery(mediaQuery);

            while (media.next()) {
                FileIdentifier datafile = new FileIdentifier("");

                datafile.setFileLocation(media.getString("filename"));
                datafile.setDataBaseId(Integer.parseInt(media.getString("id")));
                this.mediaFiles.add(datafile);
            }
        }
        catch(SQLException sqlException){
            sqlException.printStackTrace();
        }

        // Load Media Attributes from the database
        String mediaAttribute = "select mediaFile_idmediaFile,date,place,city,province,country\n" +
                "from mediaattribute\n" +
                "order by idmediaAttribute;";

        ResultSet mediaAttributes;

        try {
            mediaAttributes = this.statement.executeQuery(mediaAttribute);
            FileIdentifier datafile = new FileIdentifier("");
            while (mediaAttributes.next()) {
            datafile  = returnMediaId(Integer.parseInt(mediaAttributes.getString("mediaFile_idmediaFile")));

            if(mediaAttributes.getString("date") != null){
                Date date = new SimpleDateFormat("yyyy-MM-dd").parse(mediaAttributes.getString("date"));
                datafile.setDate(date);
            }

            datafile.setPlace(mediaAttributes.getString("place"));
            datafile.setCity(mediaAttributes.getString("city"));
            datafile.setProvince(mediaAttributes.getString("province"));
            datafile.setCountry(mediaAttributes.getString("country"));

            }
        }
        catch(SQLException sqlException){
            sqlException.printStackTrace();
        }catch (ParseException parseException){
            parseException.printStackTrace();
        }


        // Load MediaTag from the dataBase
        String mediaTagQuery = "select idmediaTag,tag from mediatag;";
        ResultSet mediaTag;

        try {
            mediaTag = this.statement.executeQuery(mediaTagQuery);

            while (mediaTag.next()) {
                MediaTag mediaTagObj = new MediaTag();

                mediaTagObj.setDatabaseId(Integer.parseInt(mediaTag.getString("id")));
                mediaTagObj.setTag(mediaTag.getString("tag"));

                this.mediaTags.add(mediaTagObj);
            }
        }
        catch(SQLException sqlException){
            sqlException.printStackTrace();
        }

        // Load TagMedia mapping from the dataBase
        String mediaTagMap = "select mediaFile_idmediaFile,mediaTag_idmediaTag from tags;";
        ResultSet tagMap;

        try {
            tagMap = this.statement.executeQuery(mediaTagMap);
            MediaTag oneMediaTag;
            FileIdentifier oneFileIdentifier;

            while (tagMap.next()) {
                oneFileIdentifier = returnMediaId(Integer.parseInt(tagMap.getString("mediaFile_idmediaFile")));
                oneMediaTag = returnMediaTag(Integer.parseInt(tagMap.getString("mediaTag_idmediaTag")));

                if(oneMediaTag != null && oneFileIdentifier != null){
                    oneFileIdentifier.getTags().add(oneMediaTag);
                    oneMediaTag.getTaggedFiles().add(oneFileIdentifier);
                }
            }
        }
        catch(SQLException sqlException){
            sqlException.printStackTrace();
        }

        // Load MediaPeople  from the dataBase
        String mediaPeople = "select personIdentity_idpersonIdentity,mediaFile_idmediaFile from mediapeople;";
        ResultSet peopleMedia;

        try {
            peopleMedia = this.statement.executeQuery(mediaPeople);
            PersonIdentity onePerson;
            FileIdentifier oneFileIdentifier;

            while (peopleMedia.next()) {
                oneFileIdentifier = returnMediaId(Integer.parseInt(peopleMedia.getString("mediaFile_idmediaFile")));
                onePerson = returnPerson(Integer.parseInt(peopleMedia.getString("personIdentity_idpersonIdentity")));

                if(onePerson != null && oneFileIdentifier != null){
                    onePerson.getMediaFiles().add(oneFileIdentifier);
                    oneFileIdentifier.getPeopleInMedia().add(onePerson);
                }
            }
        }
        catch(SQLException sqlException){
            sqlException.printStackTrace();
        }
    }

    /**
     * validatePerson person retuns true for a valid person
     * @param person
     * @return Boolean
     */
    public Boolean validatePerson(PersonIdentity person){

        for (int i = 0; i < this.people.size(); i++) {
            if (this.people.get(i).getDataBaseId() == person.getDataBaseId()
            && this.people.get(i).getName().equals(person.getName())){
                return true;
            }
        }

        return false;
    }

    /**
     * PersonIdentity is a retuns the person based on the dat base id
     * @param id
     * @return
     */
    public PersonIdentity returnPerson(Integer id){

        for (int i = 0; i < this.people.size(); i++) {
            if (this.people.get(i).getDataBaseId() == id){
                return this.people.get(i);
            }
        }
        return null;
    }

    /**
     * Validate media truns true if the file exists
     * @param file
     * @return
     */
    public Boolean validateMedia(FileIdentifier file){

        for (int i = 0; i < this.mediaFiles.size(); i++) {
            if (this.mediaFiles.get(i).getDataBaseId() == file.getDataBaseId()
                    && this.mediaFiles.get(i).getFileLocation().equals(file.getFileLocation())){
                return true;
            }
        }

        return false;
    }

    /**
     * FUnction that retuns Fileidentifier based in file id
     * @param fileId
     * @return
     */
    public FileIdentifier returnMediaId(int fileId){

        for (int i = 0; i < this.mediaFiles.size(); i++) {
            if (this.mediaFiles.get(i).getDataBaseId() == fileId){
                return this.mediaFiles.get(i);
            }
        }
        return null;
    }


    /**
     * Function that returns true when locationname exists in the system
     * @param locationName
     * @return
     */
    public Boolean validateMediaName(String locationName){

        for (int i = 0; i < this.mediaFiles.size(); i++) {
            if (this.mediaFiles.get(i).getFileLocation().equals(locationName)){
                return false;
            }
        }

        return true;
    }

    /**
     * validateMedia tag is a function that returns MediaTag based on Tag
     * @param tag
     * @return
     */
    public MediaTag validateMediaTag(String tag){

        for (int i = 0; i < this.mediaTags.size(); i++) {
            if(this.mediaTags.get(i).getTag().equals(tag)){
                return this.mediaTags.get(i);
            }
        }

        return null;
    }

    /**
     * returnMediaTag function retuns Media tag based on id
     * @param id
     * @return
     */
    public MediaTag returnMediaTag(int id){

        for (int i = 0; i < this.mediaTags.size(); i++) {
            if(this.mediaTags.get(i).getDatabaseId() == id){
                return this.mediaTags.get(i);
            }
        }
        return null;
    }

    /**
     * Function that parse date and sets it to a standard format
     * @param stringDate
     * @return
     */
    Date getValidDate(String stringDate){

        if(stringDate == null){
            return null;
        }
        Date validDate = new Date();

        String yearRegex = "((?:19|20)[0-9][0-9])";
        String yearMonthRegex = "(((?:19|20)[0-9][0-9])-(0?[1-9]|1[012]))";
        String YearMonthDayRegex = "((?:19|20)[0-9][0-9])-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])";

        Pattern patternYear = Pattern.compile(yearRegex);
        Pattern patternYearMonth = Pattern.compile(yearMonthRegex);
        Pattern patternYearMonthDay = Pattern.compile(YearMonthDayRegex);

        Matcher matcheryear = patternYear.matcher(stringDate);
        Matcher matcherYearMonth = patternYearMonth.matcher(stringDate);
        Matcher matherYearMonthDay = patternYearMonthDay.matcher(stringDate);

        try{
            if(matcheryear.matches()){
                stringDate = stringDate.trim();
                stringDate   += "-01-01";
                SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
                validDate = dateformat.parse(stringDate);
            }else if(matcherYearMonth.matches()){
                stringDate = stringDate.trim();
                stringDate   += "-01";
                SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
                dateformat.setLenient(false);
                validDate = dateformat.parse(stringDate);
            }else if(matherYearMonthDay.matches()){
                SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
                dateformat.setLenient(false);
                validDate = dateformat.parse(stringDate);
            }else {
                System.out.println("Enter valid date");
                return null;

            }
        }
        catch (ParseException parseException){
            parseException.printStackTrace();
        }

        return validDate;
    }

}
