// package com.example.AnalyzerAgent;

 
// import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ObjectMapper;
 
// import java.io.File;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.nio.file.Paths;
// import java.util.ArrayList;
// import java.util.List;
 
// public class SnykReporterParser {
 
//     // public static void parse(){

//     //     System.out.println("Reading Snyk Report...");
 
//     //     try{
 
//     //         ObjectMapper mapper=new ObjectMapper();
 
//     //         JsonNode root = mapper.readTree(new File("snyk-report.json"));
 
//     //         JsonNode vulns=root.get("vulnerabilities");

//     //         System.out.println(new String(Files.readAllBytes(Paths.get("snyk-report.json"))));

//     //         if (vulns == null || !vulns.isArray() || vulns.size() == 0) {
//     //             System.out.println("Snyk report is empty. Skipping.");
//     //             return;
//     //         }

//     //         int count =0;
//     //         int processedCount=0;
//     //         int vulnerability=0;
//     //            List<JsonNode> highRiskVulns = new ArrayList<>();
//     //             for (JsonNode v : vulns) {
//     //                 double cvssScore = v.has("cvssScore") ? v.get("cvssScore").asDouble() : 0.0;
//     //                 String severity = v.has("severityWithCritical") ? v.get("severityWithCritical").asText() : v.get("severity").asText();
//     //                 String title = v.get("title").asText();

//     //                 if (cvssScore >= 9.0 && (severity.equalsIgnoreCase("critical") || title.toLowerCase().contains("remote code execution"))) {
//     //                     highRiskVulns.add(v);
//     //                 } else {
//     //                     System.out.println("Skipping low risk vulnerability: " + title + " (CVSS: " + cvssScore + ")");
//     //                 }
//     //             }
//     //         for(JsonNode v:vulns){
//     //             vulnerability++;
//     //             double cvssScore = v.has("cvssScore") ? v.get("cvssScore").asDouble() : 0.0;
//     //             System.out.println("Vulnerabilty no."+vulnerability);
           
//     //             String title=v.get("title").asText();
//     //             System.out.println("Proccesing Report..."+title);
//     //              // Skip low severity vulnerabilities (score < 8)
//     //             if (cvssScore < 9.0) {
//     //                  System.out.println("Skiped Vulnerabilty no."+vulnerability);
//     //              System.out.println("Skipping low severity vulnerability (CVSS: " + cvssScore + "): " + title);
//     //             continue;
//     //              }
//     //             String severity = v.has("severityWithCritical") ? v.get("severityWithCritical").asText() : v.get("severity").asText();
//     //             if (!severity.equalsIgnoreCase("critical") &&  !title.toLowerCase().contains("remote code execution")) {
//     //              System.out.println("Skipping non-critical vulnerability: " + severity);
//     //                 continue;
//     //                 }
//     //             String pkg=v.get("packageName").asText();
//     //             System.out.println("Count:"+count);
//     //             if(count>1)
//     //                 break;
//     //              System.out.println("Excecute Vulnerabilty no."+vulnerability);
//     //             RiskAnalysis risk = AIRiskAnalyzer.analyze("SNYK",title,severity,pkg);
//     //             Thread.sleep(4000);
//     //             count++;
//     //              if (risk == null) {
//     //                 System.out.println("AI analysis failed. Creating Jira ticket anyway.");
//     //                 risk = new RiskAnalysis();
//     //                 risk.criticality = severity.equalsIgnoreCase("critical") ? "Critical" : "High";
//     //                 risk.riskScore = severity.equalsIgnoreCase("critical") ? 10 : 8;
//     //                 risk.businessImpact = "Manual review required. Refer to Snyk report.";
//     //                 risk.remediation = "Upgrade or patch as recommended in the Snyk report.";
//     //                 }
//     //             String summary="[SNYK] "+title;
 
//     //             JiraService.createTicketIfNeeded(summary,risk);

//     //              processedCount++;
//     //             if (processedCount >= 2) break; // Optional: limit tickets per run

//     //             // Small delay to avoid API rate limits
//     //             Thread.sleep(4000);
                
 
//     //         }
 
//     //     }catch(Exception e){
//     //         e.printStackTrace();
//     //     }
 
//     // }

//     public static void parse() {

//     System.out.println("Reading Snyk Report...");

//     try {
//         ObjectMapper mapper = new ObjectMapper();
//         JsonNode root = mapper.readTree(new File("snyk-report.json"));
//         JsonNode vulns = root.get("vulnerabilities");

//         if (vulns == null || !vulns.isArray() || vulns.size() == 0) {
//             System.out.println("Snyk report is empty. Skipping.");
//             return;
//         }

//         // Step 1: Filter high-risk vulnerabilities
//         List<JsonNode> highRiskVulns = new ArrayList<>();
//         int vulnerabilityNumber = 0;
//         for (JsonNode v : vulns) {
//             vulnerabilityNumber++;
//             double cvssScore = v.has("cvssScore") ? v.get("cvssScore").asDouble() : 0.0;
//             String severity = v.has("severityWithCritical") ? v.get("severityWithCritical").asText() : v.get("severity").asText();
//             String title = v.get("title").asText();

//             if (cvssScore >= 8.0 && (severity.equalsIgnoreCase("critical") || title.toLowerCase().contains("remote code execution"))) {
//                 highRiskVulns.add(v);
//             } else {
//                 System.out.println("Skipping low-risk vulnerability #" + vulnerabilityNumber + ": " + title + " (CVSS: " + cvssScore + ", Severity: " + severity + ")");
//             }
//         }

//         // Step 2: Print filtered high-risk vulnerabilities
//         System.out.println("\n=== High-Risk Vulnerabilities ===");
//         for (JsonNode v : highRiskVulns) {
//             System.out.println("- " + v.get("title").asText() + " | CVSS: " + v.get("cvssScore").asDouble() + " | Severity: " +
//                                (v.has("severityWithCritical") ? v.get("severityWithCritical").asText() : v.get("severity").asText()));
//         }
//         System.out.println("================================\n");

//         // Step 3: Process filtered vulnerabilities
//         int processedCount = 0;
//         int maxAiCalls = 2; // optional limit
//         for (JsonNode v : highRiskVulns) {
//             if (processedCount >= maxAiCalls) break;

//             String title = v.get("title").asText();
//             String severity = v.has("severityWithCritical") ? v.get("severityWithCritical").asText() : v.get("severity").asText();
//             String pkg = v.get("packageName").asText();

//             System.out.println("Processing high-risk vulnerability: " + title);

//             RiskAnalysis risk = AIRiskAnalyzer.analyze("SNYK", title, severity, pkg);
//             Thread.sleep(2000);

//             if (risk == null) {
//                 System.out.println("AI analysis failed. Creating Jira ticket anyway.");
//              //   continue;
//                 risk = new RiskAnalysis();
//                 risk.criticality = severity.equalsIgnoreCase("critical") ? "Critical" : "High";
//                 risk.riskScore = severity.equalsIgnoreCase("critical") ? 10 : 8;
//                 risk.businessImpact = "Manual review required. Refer to Snyk report.";
//                 risk.remediation = "Upgrade or patch as recommended in the Snyk report.";
//             }

//             String summary = "[SNYK] " + title;
//             JiraService.createTicketIfNeeded(summary, risk);

//             processedCount++;
//             Thread.sleep(2000); // small delay for API rate limits
//         }

//     } catch (Exception e) {
//         e.printStackTrace();
//     }

// }
 
// }
 package com.example.AnalyzerAgent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SnykReporterParser {

    public static void parse() {
        System.out.println("Reading Snyk Report...");

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new File("snyk-report.json"));
            JsonNode vulns = root.get("vulnerabilities");

            if (vulns == null || !vulns.isArray() || vulns.size() == 0) {
                System.out.println("Snyk report is empty. Skipping.");
                return;
            }

            // Step 1: Filter high-risk vulnerabilities
            List<JsonNode> highRiskVulns = new ArrayList<>();
            int vulnerabilityNumber = 0;
            for (JsonNode v : vulns) {
                vulnerabilityNumber++;
                double cvssScore = v.has("cvssScore") ? v.get("cvssScore").asDouble() : 0.0;
                String severity = v.has("severityWithCritical") ? v.get("severityWithCritical").asText() : v.get("severity").asText();
                String title = v.get("title").asText();

                if (cvssScore >= 8.0 && (severity.equalsIgnoreCase("critical") || title.toLowerCase().contains("remote code execution"))) {
                    highRiskVulns.add(v);
                } else {
                    System.out.println("Skipping low-risk vulnerability #" + vulnerabilityNumber + ": " + title +
                            " (CVSS: " + cvssScore + ", Severity: " + severity + ")");
                }
            }

            // Step 2: Print filtered high-risk vulnerabilities
            System.out.println("\n=== High-Risk Vulnerabilities ===");
            for (JsonNode v : highRiskVulns) {
                String severity = v.has("severityWithCritical") ? v.get("severityWithCritical").asText() : v.get("severity").asText();
                System.out.println("- " + v.get("title").asText() + " | CVSS: " + v.get("cvssScore").asDouble() + " | Severity: " + severity);
            }
            System.out.println("================================\n");

            // Step 3: Process filtered vulnerabilities
            int processedCount = 0;
            int maxTicketsPerRun = 2; // limit tickets per run
            for (JsonNode v : highRiskVulns) {
                if (processedCount >= maxTicketsPerRun) break;

                String title = v.get("title").asText();
                String severity = v.has("severityWithCritical") ? v.get("severityWithCritical").asText() : v.get("severity").asText();
                String pkg = v.get("packageName").asText();

                System.out.println("Processing high-risk vulnerability: " + title);

                // Call AI for risk analysis
                RiskAnalysis risk = AIRiskAnalyzer.analyze("SNYK", title, severity, pkg);

                if (risk == null) {
                    System.out.println("AI analysis failed. Creating Jira ticket anyway.");
                    continue;
                    // risk = new RiskAnalysis();
                    // risk.criticality = severity.equalsIgnoreCase("critical") ? "Critical" : "High";
                    // risk.riskScore = severity.equalsIgnoreCase("critical") ? 10 : 8;
                    // risk.businessImpact = "Manual review required. Refer to Snyk report.";
                    // risk.remediation = "Upgrade or patch as recommended in the Snyk report.";
                }

                String summary = "[SNYK] " + title;
                JiraService.createTicketIfNeeded(summary, risk);

                processedCount++;
                Thread.sleep(2000); // small delay to avoid API throttling
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}