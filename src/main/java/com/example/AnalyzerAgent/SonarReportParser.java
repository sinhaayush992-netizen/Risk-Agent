package com.example.AnalyzerAgent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
 
import java.net.URL;

import javax.management.RuntimeErrorException;
 
public class SonarReportParser {
 
    // public static void parse(){
 
    //     try{
 
    //         String projectKey=
    //         System.getenv("SONAR_PROJECT_KEY");

    //         if(projectKey==null || projectKey.isEmpty()){
    //             System.out.println("Project Key Misssing");
    //             throw new RuntimeErrorException(null, "SONAR_PROJECT_KEY is MISSing");
    //         }
 
    //         String api="https://sonarcloud.io/api/issues/search?componentKeys=sinhaayush992-netizen_DevSecOps-Agent&types=VULNERABILITY&statuses=OPEN&severities=BLOCKER,CRITICAL,MAJOR";
    //        // "https://sonarcloud.io/api/issues/search?componentKeys="+projectKey;
    //         System.out.println("SONAR API:"+api);
 
    //         ObjectMapper mapper=new ObjectMapper();
 
    //         JsonNode root=mapper.readTree(new URL(api));
 
    //         JsonNode issues=root.get("issues");
 
    //         if(issues==null) return;
 
    //         for(JsonNode issue:issues){
 
    //             String message=issue.get("message").asText();
    //             String severity=issue.get("severity").asText();
 
    //             RiskAnalysis risk=
    //             AIRiskAnalyzer.analyze("SONAR",message,severity,"code");
    //             Thread.sleep(10000);
                
    //             String safeMessage = message.replaceAll("[^a-zA-Z0-9\\s]", "_"); // replace special chars with underscore
    //             String summary = "[SONAR] " + safeMessage;
    //            // String summary="[SONAR] "+message;
 
    //             JiraService.createTicketIfNeeded(summary,risk);
 
    //         }
 
    //     }catch(Exception e){
    //         e.printStackTrace();
    //     }
 
    // }

    public static void parse() {
        try {
            String projectKey = System.getenv("SONAR_PROJECT_KEY");
            if (projectKey == null || projectKey.isEmpty()) {
                System.out.println("Project Key Missing");
                throw new RuntimeException("SONAR_PROJECT_KEY is missing");
            }

            // Sonar API URL - filter only OPEN vulnerabilities
            String api = "https://sonarcloud.io/api/issues/search?componentKeys=" + projectKey
                    + "&types=VULNERABILITY&statuses=OPEN&severities=BLOCKER,CRITICAL,MAJOR";
            System.out.println("SONAR API: " + api);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new URL(api));
            JsonNode issues = root.get("issues");

            if (issues == null || !issues.isArray() || issues.size() == 0) {
                System.out.println("No issues found.");
                return;
            }

            for (JsonNode issue : issues) {
                String issueKey = issue.get("key").asText();
                String message = issue.get("message").asText();
                String severity = issue.get("severity").asText();
                String component = issue.get("component").asText().split(":")[1]; // file path

                // Call AI Analyzer
                RiskAnalysis risk = AIRiskAnalyzer.analyze("SONAR", message, severity, component);

                if (risk == null) {
                    System.out.println("AI analysis failed for issue " + issueKey);
                    continue;
                }

                // Construct Jira summary using Sonar issue key to prevent duplicates
                String safeMessage = message.replaceAll("[^a-zA-Z0-9\\s]", "_");
                String summary = "[SONAR] " + issueKey + " | " + component + " | " + safeMessage;

                JiraService.createTicketIfNeeded(summary, risk);

                // Avoid spamming AI/API
                Thread.sleep(5000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
}