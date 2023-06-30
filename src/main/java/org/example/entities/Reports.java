package org.example.entities;

public class Reports {
    private int id;
    private String filename, type;
    private Address address;

    public int getId(){
        return this.getId();
    }

    public String getFilename() {
        return filename;
    }

    public String getType(){
        return this.type;
    }

    public Address getAddress() {
        return address;
    }



    public Reports(int id, String filename, String type, Address address) {
        this.id = id;
        this.filename = filename;
        this.type = type;
        this.address = address;
    }
}
