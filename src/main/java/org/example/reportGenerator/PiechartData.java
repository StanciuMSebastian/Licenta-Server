package org.example.reportGenerator;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

public class PiechartData implements JRDataSource {
    private Integer riskWeight;
    private String riskName;

    public void incrementWeight(){
        this.riskWeight ++;
    }

    public Integer getRiskWeight(){
        return this.riskWeight;
    }
    public String getRiskName(){
        return this.riskName;
    }

    public PiechartData(String name, Integer weight){
        this.riskName = name;
        this.riskWeight = weight;
    }


    @Override
    public boolean next() throws JRException {
        return false;
    }

    @Override
    public Object getFieldValue(JRField jrField) throws JRException {
        switch(jrField.getName()){
            case "riskWeight" -> {
                return this.riskWeight;
            }
            case "riskName" -> {
                return this.riskName;
            }
        }

        return null;
    }
}
