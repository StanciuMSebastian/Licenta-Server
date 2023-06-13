package org.example;

import java.util.Vector;

public class Address {
    public static Vector<Address> addressList = new Vector<>();
    private String ip;
    private String name;
    private String scanType;

    private int id;
    private User tester, client;
    private boolean areScansComplete, isAutomaticScanComplete, isManualScanComplete;

    public String getIp() {
        return ip;
    }

    public String getName() {
        return name;
    }

    public String getScanType() {
        return scanType;
    }

    public User getTester() {
        return this.tester;
    }

    public User getClient() {
        return this.client;
    }

    public boolean isCompletelyScanned() {
        return areScansComplete;
    }

    public boolean isAutomaticScanComplete(){
        return this.isAutomaticScanComplete;
    }

    public boolean isManualScanComplete(){
        return this.isManualScanComplete;
    }

    public void doneAutomaticScan(){
        this.isAutomaticScanComplete = true;

        if(this.scanType.equals("Automatic") || this.isManualScanComplete)
            this.areScansComplete = true;
    }

    public void doneManualScan(){
        this.isManualScanComplete = true;

        if(this.scanType.equals("Manual") || this.isAutomaticScanComplete)
            this.areScansComplete = true;
    }
    public int getId() {
        return id;
    }

    public void setTester(User tester) {
        this.tester = tester;
    }


    public Address(int id, String ip, String name, String scanType, User tester, User client) {
        this.id = id;
        this.ip = ip;
        this.name = name;
        this.scanType = scanType;
        this.tester = tester;
        this.client = client;
        areScansComplete = false;
        isAutomaticScanComplete = false;
        isManualScanComplete = false;
    }

    public Address(){

    }
}
