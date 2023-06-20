package org.example;

import org.zaproxy.clientapi.core.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AddressScanner {
    private static final int ZAP_PORT = 8081;
    private static final String ZAP_API_KEY = "rocqgfbt5tbgvto9sj0hat7pel";
    private static final String ZAP_ADDRESS = "localhost";
    private static final String TARGET = "http://192.168.112.132:8080/WebGoat";

    private static ClientApi api;

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
        System.out.println("URL exploring complete\nStarting passive scan");
        // Perform additional operations with the Ajax Spider results
        List<ApiResponse> ajaxSpiderResponse = ((ApiResponseList) api.ajaxSpider.results("0", "10")).getItems();

        return ajaxSpiderResponse;
    }

    public static String passiveScan(String targetAddress){
        if(api == null)
            api = new ClientApi(ZAP_ADDRESS, ZAP_PORT, ZAP_API_KEY);

        try {
            List<ApiResponse> ajaxSpiderResponse = spiderCrawl(targetAddress);
            int numberOfRecords;

            // Passive scan
            do{
                Thread.sleep(2000);
                api.pscan.recordsToScan();
                numberOfRecords = Integer.parseInt(((ApiResponseElement) api.pscan.recordsToScan()).getValue());
                System.out.println("Number of records left for scanning : " + numberOfRecords);
            }while(numberOfRecords != 0);

            System.out.println("Passive Scan completed");

            //return new String(api.core.htmlreport());
            return new String(api.core.jsonreport());

            // Print Passive scan results/alerts
            //System.out.println("Alerts:");
            //System.out.println(new String(api.core.xmlreport(), StandardCharsets.UTF_8));

            //BufferedWriter writer = new BufferedWriter(new FileWriter("/home/stanciul420/Desktop/passivescan.html"));
            //writer.write(new String(api.core.htmlreport()));
        }catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public static String activeScan(String targetAddress){
        if(api == null)
            api = new ClientApi(ZAP_ADDRESS, ZAP_PORT, ZAP_API_KEY);

        try {
            List<ApiResponse> ajaxSpiderResponse = spiderCrawl(targetAddress);

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
                System.out.println("Active Scan progress : " + progress + "%");
            } while (progress < 100);

            System.out.println("Active Scan complete");
            // Print vulnerabilities found by the scanning
            System.out.println("Alerts:");
            System.out.println(new String(api.core.xmlreport(), StandardCharsets.UTF_8));

            return new String(api.core.jsonreport());
        }catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            e.printStackTrace();
        }

        return "";
    }

    public static String startScan(String targetAddress, String clientUsername){
        try{
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMyyHHmmss");
            LocalDateTime now = LocalDateTime.now();

            String scanResult = passiveScan(targetAddress);
//            String reportName = "Reports/" + clientUsername + "_" + dtf.format(now) + ".html";
//            File reportFile = new File(reportName);
//            reportFile.createNewFile();
//
//            FileOutputStream writer = new FileOutputStream(reportFile);
//            writer.write(scanResult.getBytes());

            return scanResult;
        }catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
