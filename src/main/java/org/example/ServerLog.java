package org.example;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerLog {
    private static File logFile;
    private static FileOutputStream oFile;

    public static void write(String message){
        try{
            if(logFile == null){
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd_MM_yy");
                LocalDateTime now = LocalDateTime.now();

                logFile = new File("Logs/Log_" + dtf.format(now));

                oFile = new FileOutputStream(logFile, true);
            }

            message = message.concat("\n");
            oFile.write(message.getBytes());
        }catch(Exception e){
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }
    }
}
