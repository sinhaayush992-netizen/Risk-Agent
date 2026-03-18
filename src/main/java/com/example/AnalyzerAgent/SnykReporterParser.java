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
                double cvssScore = v.has("cvssScore") ? v.get("cvssScore").asDouble() : 0.0;

           
                String title=v.get("title").asText();
                System.out.println("Proccesing Report..."+title);
                 // Skip low severity vulnerabilities (score < 8)
                if (cvssScore < 8.0) {
                 System.out.println("Skipping low severity vulnerability (CVSS: " + cvssScore + "): " + title);
                continue;
                 }
                String severity = v.has("severityWithCritical") ? v.get("severityWithCritical").asText() : v.get("severity").asText();
                if (!severity.equalsIgnoreCase("critical") &&  !title.toLowerCase().contains("remote code execution")) {
                 System.out.println("Skipping non-critical vulnerability: " + severity);
                    continue;
                    }
                String pkg=v.get("packageName").asText();
                System.out.println("Count:"+count);
                if(count>1)
                    break;
 
                RiskAnalysis risk = AIRiskAnalyzer.analyze("SNYK",title,severity,pkg);
                Thread.sleep(4000);
                count++;
                 if (risk == null) {
                    System.out.println("AI analysis failed. Creating Jira ticket anyway.");
                    risk = new RiskAnalysis();
                    risk.criticality = severity.equalsIgnoreCase("critical") ? "Critical" : "High";
                    risk.riskScore = severity.equalsIgnoreCase("critical") ? 10 : 8;
                    risk.businessImpact = "Manual review required. Refer to Snyk report.";
                    risk.remediation = "Upgrade or patch as recommended in the Snyk report.";
                    }
                String summary="[SNYK] "+title;
 
                JiraService.createTicketIfNeeded(summary,risk);

                 processedCount++;
                if (processedCount >= 2) break; // Optional: limit tickets per run

                // Small delay to avoid API rate limits
                Thread.sleep(4000);
                
 
            }
 
        }catch(Exception e){
            e.printStackTrace();
        }
 
    }
 
}
 