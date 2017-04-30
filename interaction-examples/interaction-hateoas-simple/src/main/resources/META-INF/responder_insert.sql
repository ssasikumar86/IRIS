#
# Populate your responder inmemory database with some test values
#
# For example:
INSERT INTO `Profile`(`userID` , `name` , `DOB` , `location` , `pictureTaken`) VALUES('aphethean' , 'Aaron Phethean' , '1824-08-21 00:00:00' , '-27.4667,153.0333' , '2012-04-16 15:23:00');	
INSERT INTO `Preferences`(`userID` , `currency` , `language`) VALUES('aphethean' , 'GBP' , 'EN');	
INSERT INTO `Profile`(`userID` , `name` , `DOB` , `location` , `pictureTaken`) VALUES('test+User 1\2/3''4' , 'Test User' , '1999-08-21 00:00:00' , '-27.4667,153.0333' , '2012-04-16 15:23:00');
INSERT INTO `Preferences`(`userID` , `currency` , `language`) VALUES('test+User 1\2/3''4' , 'USD' , 'EN');

#INSERT INTO `ID`(`DomainObjectName`, `LastId`) VALUES('NOTE', 1);

INSERT INTO `Note`(`NoteID`,`Body`) VALUES(1,'Beverages');
INSERT INTO `Note`(`NoteID`,`Body`) VALUES(2,'Condiments');
INSERT INTO `Note`(`NoteID`,`Body`) VALUES(3,'Confections');
INSERT INTO `Note`(`NoteID`,`Body`) VALUES(4,'Dairy Products');
INSERT INTO `Note`(`NoteID`,`Body`) VALUES(5,'Grains/Cereals');
INSERT INTO `Note`(`NoteID`,`Body`) VALUES(6,'Meat/Poultry');
INSERT INTO `Note`(`NoteID`,`Body`) VALUES(7,'Produce');
INSERT INTO `Note`(`NoteID`,`Body`) VALUES(8,'Seafood');
INSERT INTO `Note`(`NoteID`,`Body`) VALUES(9,'IMPORTANT');
