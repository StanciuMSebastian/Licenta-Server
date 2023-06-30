package org.example;

import org.zaproxy.clientapi.core.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AddressScanner{
    private static final int ZAP_PORT = 8081;
    private static final String ZAP_API_KEY = "rocqgfbt5tbgvto9sj0hat7pel";
    private static final String ZAP_ADDRESS = "localhost";
    private static final String TARGET = "http://192.168.112.132:8080/WebGoat";

    private static ClientApi api;
    private DataInputStream in;
    private DataOutputStream out;


    private static List<ApiResponse> spiderCrawl(String target) throws ClientApiException, InterruptedException {
        // Start spidering the target
        System.out.println("Discovering URLs");
        ApiResponse resp = api.ajaxSpider.scan(target, null, null, null);
        String status;

        long startTime = System.currentTimeMillis();
        long timeout = TimeUnit.MINUTES.toMillis(2); // Two minutes in milliseconds
        // Loop until the ajax spider has finished or the timeout has exceeded
        while (true) {
            Thread.sleep(2000);
            status = (((ApiResponseElement) api.ajaxSpider.status()).getValue());
            System.out.println("Exploring status : " + status);
            if (!("stopped".equals(status)) || (System.currentTimeMillis() - startTime) < timeout) {
                break;
            }
        }
        System.out.println("URL exploring complete");
        // Perform additional operations with the Ajax Spider results
        List<ApiResponse> ajaxSpiderResponse = ((ApiResponseList) api.ajaxSpider.results("0", "10")).getItems();

        return ajaxSpiderResponse;
    }

    public String passiveScan(String targetAddress){
        if(api == null)
            api = new ClientApi(ZAP_ADDRESS, ZAP_PORT, ZAP_API_KEY);

        try {
            out.writeUTF("Passive Scanning target : " + targetAddress);

            out.writeUTF("Passive Scanning target : " + targetAddress);
            out.writeInt(0);

            List<ApiResponse> ajaxSpiderResponse = spiderCrawl(targetAddress);
            int numberOfRecords;



            do{
                Thread.sleep(2000);
                api.pscan.recordsToScan();
                numberOfRecords = Integer.parseInt(((ApiResponseElement) api.pscan.recordsToScan()).getValue());
                System.out.println("Number of records left for scanning : " + numberOfRecords);

                out.writeUTF("Number of records left for scanning : " + numberOfRecords);
                out.writeInt(50);
            }while(numberOfRecords != 0);

            System.out.println("Passive Scan completed");
            out.writeUTF("Generating Report");
            out.writeInt(100);

            return new String(api.core.jsonreport());

        }catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public String activeScan(String targetAddress){
        if(api == null)
            api = new ClientApi(ZAP_ADDRESS, ZAP_PORT, ZAP_API_KEY);

        try {
            out.writeUTF("Spider Crawling target : " + targetAddress);

            List<ApiResponse> ajaxSpiderResponse = spiderCrawl(targetAddress);

            out.writeUTF("Active Scanning target : " + targetAddress);
            out.writeInt(0);

            System.out.println("Active Scanning target : " + targetAddress);

            ApiResponse resp1 = api.ascan.scan(targetAddress, "True", "False", null, null, null);
            String scanid;
            int progress;

            // The scan now returns a scan id to support concurrent scanning
            scanid = ((ApiResponseElement) resp1).getValue();

            // Poll the status until it completes
            do {
                Thread.sleep(5000);
                progress =
                        Integer.parseInt(
                                ((ApiResponseElement) api.ascan.status(scanid)).getValue());
                out.writeUTF("Active Scan progress : " + progress + "%");
                out.writeInt(progress);

                System.out.println("Active Scan progress : " + progress + "%");
            } while (progress < 100);

            out.writeUTF("Done");
            out.writeInt(100);    // pentru sincronizarea cu aplicatia client

            System.out.println("Active Scan complete");

            return new String(api.core.jsonreport());
        }catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            e.printStackTrace();
        }

        return "";
    }

    public String startScan(String scanType, String targetAddress){
        try{
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMyyHHmmss");
            LocalDateTime now = LocalDateTime.now();
            String scanResult = "";

            if(scanType.equals("Active"))
                scanResult = activeScan(targetAddress);//dureaza 3:21 minute pentru scanare + generare raport
            else
                scanResult = passiveScan(targetAddress); //dureaza 37 de secunde

            out.flush();

            return scanResult;
        }catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


    public AddressScanner(DataInputStream in, DataOutputStream out){
        this.in = in;
        this.out = out;
    }

    public AddressScanner(){
    }
}
