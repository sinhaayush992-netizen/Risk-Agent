package com.example.AnalyzerAgent;
 
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
 
import java.util.Base64;
 
public class JiraService {
 
    private static final String JIRA_URL="https://sinhaayush992.atlassian.net/rest/api/3";
    private static final String EMAIL=System.getenv("JIRA_EMAIL");
    private static final String TOKEN=System.getenv("JIRA_TOKEN");
    private static final String PROJECT=System.getenv("JIRA_PROJECT_KEY");
 
    private static final double THRESHOLD=7.0;
 
    private static String auth(){
 
        String cred=EMAIL+":"+TOKEN;
 
        return "Basic "+
        Base64.getEncoder().encodeToString(cred.getBytes());
    }
 
    public static boolean ticketExists(String summary){
 
        try{
 
            String jql=
            "project="+PROJECT+
            " AND summary~\""+summary+"\"";
 
            String url=
            JIRA_URL+"/search?jql="+jql;
 
            String response=
            Request.get(url)
            .addHeader("Authorization",auth())
            .execute()
            .returnContent()
            .asString();
 
            ObjectMapper mapper=new ObjectMapper();
 
            JsonNode root=mapper.readTree(response);
 
            int total=root.get("total").asInt();
 
            return total>0;
 
        }catch(Exception e){
            e.printStackTrace();
        }
 
        return false;
    }
 
    public static void createTicketIfNeeded(String summary,RiskAnalysis risk){
 
        try{
            System.out.println("Check");

            System.out.println("JIRA_URL = " + JIRA_URL);
            System.out.println("EMAIL = " + EMAIL);
            System.out.println("PROJECT = " + PROJECT);
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
 
    public static void createTicket(String summary,String description){
 
        try{

            System.out.println("Ticket Creating");
 
            String url=JIRA_URL+"/issue";
 
            String body=
            "{"+
            "\"fields\":{"+
            "\"project\":{\"key\":\""+PROJECT+"\"},"+
            "\"summary\":\""+summary+"\","+
            "\"description\":\""+description+"\","+
            "\"issuetype\":{\"name\":\"Task\"}"+
            "}"+
            "}";
 
            Request.post(url)
            .addHeader("Authorization",auth())
            .addHeader("Content-Type","application/json")
            .bodyString(body,ContentType.APPLICATION_JSON)
            .execute();
 
            System.out.println("Jira ticket created");
 
        }catch(Exception e){
            e.printStackTrace();
        }
 
    }
 
}