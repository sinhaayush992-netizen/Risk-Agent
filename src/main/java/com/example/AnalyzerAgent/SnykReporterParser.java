package com.example.AnalyzerAgent;

 
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
 
import java.io.File;
 
public class SnykReporterParser {
 
    public static void parse(){

        System.out.println("Reading Snyk Report...");
 
        try{
 
            ObjectMapper mapper=new ObjectMapper();
 
            JsonNode root =
            mapper.readTree(new File("snyk-report.json"));
 
            JsonNode vulns=root.get("vulnerabilities");
 
            if(vulns==null) return;
            int count =3;
            for(JsonNode v:vulns){
                if(count>3)
                    break;
                System.out.println("Proccesing Report...");
 
 
                String title=v.get("title").asText();
                String severity=v.get("severity").asText();
                String pkg=v.get("packageName").asText();
 
                RiskAnalysis risk =
                AIRiskAnalyzer.analyze("SNYK",title,severity,pkg);
                Thread.sleep(10000);
 
                String summary="[SNYK] "+title;
 
                JiraService.createTicketIfNeeded(summary,risk);
                count++;
 
            }
 
        }catch(Exception e){
            e.printStackTrace();
        }
 
    }
 
}
 