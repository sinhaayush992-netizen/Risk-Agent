package com.example.AnalyzerAgent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
 
import java.net.URL;

import javax.management.RuntimeErrorException;
 
public class SonarReportParser {
 
    public static void parse(){
 
        try{
 
            String projectKey=
            System.getenv("SONAR_PROJECT_KEY");

            if(projectKey==null || projectKey.isEmpty()){
                System.out.println("Project Key Misssing");
                throw new RuntimeErrorException(null, "SONAR_PROJECT_KEY is MISSing");
            }
 
            String api=
            "https://sonarcloud.io/api/issues/search?componentKeys="+projectKey;
 
            ObjectMapper mapper=new ObjectMapper();
 
            JsonNode root=
            mapper.readTree(new URL(api));
 
            JsonNode issues=root.get("issues");
 
            if(issues==null) return;
 
            for(JsonNode issue:issues){
 
                String message=issue.get("message").asText();
                String severity=issue.get("severity").asText();
 
                RiskAnalysis risk=
                AIRiskAnalyzer.analyze("SONAR",message,severity,"code");
 
                String summary="[SONAR] "+message;
 
                JiraService.createTicketIfNeeded(summary,risk);
 
            }
 
        }catch(Exception e){
            e.printStackTrace();
        }
 
    }
 
}