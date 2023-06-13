package org.example;

import javax.xml.crypto.Data;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;


public class Main {
    private static Vector<User> userVector;
    private static Vector<Address> addressVector;
    private static Vector<Reports> reportVector;

    public static void addUser(User newUser){
        userVector.add(newUser);
    }
    public static void deleteAddress(Address a){addressVector.remove(a);}

    public static Address findAddressById(int id){
        for(Address a : addressVector){
            if(a.getId() == id)
                return a;
        }

        return null;
    }

    public static boolean verifyAddress(int id, String addressName, String addressIp){
        Address a = findAddressById(id);
        if(a == null)
            return false;

        return (a.getName().equals(addressName) && a.getIp().equals(addressIp));
    }
    public static void addAddress(Address a){
        addressVector.add(a);
    }
    public static void addReports(Reports r){reportVector.add(r);}

    public static void assignTester(int addressID, int testerID){
        DatabaseConnector.assignTester(addressID, testerID);

        Address a = findAddressById(addressID);
        assert a != null;
        a.setTester(Main.findUserById(testerID));
    }

    public static Vector<Address> getUntestedAddresses(){
        Vector<Address> retVector = new Vector<>();

        for(Address a : addressVector){
            if(a.getTester() == null && (a.getScanType().equals("Manual") || a.getScanType().equals("Hybrid"))){
                retVector.add(a);
            }
        }

        return retVector;
    }
    public static Vector<Address> getTesterAddresses(int testerId){
        Vector<Address> retVector = new Vector<>();

        for(Address a : addressVector){
            if(a.getTester() != null && a.getTester().getId() == testerId){
                retVector.add(a);
            }
        }

        return retVector;
    }

    public static Vector<Address> getClientAddresses(int clientID){
        Vector<Address> retVector = new Vector<>();

        for(Address a : addressVector){
            if(a.getClient().getId() == clientID){
                retVector.add(a);
            }
        }

        return retVector;
    }

    public static User findUserById(int userID){
        for(User u : userVector)
            if(u.getId() == userID)
                return u;

        return null;
    }

    public static User findUserByUsername(String username){
        for(User u : userVector)
            if(u.getUsername().equals(username))
                return u;

        return null;
    }

    public static User findUserByEmail(String email){
        for(User u : userVector)
            if(u.getEmail().equals(email))
                return u;

        return null;
    }

    public static Reports findReportByAddress(Address a){
        for(Reports r : reportVector){
            if(r.getAddress().equals(a))
                return r;
        }

        return null;
    }

    public static Reports findReportByAddress(Address a, String type){
        for(Reports r : reportVector){
            if(r.getAddress().equals(a) && r.getType().equals(type))
                return r;
        }

        return null;
    }

    public static void main(String[] args){
        try{
            // AddressScanner.passiveScan("nothing");

            int port = 2000;
            ServerSocket server = new ServerSocket(port);

            DatabaseConnector.initDatabase();
            userVector = DatabaseConnector.initUsers();
            addressVector = DatabaseConnector.initAddress();
            reportVector = DatabaseConnector.initReports();

            while(true){
                Socket client = server.accept();

                //BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                ClientHandler clientHandler = new ClientHandler(client,
                        new DataInputStream(client.getInputStream()),
                        new DataOutputStream(client.getOutputStream()));
                clientHandler.start();
            }
        }catch(Exception e){
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }
    }
}