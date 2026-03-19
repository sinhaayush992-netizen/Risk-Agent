package com.example.AnalyzerAgent;
 
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
 
public class JiraService {
 
    private static final String JIRA_URL="https://sinhaayush992.atlassian.net";
    private static final String EMAIL=System.getenv("JIRA_EMAIL");
    private static final String TOKEN=System.getenv("JIRA_TOKEN");
    private static final String PROJECT=System.getenv("JIRA_PROJECT_KEY");
 
    private static final double THRESHOLD=7.0;
 
    private static String auth(){
 
        String cred=EMAIL+":"+TOKEN;
 
        return "Basic "+
        Base64.getEncoder().encodeToString(cred.getBytes());
    }
 
    // public static boolean ticketExists(String summary){
 
    //     try{
    //             System.out.println("Inside Ticket Exist");
    //         String jql="project="+PROJECT+" AND summary~\""+summary+"\"";
    //              System.out.println("1");
    //         // String url=
    //         // JIRA_URL+"/search?jql="+jql;
    //         String url= JIRA_URL+"/rest/api/3/search?jql="+URLEncoder.encode(jql, StandardCharsets.UTF_8);
    //             System.out.println("2");
    //         String response=Request.get(url)
    //                         .addHeader("Authorization",auth())
    //                         .execute()
    //                         .returnContent()
    //                         .asString();
    //         System.out.println("3");
    //         ObjectMapper mapper=new ObjectMapper();
    //         System.out.println("4");
    //         JsonNode root=mapper.readTree(response);
    //             System.out.println("5");
    //         int total=root.get("total").asInt();
    //             System.out.println("6");
    //         return total>0;
 
    //     }catch(Exception e){
    //         System.out.println("7");
    //         e.printStackTrace();
    //     }
 
    //     return false;
    // }
    //duplicate card--
//    public static boolean ticketExists(String summary) {
//     try {
//         System.out.println("\n========== CHECKING IF TICKET EXISTS ==========");

//         if (summary == null || summary.trim().isEmpty()) {
//             System.out.println("Summary is null or empty → safe to create");
//             return false;
//         }

//         // Normalize summary for search
//         String normalizedSummary = summary.replaceAll("\\[|\\]", "")  // remove [ and ]
//                                          .replaceAll("\\s+", " ")    // collapse multiple spaces
//                                          .trim();

//         System.out.println("Original summary: [" + summary + "]");
//         System.out.println("Normalized summary for Jira search: [" + normalizedSummary + "]");

//         // Build fuzzy JQL search
//         String jql = "project=" + PROJECT + " AND summary ~ \"" + normalizedSummary + "\"";
//         String encodedJql = URLEncoder.encode(jql, StandardCharsets.UTF_8);

//         String url = JIRA_URL + "/rest/api/3/search/jql?jql=" + encodedJql +
//                      "&maxResults=20&fields=summary,status";

//         System.out.println("Jira search URL: " + url);

//         // Call Jira REST API
//         String response = Request.get(url)
//                 .addHeader("Authorization", auth())
//                 .addHeader("Accept", "application/json")
//                 .execute()
//                 .returnContent()
//                 .asString();

//         System.out.println("RAW RESPONSE FROM JIRA: " + response);

//         ObjectMapper mapper = new ObjectMapper();
//         JsonNode issues = mapper.readTree(response).get("issues");

//         if (issues == null || issues.size() == 0) {
//             System.out.println("No issues returned → safe to create");
//             return false;
//         }

//         System.out.println("Total issues returned: " + issues.size());

//         //  Loop through issues and check exact summary
//         for (JsonNode issue : issues) {
//             String foundSummary = issue.get("fields").get("summary").asText().trim();
//             String statusCategoryKey = issue.get("fields").get("status")
//                                             .get("statusCategory").get("key").asText();

//             System.out.println("Found ticket: [" + foundSummary + "], StatusCategory: " + statusCategoryKey);

//             // Block creation if exact summary matches and ticket is active (not Done)
//             if (foundSummary.equalsIgnoreCase(summary) && !statusCategoryKey.equalsIgnoreCase("done")) {
//                 System.out.println("Active ticket exists → BLOCK creation");
//                 return true;
//             }
//         }

//         System.out.println("No active tickets found → safe to create");
//         return false;

//     } catch (Exception e) {
//         e.printStackTrace();
//     }

//     return false;
// }
//-----------------
public static boolean ticketExists(String summary) {
    try {
        System.out.println("\n========== CHECKING IF TICKET EXISTS ==========");
        if (summary == null || summary.trim().isEmpty()) {
            System.out.println("Summary is empty → safe to create");
            return false;
        }

        System.out.println("Original summary: [" + summary + "]");

        // JQL: get all issues in project
        String jql = "project=" + PROJECT;
        String encodedJql = URLEncoder.encode(jql, StandardCharsets.UTF_8);
        String url = JIRA_URL + "/rest/api/3/search/jql?jql=" + encodedJql + "&maxResults=50&fields=summary,status";

        System.out.println("Jira search URL: " + url);

        String response = Request.get(url)
                .addHeader("Authorization", auth())
                .addHeader("Accept", "application/json")
                .execute()
                .returnContent()
                .asString();

        System.out.println("RAW RESPONSE FROM JIRA: " + response);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode issues = mapper.readTree(response).get("issues");

        if (issues == null || issues.size() == 0) {
            System.out.println("No issues found → safe to create");
            return false;
        }

        System.out.println("Total issues returned: " + issues.size());

        for (JsonNode issue : issues) {
            String foundSummary = issue.get("fields").get("summary").asText().trim();
            String statusCategoryKey = issue.get("fields").get("status")
                                            .get("statusCategory").get("key").asText();

            System.out.println("Found ticket: [" + foundSummary + "], StatusCategory: " + statusCategoryKey);

            if (foundSummary.equalsIgnoreCase(summary) && !statusCategoryKey.equalsIgnoreCase("done")) {
                System.out.println("Active ticket exists → BLOCK creation");
                return true;
            }
        }

        System.out.println("No active tickets found → safe to create");
        return false;

    } catch (Exception e) {
        e.printStackTrace();
    }

    return false;
}
//     public static boolean ticketExists(String summary) {
//     try {
//         System.out.println("Inside Ticket Exist");

//         // Proper JQL
//         String jql = "project=" + PROJECT + " AND summary=\"" + summary + "\"";

//         // Encode the JQL
//         String url = JIRA_URL + "/rest/api/3/search?jql=" + URLEncoder.encode(jql, StandardCharsets.UTF_8);
//         System.out.println("SEARCH URL: "+url);

//         System.out.println("Searching Jira URL: " + url);

//         // Add Accept header and Authorization
//         String response = Request.get(url)
//                 .addHeader("Authorization", auth())
//                 .addHeader("Accept", "application/json")
//                 .execute()
//                 .returnContent()
//                 .asString();

//         System.out.println("Response from Jira search: " + response);

//         ObjectMapper mapper = new ObjectMapper();
//         JsonNode root = mapper.readTree(response);

//         // Safety check
//         if (root.has("total")) {
//             int total = root.get("total").asInt();
//             return total > 0;
//         } else {
//             System.out.println("No 'total' field found in search response.");
//         }

//     } catch (org.apache.hc.client5.http.HttpResponseException e) {
//         // Special handling for Jira 410 Gone
//         System.out.println("Jira search returned HTTP " + e.getStatusCode() + ": " + e.getReasonPhrase());
//         if (e.getStatusCode() == 410) {
//             System.out.println("API deprecated or endpoint not available. Skipping ticketExists check.");
//             return false; // allow ticket creation
//         }
//         e.printStackTrace();
//     } catch (Exception e) {
//         e.printStackTrace();
//     }

//     return false;
// }
 
    public static void createTicketIfNeeded(String summary,RiskAnalysis risk){
 
        try{
            System.out.println("Check");

            System.out.println("JIRA_URL = " + JIRA_URL);
            System.out.println("EMAIL = " + EMAIL);
            System.out.println("PROJECT = " + PROJECT);
            System.out.println("JIRA_URL length = " + (JIRA_URL == null ? "null" : JIRA_URL.length()));
            System.out.println("JIRA_URL value = [" + JIRA_URL + "]");
            System.out.println("EMAIL length = " + (EMAIL == null ? "null" : EMAIL.length()));
            System.out.println("TOKEN length = " + (TOKEN == null ? "null" : TOKEN.length()));
            System.out.println("PROJECT length = " + (PROJECT == null ? "null" : PROJECT.length()));
 
            if(risk==null) return;
 
            if(risk.riskScore<THRESHOLD){
 
                System.out.println("Skipping low risk: "+summary);
                return;
 
            }
 
            if(ticketExists(summary)){
 
                System.out.println("Duplicate ticket found");
                return;
 
            }
 
            String description=
            "Criticality: "+risk.criticality+"\n\n"+
            "Risk Score: "+risk.riskScore+"\n\n"+
            "Impact:\n"+risk.businessImpact+"\n\n"+
            "Fix:\n"+risk.remediation;
        System.out.println("Staring Ticket Creation");
 
            createTicket(summary,description);
 
        }catch(Exception e){
            e.printStackTrace();
        }
 
    }
 
    // public static void createTicket(String summary,String description){
 
    //     try{

    //         System.out.println("Ticket Creating");
 
    //        // String url=JIRA_URL+"/issue";
    //        String url=JIRA_URL+"/rest/api/3/issue";

    //         System.out.println("iNSIDE tICKET JIRA_URL = " + JIRA_URL);
    //         System.out.println("iNSIDE tICKET JIRA_URL = " + url);
 
    //         String body=
    //         "{"+
    //         "\"fields\":{"+
    //         "\"project\":{\"key\":\""+PROJECT+"\"},"+
    //         "\"summary\":\""+summary+"\","+
    //         "\"description\":\""+description+"\","+
    //         "\"issuetype\":{\"name\":\"Task\"}"+
    //         "}"+
    //         "}";
 
    //         Request.post(url)
    //         .addHeader("Authorization",auth())
    //         .addHeader("Content-Type","application/json")
    //         .bodyString(body,ContentType.APPLICATION_JSON)
    //         .execute();
 
    //         System.out.println("Jira ticket created");
 
    //     }catch(Exception e){
    //         e.printStackTrace();
    //     }
 
    // }
 
    public static void createTicket(String summary, String description) {
    try {
        System.out.println("Ticket Creating");

        String url = JIRA_URL + "/rest/api/3/issue";
        System.out.println("iNSIDE tICKET JIRA_URL = " + url);

        // Build description in Atlassian Document Format (ADF)
        String adfDescription = "{"
                + "\"type\":\"doc\","
                + "\"version\":1,"
                + "\"content\":[{"
                +     "\"type\":\"paragraph\","
                +     "\"content\":[{"
                +         "\"type\":\"text\","
                +         "\"text\":\"" + description.replace("\n", "\\n").replace("\"", "\\\"") + "\""
                +     "}]"
                + "}]"
                + "}";

        String body = "{"
                + "\"fields\":{"
                +     "\"project\":{\"key\":\"" + PROJECT + "\"},"
                +     "\"summary\":\"" + summary + "\","
                +     "\"description\":" + adfDescription + ","
                +     "\"issuetype\":{\"name\":\"Task\"}"
                + "}"
                + "}";

        Request.post(url)
                .addHeader("Authorization", auth())
                .addHeader("Content-Type", "application/json")
                .bodyString(body, ContentType.APPLICATION_JSON)
                .execute();

        System.out.println("Jira ticket created");

    } catch (Exception e) {
        e.printStackTrace();
    }
}
}