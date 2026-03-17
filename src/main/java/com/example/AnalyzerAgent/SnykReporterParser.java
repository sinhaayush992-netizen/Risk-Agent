package com.example.AnalyzerAgent;

 
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
 
import java.io.File;
 
public class SnykReporterParser {
 
    public static void parse(){
 
        try{
 
            ObjectMapper mapper=new ObjectMapper();
 
            JsonNode root =
            mapper.readTree(new File("snyk-report.json"));
 
            JsonNode vulns=root.get("vulnerabilities");
 
            if(vulns==null) return;
 
            for(JsonNode v:vulns){
 
                String title=v.get("title").asText();
                String severity=v.get("severity").asText();
                String pkg=v.get("packageName").asText();
 
                RiskAnalysis risk =
                AIRiskAnalyzer.analyze("SNYK",title,severity,pkg);
 
                String summary="[SNYK] "+title;
 
                JiraService.createTicketIfNeeded(summary,risk);
 
            }
 
        }catch(Exception e){
            e.printStackTrace();
        }
 
    }
 
}
 