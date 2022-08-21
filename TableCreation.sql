-- Assign or create databse 
-- CREATE DATABASE IF NOT EXISTS mydb;
-- use mydb;

-- -----------------------------------------------------
-- Table personIdentity
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS personIdentity(
  idpersonIdentity INT NOT NULL AUTO_INCREMENT,
  personName VARCHAR(225) NULL,
  PRIMARY KEY (idpersonIdentity));

-- -----------------------------------------------------
-- Table personAttributes
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS personAttributes(
  idpersonAttributes INT NOT NULL AUTO_INCREMENT,
  birthLocation VARCHAR(225) NULL,
  deathLocation VARCHAR(225) NULL,
  gender VARCHAR(45) NULL,
  occupation VARCHAR(45) NULL,
  birthDate VARCHAR(45) NULL,
  deathDate VARCHAR(45) NULL,
  personIdentity_id INT NOT NULL,
  PRIMARY KEY (idpersonAttributes),
  FOREIGN KEY (personIdentity_id) REFERENCES personIdentity(idpersonIdentity));


-- -----------------------------------------------------
-- Table personNoteReference
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS personNoteReference(
  idpersonNote INT NOT NULL AUTO_INCREMENT,
  noteAndReference VARCHAR(500) NULL,
 personIdentity_id INT NOT NULL,
  PRIMARY KEY (idpersonNote),
    FOREIGN KEY (personIdentity_id) REFERENCES personIdentity (idpersonIdentity));

-- -----------------------------------------------------
-- Table personChild
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS personChild(
  personIdentity_idChild INT NOT NULL,
  parent_id INT NOT NULL,
  FOREIGN KEY (parent_id) REFERENCES personIdentity (idpersonIdentity));

-- -----------------------------------------------------
-- Table `personRelationship
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS personRelationship(
  personIdentity_idpersonIdentity INT NOT NULL,
  personIdentity_id2 INT NOT NULL,
  PRIMARY KEY (personIdentity_idpersonIdentity),
  FOREIGN KEY (personIdentity_idpersonIdentity) REFERENCES personIdentity(idpersonIdentity));
-- -----------------------------------------------------
-- Table mediaFile
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS mediaFile(
  idmediaFile INT NOT NULL AUTO_INCREMENT,
  fileLocation VARCHAR(225) NULL,
  PRIMARY KEY (idmediaFile));
-- -----------------------------------------------------
-- Table mediaAttribute
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS mediaAttribute(
  idmediaAttribute INT NOT NULL AUTO_INCREMENT,
  date date NULL,
  place VARCHAR(45) NULL,
  city VARCHAR(45) NULL,
  province VARCHAR(45) NULL,
  country VARCHAR(45) NULL,
  mediaFile_idmediaFile INT NOT NULL,
  PRIMARY KEY (idmediaAttribute),
  FOREIGN KEY (mediaFile_idmediaFile) REFERENCES mediaFile(idmediaFile));


-- -----------------------------------------------------
-- Table mediaPeople
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS mediaPeople (
  personIdentity_idpersonIdentity INT NOT NULL,
  mediaFile_idmediaFile INT NOT NULL,
  FOREIGN KEY (personIdentity_idpersonIdentity) REFERENCES personIdentity(idpersonIdentity),
  FOREIGN KEY (mediaFile_idmediaFile) REFERENCES mediaFile(idmediaFile));


-- -----------------------------------------------------
-- Table `mydb`.`mediaTag`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS mediaTag(
  idmediaTag INT NOT NULL AUTO_INCREMENT,
  tag VARCHAR(255) NULL,
  PRIMARY KEY (idmediaTag));


-- -----------------------------------------------------
-- Table tags
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS tags (
  mediaFile_idmediaFile INT NOT NULL,
  mediaTag_idmediaTag INT NOT NULL,
    FOREIGN KEY (mediaFile_idmediaFile) REFERENCES mediaFile(idmediaFile),
    FOREIGN KEY (mediaTag_idmediaTag) REFERENCES mediaTag(idmediaTag));

