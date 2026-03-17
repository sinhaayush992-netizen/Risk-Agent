package com.example.AnalyzerAgent;

 
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
 
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
 
public class SnykReporterParser {
 
    public static void parse(){

        System.out.println("Reading Snyk Report...");
 
        try{
 
            ObjectMapper mapper=new ObjectMapper();
 
            JsonNode root = mapper.readTree(new File("snyk-report.json"));
 
            JsonNode vulns=root.get("vulnerabilities");

            System.out.println(new String(Files.readAllBytes(Paths.get("snyk-report.json"))));

            if (vulns == null || !vulns.isArray() || vulns.size() == 0) {
                System.out.println("Snyk report is empty. Skipping.");
                return;
            }

            int count =0;
            int processedCount=0;
            for(JsonNode v:vulns){
       
                String title=v.get("title").asText();
                System.out.println("Proccesing Report..."+title);
                String severity = v.has("severityWithCritical") ? v.get("severityWithCritical").asText() : v.get("severity").asText();
                if (!severity.equalsIgnoreCase("critical")) {
                 System.out.println("Skipping non-critical vulnerability: " + severity);
                    continue;
                    }
                String pkg=v.get("packageName").asText();
                System.out.println("Count:"+count);
                if(count>1)
                    break;
 
                RiskAnalysis risk = AIRiskAnalyzer.analyze("SNYK",title,severity,pkg);
                Thread.sleep(2000);
                count++;
                 if (risk == null) {
                    System.out.println("Risk analysis returned null. Skipping ticket creation for: " + title);
                    continue;
                }
                String summary="[SNYK] "+title;
 
                JiraService.createTicketIfNeeded(summary,risk);

                 processedCount++;
                if (processedCount >= 2) break; // Optional: limit tickets per run

                // Small delay to avoid API rate limits
                Thread.sleep(2000);
                
 
            }
 
        }catch(Exception e){
            e.printStackTrace();
        }
 
    }
 
}
 