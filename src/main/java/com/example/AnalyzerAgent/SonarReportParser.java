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

            String api = "https://sonarcloud.io/api/issues/search?componentKeys=" + projectKey
                    + "&types=VULNERABILITY&statuses=OPEN&severities=BLOCKER,CRITICAL,MAJOR";
            System.out.println("SONAR API: " + api);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new URL(api));
            JsonNode issues = root.get("issues");

            if (issues == null || !issues.isArray() || issues.size() == 0) {
                System.out.println("No vulnerabilities found.");
                return;
            }

            // Aggregate all issues for AI prompt
            List<String> issueSummaries = new ArrayList<>();
            for (JsonNode issue : issues) {
                String message = issue.get("message").asText();
                String severity = issue.get("severity").asText();
                String component = issue.get("component").asText().split(":")[1]; // file path

                issueSummaries.add("[" + severity + "] " + component + ": " + message);
            }

            // Prepare AI prompt for overall analysis
            StringBuilder prompt = new StringBuilder();
            prompt.append("You are a DevSecOps security expert.\n");
            prompt.append("Analyze the following Sonar issues as a single security report.\n\n");
            for (String s : issueSummaries) {
                prompt.append("- ").append(s).append("\n");
            }
            prompt.append("\nProvide overall analysis in JSON format:\n");
            prompt.append("{\n");
            prompt.append("\"criticality\":\"Low/Medium/High/Critical\",\n");
            prompt.append("\"riskScore\":0-10,\n");
            prompt.append("\"businessImpact\":\"text\",\n");
            prompt.append("\"remediation\":\"text\"\n");
            prompt.append("}");

            StringBuilder aggregatedIssues = new StringBuilder();
            for (String s : issueSummaries) {
                aggregatedIssues.append(s).append("\n");
            }

            // Call AI with actual issues
            RiskAnalysis risk = AIRiskAnalyzer.analyze(
                    "SONAR",
                    aggregatedIssues.toString(),  // Pass all issues here
                    "BLOCKER",
                    "code"
            );
            System.out.println("Calling AI for overall Sonar risk analysis...");
          //  RiskAnalysis risk = AIRiskAnalyzer.analyze("SONAR", "Aggregated Sonar issues", "BLOCKER", "code");

            if (risk == null) {
                System.out.println("AI analysis failed. Using fallback risk.");
                return;
                // risk = new RiskAnalysis();
                // risk.criticality = "High";
                // risk.riskScore = 8.5;
                // risk.businessImpact = "Multiple vulnerabilities detected in the codebase.";
                // risk.remediation = "Review all Sonar issues and apply fixes according to severity.";
            }

            // Build single Jira ticket
            String summary = "[SONAR] " + " - " + issues.size() + " Vulnerabilities Detected";

            StringBuilder description = new StringBuilder();
            description.append("Sonar Vulnerabilities Report:\n\n");
            for (String s : issueSummaries) {
                description.append(s).append("\n\n");
            }

            description.append("\n=== AI Risk Analysis ===\n");
            description.append("Criticality: ").append(risk.criticality).append("\n");
            description.append("Risk Score: ").append(risk.riskScore).append("\n");
            description.append("Impact: ").append(risk.businessImpact).append("\n");
            description.append("Remediation: ").append(risk.remediation).append("\n");

            // Create only one ticket
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