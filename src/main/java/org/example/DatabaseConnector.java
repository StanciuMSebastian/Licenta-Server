package org.example;

import org.example.entities.Address;
import org.example.entities.Reports;
import org.example.entities.User;

import java.sql.*;
import java.util.Vector;

public class DatabaseConnector {

    private static Connection con;

    public static void initDatabase(){
        try{
            String url = "jdbc:mysql://localhost:3306/Licenta";
            String username = "Client";
            String password = "strongestPassword";

            con = DriverManager.getConnection(url, username, password);
        }catch(Exception e){
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }
    }

    public static void assignTester(int addressID, int testerID){
        try{
            String query = "UPDATE Licenta.IpAddresses SET TesterID = ? WHERE IpAddressID = ?;";
            PreparedStatement updateStatement = con.prepareStatement(query);

            updateStatement.setInt(1, testerID);
            updateStatement.setInt(2, addressID);

            updateStatement.executeUpdate();
        }catch(Exception e){
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }
    }

    public static int addAddressReport(String reportName, int addressId){
        try{
            String insertQuery = "INSERT INTO Licenta.Reports(AddressID, FileName, Type) VALUES (?, ?, ?);";
            String updateQuery = "UPDATE Licenta.IpAddresses SET AutomaticScanStatus = 'Done' WHERE IpAddressID = ?;";

            PreparedStatement insertStatement = con.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
            PreparedStatement updateStatement = con.prepareStatement(updateQuery);

            insertStatement.setInt(1, addressId);
            insertStatement.setString(2, reportName);
            insertStatement.setString(3, "Automatic");

            updateStatement.setInt(1, addressId);

            insertStatement.execute();
            updateStatement.executeUpdate();

            ResultSet rs = insertStatement.getGeneratedKeys();
            rs.next();

            return rs.getInt(1);
        }catch(Exception e){
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }

        return -1;
    }

    public static Vector<User> initUsers(){
        try{
            Vector<User> returnList = new Vector<>();

            String query = "SELECT * FROM Licenta.Users;";
            Statement statement = con.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while(resultSet.next()){
                returnList.add(new User(
                        resultSet.getString("Username"),
                        resultSet.getString("Role"),
                        resultSet.getString("Email"),
                        resultSet.getString("Password"),
                        resultSet.getInt("UserID")));
            }

            return returnList;
        }catch(Exception e){
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }

        return null;
    }

    public static Vector<Address> initAddress() throws SQLException {
        String query = "SELECT * FROM Licenta.IpAddresses;";
        Statement selectStatement = con.createStatement();
        ResultSet selectResult = selectStatement.executeQuery(query);
        Vector<Address> returnVector = new Vector<>();

        while(selectResult.next()){
            Address newAddress = new Address(selectResult.getInt("IpAddressId"),
                    selectResult.getString("Address"),
                    selectResult.getString("Name"),
                    selectResult.getString("ScanType"),
                    Main.findUserById(selectResult.getInt("TesterID")),
                    Main.findUserById(selectResult.getInt("ClientID")));

            if(selectResult.getString("AutomaticScanStatus").equals("Done"))
                newAddress.doneAutomaticScan();

            if(selectResult.getString("ManualScanStatus").equals("Done"))
                newAddress.doneManualScan();

            newAddress.setRating(selectResult.getDouble("Rating"));
            returnVector.add(newAddress);
        }

        return returnVector;
    }

    public static Vector<Reports> initReports() {
        try{
            String query = "SELECT * FROM Licenta.Reports;";
            Statement selectStatement = con.createStatement();
            ResultSet selectResult = selectStatement.executeQuery(query);
            Vector<Reports> returnVector = new Vector<>();

            while(selectResult.next()){
                returnVector.add(new Reports(selectResult.getInt("ReportId"),
                        selectResult.getString("FileName"),
                        selectResult.getString("Type"),
                        Main.findAddressById(selectResult.getInt("AddressId"))));
            }

            return returnVector;
        }catch(Exception e){
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }

        return null;
    }

    public static int uploadReport(int addressId, String filename){
        try{
            String insertQuery = "INSERT INTO Licenta.Reports(AddressID, FileName, Type) VALUES (?, ?, ?);";
            String updateQuery = "UPDATE Licenta.IpAddresses SET ManualScanStatus = 'Done' WHERE IpAddressID = ?;";

            PreparedStatement insertStatement = con.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
            PreparedStatement updateStatement = con.prepareStatement(updateQuery);

            insertStatement.setInt(1, addressId);
            insertStatement.setString(2, filename);
            insertStatement.setString(3, "Manual");

            updateStatement.setInt(1, addressId);

            insertStatement.execute();
            updateStatement.executeUpdate();

            ResultSet rs = insertStatement.getGeneratedKeys();
            rs.next();

            return rs.getInt(1);
        }catch(Exception e){
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }
        return 0;
    }

    public static boolean deleteAddress(Address address, int clientId){
        try{
            String deleteReportQuery = "DELETE FROM Licenta.Reports WHERE AddressId = ?";
            String deleteAddressQuery = "DELETE FROM Licenta.IpAddresses WHERE IpAddressID = ? AND ClientID = ?;";

            PreparedStatement deleteReportStatement = con.prepareStatement(deleteReportQuery);
            PreparedStatement deleteAddressStatement = con.prepareStatement(deleteAddressQuery);

            deleteReportStatement.setInt(1, address.getId());

            deleteAddressStatement.setInt(1, address.getId());
            deleteAddressStatement.setInt(2, clientId);

            // System.out.println(insertStatement.toString());

            if(Main.findReportByAddress(address) != null)
                deleteReportStatement.execute();

            deleteAddressStatement.execute();

            return true;
        }catch(Exception e){
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }

        return false;
    }

    public static int insertAddress(int clientId, String addressIp, String addressName, String scanType){
        try{
            String query = "INSERT INTO Licenta.IpAddresses(Name, ClientID, Address, ScanType) VALUES (?, ?, ?, ?);";

            PreparedStatement insertStatement = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            insertStatement.setString(1, addressName);
            insertStatement.setInt(2, clientId);
            insertStatement.setString(3, addressIp);
            insertStatement.setString(4, scanType);

            System.out.println(insertStatement);

            int affectedRows = insertStatement.executeUpdate();

            if(affectedRows == 0)
                return -1;

            ResultSet rs = insertStatement.getGeneratedKeys();
            rs.next();

            return rs.getInt(1);
        }catch(Exception e){
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }

        return -1;
    }

    public static boolean giveRating(int addressId, double rating){
        try{
            String query = "UPDATE Licenta.IpAddresses SET Rating = ? WHERE IpAddressId = ?";


            PreparedStatement updateStatement = con.prepareStatement(query);

            updateStatement.setDouble(1, rating);
            updateStatement.setInt(2, addressId);

            updateStatement.execute();

            return true;
        }catch(Exception e){
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }

        return false;
    }

    public static boolean insertUser(String username, String password, String email, String role){
        try{
            String query = "INSERT INTO Licenta.Users(Username, password, Role, Email) VALUES (?, ?, ?, ?);";


            PreparedStatement insertStatement = con.prepareStatement(query);

            insertStatement.setString(1, username);
            insertStatement.setString(2, password);
            insertStatement.setString(3, role);
            insertStatement.setString(4, email);

            System.out.println(insertStatement.toString());

            insertStatement.execute();

            query = "SELECT * FROM Licenta.Users WHERE Username = '" + username + "';";
            Statement selectStatement = con.createStatement();
            ResultSet selectResult = selectStatement.executeQuery(query);

            selectResult.next();


            Main.addUser(new User(selectResult.getString("Username"),
                    selectResult.getString("Role"),
                    selectResult.getString("Email"),
                    selectResult.getString("password"),
                    selectResult.getInt("UserId")));

            return true;
        }catch(Exception e){
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }

        return false;
    }
}
