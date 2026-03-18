// package com.example.AnalyzerAgent;
 
// import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.databind.node.ArrayNode;
// import com.fasterxml.jackson.databind.node.ObjectNode;

// import org.apache.hc.client5.http.fluent.Request;
// import org.apache.hc.core5.http.ContentType;
 
// public class AIRiskAnalyzer {
 
//   public static RiskAnalysis analyze(String tool, String title, String severity, String component) {
//     ObjectMapper mapper = new ObjectMapper();

//     try {
//         String API_KEY = System.getenv("OPENAI_API_KEY");
//         if (API_KEY == null || API_KEY.isEmpty()) {
//             throw new RuntimeException("OPENAI_API_KEY is missing!");
//         }

//         String prompt =
//                 "You are a DevSecOps security expert.\nAnalyze the vulnerability.\n\n" +
//                 "Tool: " + tool + "\n" +
//                 "Component: " + component + "\n" +
//                 "Severity: " + severity + "\n" +
//                 "Issue: " + title + "\n\n" +
//                 "Return JSON like:\n" +
//                 "{\n" +
//                 "\"criticality\":\"Low/Medium/High/Critical\",\n" +
//                 "\"riskScore\":0-10,\n" +
//                 "\"businessImpact\":\"text\",\n" +
//                 "\"remediation\":\"text\"\n" +
//                 "}";

//         // Build request JSON
//         ObjectNode requestBody = mapper.createObjectNode();
//         requestBody.put("model", "gpt-4o-mini");

//         ArrayNode messages = mapper.createArrayNode();
//         ObjectNode message = mapper.createObjectNode();
//         message.put("role", "user");
//         message.put("content", prompt);
//         messages.add(message);
//         requestBody.set("messages", messages);

//         String body = mapper.writeValueAsString(requestBody);
//         System.out.println("REQUEST BODY:\n" + body);

//         int maxRetries = 2;
//         int retryCount = 0;
//         long waitTimeMs = 8000; // 2 seconds initial
//         int maxAiCalls=1;

// String response= null;

// while (retryCount < maxRetries) {
//     try {
//         if(maxAiCalls>=2)
//             break;
//         response = Request.post("https://api.openai.com/v1/chat/completions")
//             .addHeader("Authorization", "Bearer " + API_KEY)
//             .addHeader("Content-Type", "application/json")
//             .bodyString(body, ContentType.APPLICATION_JSON)
//             .execute()
//             .returnContent()
//             .asString();

//             maxAiCalls++;
//         break; // success
//     } catch (org.apache.hc.client5.http.HttpResponseException ex) {
//         if (ex.getStatusCode() == 429) { // rate limit
//             retryCount++;
//             System.out.println("Rate limit hit. Retrying in " + waitTimeMs/1000 + " seconds...");
//             Thread.sleep(waitTimeMs);
//             waitTimeMs *= 2; // exponential backoff
//         } else {
//             throw ex;
//         }
//     }
// }
// if (response == null || response.isEmpty()) {
//     System.out.println("AI response is null after retries. Skipping this vulnerability.");
//     return null;
// }
//         // Parse response
//         JsonNode root = mapper.readTree(response);
//         String content = root.get("choices")
//                              .get(0)
//                              .get("message")
//                              .get("content")
//                              .asText();

//         System.out.println("AI CONTENT:\n" + content);

//         return mapper.readValue(content, RiskAnalysis.class);

//     } catch (Exception e) {
//         e.printStackTrace();
//     }

//     return null;
// }
 
// }
 
package com.example.AnalyzerAgent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;

public class AIRiskAnalyzer {

    // Class-level counter for AI calls
    private static int aiCallsThisRun = 0;
    private static final int MAX_AI_CALLS_PER_RUN = 1; // adjust as needed

    public static RiskAnalysis analyze(String tool, String title, String severity, String component) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            String API_KEY = System.getenv("OPENAI_API_KEY");
            if (API_KEY == null || API_KEY.isEmpty()) {
                throw new RuntimeException("OPENAI_API_KEY is missing!");
            }

            // Skip AI call if limit reached
            if (aiCallsThisRun >= MAX_AI_CALLS_PER_RUN) {
                System.out.println("Skipping AI call for \"" + title + "\" due to max AI call limit (" + MAX_AI_CALLS_PER_RUN + ")");
                return null; // fallback logic will create the ticket anyway
            }

            String prompt =
                    "You are a DevSecOps security expert.\nAnalyze the vulnerability.\n\n" +
                    "Tool: " + tool + "\n" +
                    "Component: " + component + "\n" +
                    "Severity: " + severity + "\n" +
                    "Issue: " + title + "\n\n" +
                    "Return JSON like:\n" +
                    "{\n" +
                    "\"criticality\":\"Low/Medium/High/Critical\",\n" +
                    "\"riskScore\":0-10,\n" +
                    "\"businessImpact\":\"text\",\n" +
                    "\"remediation\":\"text\"\n" +
                    "}";

            // Build request JSON
            ObjectNode requestBody = mapper.createObjectNode();
            requestBody.put("model", "gpt-4o-mini");

            ArrayNode messages = mapper.createArrayNode();
            ObjectNode message = mapper.createObjectNode();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);
            requestBody.set("messages", messages);

            String body = mapper.writeValueAsString(requestBody);
            System.out.println("REQUEST BODY:\n" + body);

            int maxRetries = 2;
            int retryCount = 0;
            long waitTimeMs = 8000;
            String response = null;

            while (retryCount < maxRetries) {
                try {
                    response = Request.post("https://api.openai.com/v1/chat/completions")
                            .addHeader("Authorization", "Bearer " + API_KEY)
                            .addHeader("Content-Type", "application/json")
                            .bodyString(body, ContentType.APPLICATION_JSON)
                            .execute()
                            .returnContent()
                            .asString();

                    aiCallsThisRun++; // increment counter **only after a successful call**
                    break;

                } catch (org.apache.hc.client5.http.HttpResponseException ex) {
                    if (ex.getStatusCode() == 429) {
                        retryCount++;
                        System.out.println("Rate limit hit. Retrying in " + waitTimeMs/1000 + " seconds...");
                        Thread.sleep(waitTimeMs);
                        waitTimeMs *= 2;
                    } else {
                        throw ex;
                    }
                }
            }

            if (response == null || response.isEmpty()) {
                System.out.println("AI response is null after retries. Skipping this vulnerability.");
                return null;
            }

            // Parse response
            JsonNode root = mapper.readTree(response);
            String content = root.get("choices")
                                 .get(0)
                                 .get("message")
                                 .get("content")
                                 .asText();

            System.out.println("AI CONTENT:\n" + content);
            return mapper.readValue(content, RiskAnalysis.class);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}