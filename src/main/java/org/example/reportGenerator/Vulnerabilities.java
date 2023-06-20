package org.example.reportGenerator;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import java.util.ArrayList;

public class Vulnerabilities implements JRDataSource {
    private final  String name, description, riskDescription, solution, reference;
    private String instances;
    private final  Integer id, riskCode, confidence, count;
    private final ArrayList<Instance> instanceList;

    public void addInstance(Instance newInstance){
        this.instanceList.add(newInstance);

        instances = instances.concat(" URL: " + newInstance.getUrl());
        instances = instances.concat("\n\tAttack: " + newInstance.getAttack());
        instances = instances.concat("\n\tMethod: " + newInstance.getMethod());
        instances = instances.concat("\n\n");
    }

    public void addInstance(ArrayList<Instance> newInstance){
        this.instanceList.addAll(newInstance);

        int instanceCount = 0;

        for(Instance i : newInstance){
            instanceCount++;

            instances = instances.concat("URL: " + i.getUrl());
            instances = instances.concat("\nAttack: " + i.getAttack());
            instances = instances.concat("\nMethod: " + i.getMethod());
            instances = instances.concat("\n\n");

            if(instanceCount >= 10)
                return;
        }
    }

    public String getInstances(){
        return this.instances;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getRiskDescription() {
        return riskDescription;
    }

    public String getSolution() {
        return solution;
    }

    public String getReference() {
        return reference;
    }

    public Integer getId() {
        return id;
    }

    public Integer getRiskCode() {
        return riskCode;
    }

    public Integer getConfidence() {
        return confidence;
    }

    public Integer getCount() {
        return count;
    }

    public ArrayList<Instance> getInstanceList() {
        return instanceList;
    }

    public Vulnerabilities(String name, String description, String riskDescription, String solution, String reference, Integer id, Integer riskCode, Integer confidence, Integer count) {
        this.name = name;
        this.description = description;
        this.riskDescription = riskDescription;
        this.solution = solution;
        this.reference = reference;
        this.id = id;
        this.riskCode = riskCode;
        this.confidence = confidence;
        this.count = count;
        this.instances = "";

        this.instanceList = new ArrayList<>();
    }
    public Vulnerabilities(){
        this.name = "";
        this.description = "";
        this.riskDescription = "";
        this.solution = "";
        this.reference = "";
        this.id = -1;
        this.riskCode = -1;
        this.confidence = -1;
        this.count = -1;
        this.instances = "";

        this.instanceList = new ArrayList<>();
    }

    @Override
    public boolean next() throws JRException {
        //return next != null;
        return false;
    }

    @Override
    public Object getFieldValue(JRField jrField) throws JRException {
        switch(jrField.getName()){
            case "name" -> {
                return this.name;
            }
            case "description" -> {
                return this.description;
            }
            case "riskDescription" -> {
                return this.riskDescription;
            }
            case "solution" -> {
                return this.solution;
            }
            case "reference" -> {
                return this.reference;
            }
            case "id" -> {
                return this.id;
            }
            case "riskCode" -> {
                return this.riskCode;
            }
            case "confidence" -> {
                return this.confidence;
            }
            case "count" -> {
                return this.count;
            }
            case "instances" ->{
                return this.instances;
            }
        }

        return null;
    }
}
