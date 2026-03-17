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
 
            if(vulns==null) return;
            int count =0;
            for(JsonNode v:vulns){
                
                System.out.println("Proccesing Report...");

 
 
                String title=v.get("title").asText();
                String severity=v.get("severity").asText();
                String pkg=v.get("packageName").asText();

                if(severity.equalsIgnoreCase("critical")){
                    System.out.println("Low Severity");
                    continue;
                }
                if(count>3)
                    break;
 
                RiskAnalysis risk =
                AIRiskAnalyzer.analyze("SNYK",title,severity,pkg);
                Thread.sleep(10000);
                count++;
                String summary="[SNYK] "+title;
 
                JiraService.createTicketIfNeeded(summary,risk);
                
 
            }
 
        }catch(Exception e){
            e.printStackTrace();
        }
 
    }
 
}
 