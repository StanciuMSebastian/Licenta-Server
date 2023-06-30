package org.example;

import org.example.entities.Address;
import org.example.entities.Reports;
import org.example.entities.User;
import org.example.reportGenerator.PdfGenerator;
import org.jfree.chart.title.ShortTextTitle;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Vector;

public class ClientHandler extends Thread{
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private User user;

    private void sendErrorMessage(String message){
        try{
            out.writeUTF(message);
        }catch(Exception e){
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }
    }

    private void login(){
        try{
            String response;
            String username = in.readUTF();
            int passwordLength = in.readInt();
            byte[] password = new byte[passwordLength];

            if(in.read(password, 0, passwordLength) != passwordLength){
                System.out.println("Password read incorrectly");
            }


            User u = Main.findUserByUsername(username);

            if(u == null){
                response = "Wrong username";
            }else{
                if(!u.checkPassword(new String(password, StandardCharsets.UTF_8))){
                    response = "Wrong password";
                }else{
                    this.user = u;
                    response = "Accept " + u.getId() + " " + u.getRole() + " ";
                }
            }

            out.writeUTF(response);
            if(response.contains("Accept")){
                ServerLog.write("Client login: " + username + " ; " + socket.getInetAddress());
            }else{
                ServerLog.write("Failed Client login: " + username + " ; " + socket.getInetAddress());
            }
        }catch(Exception e){
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }
    }

    private void register(){
        try{
            String response = "ok";
            String username = in.readUTF();
            String email = in.readUTF();
            String role = in.readUTF();
            int passwordLength = in.readInt();
            byte[] password = new byte[passwordLength];

            if(in.read(password, 0, passwordLength) != passwordLength){
                System.out.println("Password read incorrectly");
            }

            if(Main.findUserByEmail(email) != null)
                response = "Email already used";

            if(Main.findUserByUsername(username) != null)
                response = "Username already used";

            if(!role.equals("Tester") && !role.equals("Client"))
                response = "Wrong associated role";

            if(response.equals("ok")){
                if(DatabaseConnector.insertUser(username, new String(password, StandardCharsets.UTF_8), email, role)){
                    User newUser = Main.findUserByUsername(username);

                    this.user = newUser;
                    response = "Accept " + newUser.getId();
                }else{
                    response = "Database error";
                }
            }

            out.writeUTF(response);
            if(response.contains("Accept")){
                ServerLog.write("Client register: " + username + " ; " + socket.getInetAddress());
            }else{
                ServerLog.write("Failed Client register: " + username + " ; " + socket.getInetAddress());
            }
        }catch(Exception e){
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }
    }

    private boolean checkConnection(String addressIp){
        try{
            HttpURLConnection connection = (HttpURLConnection) new URL(addressIp).openConnection();
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();

            if(responseCode == 200)
                return true;
            else
                return false;
        }catch(Exception e){
            return false;
        }
    }

    private void insertAddress(){
        try{
            if(this.user.getRole().equals("Client")) {
                String addressName = in.readUTF();
                String addressIp = in.readUTF();
                String scanType = in.readUTF();
                boolean hasActiveScan = in.readBoolean();
                String response;

                if(checkConnection(addressIp)){
                    response = "ok";
                }else if(checkConnection("http://" + addressIp)){
                    response = "ok";
                    addressIp = "http://" + addressIp;
                }else if(checkConnection("https://" + addressIp)) {
                    response = "ok";
                    addressIp = "https://" + addressIp;
                }else
                    response = "Invalid ip";


                if (response.equals("ok")) {
                    int addressId = DatabaseConnector.insertAddress(user.getId(), addressIp, addressName, scanType);
                    if (addressId != -1) {
                        response = "Accepted " + addressId;
                        Address newAddress = new Address(addressId, addressIp, addressName, scanType, null, this.user);
                        Main.addAddress(newAddress);

                        out.writeUTF(response);
                        ServerLog.write("Client " + this.user.getUsername() + " added a new address");

                        if (scanType.equals("Automatic") || scanType.equals("Hybrid")) {
                            AddressScanner addressScanner = new AddressScanner(this.in, this.out);
                            String scanDepth = "Passive";

                            if(hasActiveScan)
                                scanDepth = "Active";

                            String reportJson = addressScanner.startScan(scanDepth, addressIp);


                            if (reportJson != null) {
                                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMyyHHmmss");
                                LocalDateTime now = LocalDateTime.now();

                                String reportName = "Reports/" + user.getUsername() + "_" + dtf.format(now) + ".pdf";
                                PdfGenerator pdfGenerator = new PdfGenerator();
                                if(pdfGenerator.generateReport(reportJson, reportName)){
                                    int reportId = DatabaseConnector.addAddressReport(reportName, addressId);
                                    Main.addReports(new Reports(reportId,reportName,"Automatic",  newAddress));
                                    newAddress.doneAutomaticScan();
                                }
                            }

                            out.writeUTF("Done");
                            out.writeInt(100);
                        }
                    }
                }else
                    out.writeUTF(response);
            }else{
                int addressId = in.readInt();
                Main.assignTester(addressId, this.user.getId());
                out.writeUTF("Accepted");
            }

        }catch(Exception e){
            System.out.println("Exception: " + e);
            e.printStackTrace();
            sendErrorMessage("Invalid Ip Address");
        }
    }

    private void sendAddresses(){
        try{
            String role = in.readUTF();
            int userID = in.readInt();

            Vector<Address> userAddresses = new Vector<>();


            if(role.equals("Tester"))
                userAddresses = Main.getTesterAddresses(userID);
            else if(role.equals("Client"))
                userAddresses = Main.getClientAddresses(userID);

            for(Address a : userAddresses){
                // Sending a true boolean while there are still addresses to be sent
                out.writeBoolean(true);

                out.writeUTF(a.getName());
                out.writeUTF(a.getIp());
                out.writeUTF(a.getScanType());
                out.writeUTF(a.getClient().getUsername());
                out.writeInt(a.getId());
                out.writeDouble(a.getRating());
                out.writeBoolean(a.isCompletelyScanned());
                out.writeBoolean(a.isAutomaticScanComplete());
                out.writeBoolean(a.isManualScanComplete());

                if(a.getTester() != null)
                    out.writeUTF(a.getTester().getUsername());
                else
                    out.writeUTF("");
            }

            // Sending a false boolean when there are no more addresses to send
            out.writeBoolean(false);

            ServerLog.write("Client " + Main.findUserById(userID).getUsername() + " requested his addresses\n");

        }catch(Exception e){
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }
    }

    private void deleteAddress(){
        try{
            out.flush();

            int clientId = in.readInt();
            int addressId = in.readInt();

            Address a = Main.findAddressById(addressId);

            User client = Main.findUserById(clientId);

            if(client == null){
                out.writeUTF("User not found");
                return;
            }

            if(a == null){
                out.writeUTF("Address not found");
                return;
            }

            if(DatabaseConnector.deleteAddress(a, clientId)){
                out.writeUTF("ok");
                Main.deleteAddress(a);
                ServerLog.write("User " + client.getUsername() + " deleted an address");
            }else
                out.writeUTF("Database error");
        }catch(Exception e){
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }
    }

    private void downloadReport(){
        try{
            int clientId = in.readInt();
            int addressId = in.readInt();
            String reportType = in.readUTF();
            User user = Main.findUserById(clientId);
            Address address = Main.findAddressById(addressId);
            Reports report = Main.findReportByAddress(address, reportType);

            out.flush();

            if(user == null || address == null){
                out.writeUTF("Error");
                return;
            }

            if(report == null){
                out.writeUTF("No report found");
                return;
            }

            File file = new File(report.getFilename());

            if(!file.exists()){
                out.writeUTF("No report found");
                return;
            }

            out.writeUTF("ok");

            FileInputStream fStream = new FileInputStream(file);

            Long fileSize = file.length();

            out.writeUTF(file.getName());
            out.writeLong(file.length());

            int bytes = 0;
            byte[] buffer = new byte[4096];

            while((bytes = fStream.read(buffer)) != -1){
                out.flush();
                out.write(buffer, 0, bytes);
            }

            fStream.close();
        }catch(Exception e){
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }
    }

    private void uploadReport(){
        try{
            String filename = in.readUTF();
            int addressId = in.readInt();
            int uploaderId = in.readInt();
            long fileLength = in.readLong();

            Address address = Main.findAddressById(addressId);

            if(address == null){
                out.writeUTF("Could not find address");
                return;
            }

            User uploader = Main.findUserById(uploaderId);

            if(uploader == null){
                out.writeUTF("Could not find user");
                return;
            }

            String filePath = "Reports/Uploads/" + uploader.getUsername() + "/" + filename;
            Path directory = Paths.get("Reports/Uploads/" + uploader.getUsername());

            if(!Files.exists(directory))
                Files.createDirectories(directory);

            ServerLog.write("User " + uploader.getUsername() + " uploaded file " + filePath);

            File newReport = new File(filePath);

            if(!newReport.createNewFile()){
                out.writeUTF("File already exists");
                return;
            }else{
                out.writeUTF("ok");
            }

            FileOutputStream fOut = new FileOutputStream(newReport);


            byte[] buffer = new byte[4096];
            int bytesRead = 0;

            while(fileLength > 0 &&(bytesRead = in.read(buffer, 0, (int) Math.min(buffer.length, fileLength))) != -1){
                fOut.write(buffer, 0, bytesRead);
                fileLength -= bytesRead;
            }

            fOut.close();

            int reportId = DatabaseConnector.uploadReport(addressId, filePath);
            Main.addReports(new Reports(reportId, filePath, "Manual", address));
            address.doneManualScan();
        }catch(Exception e){
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }
    }

    private void getUntestedAddresses() {
        try{
            Vector<Address> addresses = Main.getUntestedAddresses();

            for(Address a : addresses){
                // Sending a true boolean while there are still addresses to be sent
                out.writeBoolean(true);

                out.writeUTF(a.getClient().getUsername());
                out.writeUTF(a.getName());
                out.writeUTF(a.getIp());
                out.writeUTF(a.getScanType());
                out.writeUTF(a.getClient().getUsername());
                out.writeInt(a.getId());

                out.writeBoolean(a.isCompletelyScanned());
                out.writeBoolean(a.isAutomaticScanComplete());
                out.writeBoolean(a.isManualScanComplete());
            }

            // Sending a false boolean when there are no more addresses to send
            out.writeBoolean(false);

            ServerLog.write("User " + this.user.getUsername() + " requested all untested addresses");
        }catch(Exception e){
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }
    }


    @Override
    public void run(){
        System.out.println("Client connected");
        ServerLog.write("Client connected; " + socket.getInetAddress());


        while(true){
            try{
                //System.out.println(in.available());

                //in.reset();
                String command = in.readUTF();


                switch (command) {
                    case "login" -> {
                        System.out.println("Client login");
                        login();
                    }
                    case "register" ->{
                        System.out.println("Client register");
                        register();
                    }
                    case "File Upload" ->{
                        uploadReport();
                    }
                    case "Add address" ->{
                        insertAddress();
                    }
                    case "Init Addresses"->{
                        sendAddresses();
                    }
                    case "Delete address"->{
                        deleteAddress();
                    }
                    case "exit" -> {
                        ServerLog.write("Client " + this.user.getUsername() + " disconnected");
                        System.out.println("Client diconnected");
                        socket.close();
                        return;
                    }
                    case "Download report" ->{
                        downloadReport();
                    }
                    case "Get Untested Addresses" ->{
                        getUntestedAddresses();
                    }
                    case "User info" ->{
                        getUserInfo();
                    }
                    case "Address Update" -> {
                        updateAddress();
                    }
                    case "Give User Rating" -> {
                        giveUserRating();
                    }
                    default -> {
                        ServerLog.write("Something went wrong");
                        System.out.println("Something went wrong");
                    }
                }
            }catch(Exception e){
                    System.out.println("Exception: " + e);
                    e.printStackTrace();

                    return;
                }
        }
    }

    private void giveUserRating() {
        try{
            int addressId = in.readInt();
            double rating = in.readDouble();

            Address address = Main.findAddressById(addressId);

            if(address == null){
                out.writeUTF("Could not find address");
                return;
            }

            if(!DatabaseConnector.giveRating(address.getId(), rating)){
                out.writeUTF("Database Error");
                return;
            }

            Main.setRating(address, rating);

            out.writeUTF("ok");
        }catch(Exception e){
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }
    }

    private void updateAddress() {
        try{
            int userAddressList = in.readInt();
            int doneAddresses = Main.getDoneAddresses(this.user);

            if(doneAddresses == userAddressList){
                out.writeUTF("ok");
            }else{
                out.writeUTF("Need Update");
            }
        }catch(Exception e){
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }
    }

    private void getUserInfo() {
        try{
            String userInfo = "";
            String username = in.readUTF();
            int clientId = in.readInt();

            User tester = Main.findUserByUsername(username);
            User client = Main.findUserById(clientId);

            if(tester == null){
                out.writeUTF("Error: tester not found");
                return;
            }

            if(client == null){
                out.writeUTF("Error: invalid user");
                return;
            }

            userInfo = tester.toString();

            userInfo += "\nRating: " + Main.getUserRating(tester.getId()) +
            "\nVotes: " + Main.getUserRatingCount(tester.getId());
            out.writeUTF(userInfo);
        }catch(Exception e){
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }
    }

    public ClientHandler(Socket clientSocket, DataInputStream input, DataOutputStream output){
        this.socket = clientSocket;
        this.in = input;
        this.out = output;
    }

    public ClientHandler(){}
}
