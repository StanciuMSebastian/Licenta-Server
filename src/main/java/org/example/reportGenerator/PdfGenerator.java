package org.example.reportGenerator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class PdfGenerator {

    private DataInputStream input;
    private DataOutputStream output;
    private String generatedDate, addressName, addressHost;

    private ArrayList<Vulnerabilities> loadData(String jsonContent) throws IOException {
        ArrayList<Vulnerabilities> vulnList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(jsonContent);
        JsonNode alertsNode = jsonNode.get("site").get(0).get("alerts");

        this.generatedDate = jsonNode.get("@generated").toString().replaceAll("\"", "");
        this.addressName = jsonNode.get("site").get(0).get("@name").toString().replaceAll("\"", "");
        this.addressHost = jsonNode.get("site").get(0).get("@host").toString().replaceAll("\"", "");


        for(JsonNode alertChildNode : alertsNode){
            Vulnerabilities newVulnerability = new Vulnerabilities(alertChildNode.get("name").toString().replaceAll("\"", ""),
                    alertChildNode.get("desc").toString().replaceAll("<p>", "\n").replaceAll("</p>", "").replaceAll("\"", ""),
                    alertChildNode.get("riskdesc").toString().replaceAll(" \\(.*?\\)", "").replaceAll("\"", ""),
                    alertChildNode.get("solution").toString().replaceAll("<p>", "\n").replaceAll("</p>", "").replaceAll("\"", ""),
                    alertChildNode.get("reference").toString().replaceAll("<p>", "\n").replaceAll("</p>", "").replaceAll("\"", ""),
                    Integer.parseInt(alertChildNode.get("pluginid").toString().replaceAll("\"", "")),
                    Integer.parseInt(alertChildNode.get("riskcode").toString().replaceAll("\"", "")),
                    Integer.parseInt(alertChildNode.get("confidence").toString().replaceAll("\"", "")),
                    Integer.parseInt(alertChildNode.get("count").toString().replaceAll("\"", "")));


            JsonNode instancesJsonNode = alertChildNode.get("instances");
            ArrayList<Instance> instanceArray = new ArrayList<>();

            for(JsonNode instanceChildNode : instancesJsonNode){
                instanceArray.add(new Instance(instanceChildNode.get("uri").toString(),
                        instanceChildNode.get("method").toString(),
                        instanceChildNode.get("param").toString(),
                        instanceChildNode.get("attack").toString(),
                        instanceChildNode.get("evidence").toString(),
                        instanceChildNode.get("otherinfo").toString()));
            }
            newVulnerability.addInstance(instanceArray);
            vulnList.add(newVulnerability);
        }

        return vulnList;
    }

    public boolean generateReport(String jsonContent, String outputPath){
        try{
            String piechartJRXMLPath = "./src/main/resources/PiechartReport.jrxml";
            String piechartJasperPath = "./src/main/resources/PiechartReport.jasper";
            String mainJRXMLPath = "./src/main/resources/Vulnerability_Report.jrxml";

            ArrayList<PiechartData> piechartData = new ArrayList<>();
            ArrayList<Vulnerabilities> vulnList = loadData(jsonContent);

            vulnList.sort(new Comparator<Vulnerabilities>() {
                @Override
                public int compare(Vulnerabilities o1, Vulnerabilities o2) {
                    return o2.getRiskCode().compareTo(o1.getRiskCode());
                }
            });

            for(Vulnerabilities v : vulnList){
                String vulName = v.getRiskDescription();
                boolean found = false;

                for(PiechartData p : piechartData)
                    if(p.getRiskName().equals(vulName)){
                        p.incrementWeight();
                        found = true;
                        break;
                    }

                if(!found)
                    piechartData.add(new PiechartData(vulName, 1));
            }

            JasperReport mainReport = JasperCompileManager.compileReport(mainJRXMLPath);
            JasperReport piechartSubreport = JasperCompileManager.compileReport(piechartJRXMLPath);

            JRDataSource dataSource = new JRBeanCollectionDataSource(vulnList);
            JRDataSource piechartDataSource = new JRBeanCollectionDataSource(piechartData);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("addressName", this.addressName);
            parameters.put("hostName", this.addressHost);
            parameters.put("scanDate", this.generatedDate);
            parameters.put("piechartSubreport", piechartSubreport);
            parameters.put("piechartSubreportDataset", piechartDataSource);


            JasperPrint reportPrint = JasperFillManager.fillReport(mainReport, parameters, dataSource);
            JasperExportManager.exportReportToPdfFile(reportPrint, outputPath);

            System.out.println("Report created");
            return true;
        } catch (Exception e) {
            System.err.println("Error generating the report: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }
}
