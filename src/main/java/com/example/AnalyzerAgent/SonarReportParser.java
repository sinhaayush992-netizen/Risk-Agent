package com.example.AnalyzerAgent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
 
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.management.RuntimeErrorException;
 
public class SonarReportParser {
 
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

            // Aggregate all issues
            List<String> issueSummaries = new ArrayList<>();
            List<String> issueDetails = new ArrayList<>();
            for (JsonNode issue : issues) {
                String issueKey = issue.get("key").asText();
                String message = issue.get("message").asText();
                String severity = issue.get("severity").asText();
                String component = issue.get("component").asText().split(":")[1]; // file path

                String safeMessage = message.replaceAll("[^a-zA-Z0-9\\s]", "_");

                issueSummaries.add(issueKey + ": " + safeMessage);
                issueDetails.add(issueKey + " | " + component + " | Severity: " + severity + "\n" + message);
            }

            // Create a combined summary and description
            String summary = "[SONAR] " + projectKey + " - " + issues.size() + " Vulnerabilities Detected";
            StringBuilder description = new StringBuilder();
            description.append("Sonar Vulnerabilities Report:\n\n");
            for (String detail : issueDetails) {
                description.append(detail).append("\n\n");
            }

            // Optional: Call AI once for overall risk (sum or highest severity)
            // You can create a simple RiskAnalysis object if you don't want AI
            RiskAnalysis risk = new RiskAnalysis();
            risk.criticality = "High"; // or calculate based on max severity
            risk.riskScore = 8.5;
            risk.businessImpact = "Multiple security vulnerabilities detected in codebase.";
            risk.remediation = "Review the listed vulnerabilities and fix according to Sonar recommendations.";

            // Create only ONE Jira ticket
            JiraService.createTicketIfNeeded(summary, risk);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // public static void parse() {
    //     try {
    //         String projectKey = System.getenv("SONAR_PROJECT_KEY");
    //         if (projectKey == null || projectKey.isEmpty()) {
    //             System.out.println("Project Key Missing");
    //             throw new RuntimeException("SONAR_PROJECT_KEY is missing");
    //         }

    //         // Sonar API URL - filter only OPEN vulnerabilities
    //         String api = "https://sonarcloud.io/api/issues/search?componentKeys=" + projectKey
    //                 + "&types=VULNERABILITY&statuses=OPEN&severities=BLOCKER,CRITICAL,MAJOR";
    //         System.out.println("SONAR API: " + api);

    //         ObjectMapper mapper = new ObjectMapper();
    //         JsonNode root = mapper.readTree(new URL(api));
    //         JsonNode issues = root.get("issues");

    //         if (issues == null || !issues.isArray() || issues.size() == 0) {
    //             System.out.println("No issues found.");
    //             return;
    //         }

    //         for (JsonNode issue : issues) {
    //             String issueKey = issue.get("key").asText();
    //             String message = issue.get("message").asText();
    //             String severity = issue.get("severity").asText();
    //             String component = issue.get("component").asText().split(":")[1]; // file path

    //             // Call AI Analyzer
    //             RiskAnalysis risk = AIRiskAnalyzer.analyze("SONAR", message, severity, component);

    //             if (risk == null) {
    //                 System.out.println("AI analysis failed for issue " + issueKey);
    //                 continue;
    //             }

    //             // Construct Jira summary using Sonar issue key to prevent duplicates
    //             String safeMessage = message.replaceAll("[^a-zA-Z0-9\\s]", "_");
    //             String summary = "[SONAR] " + issueKey + " | " + component + " | " + safeMessage;

    //             JiraService.createTicketIfNeeded(summary, risk);

    //             // Avoid spamming AI/API
    //             Thread.sleep(5000);
    //         }

    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }
 
}