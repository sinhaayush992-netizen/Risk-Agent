package com.example.AnalyzerAgent;
 
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;
 
public class AIRiskAnalyzer {
 
   public static RiskAnalysis analyze(String tool,String title,String severity,String component){
 
    try{
 
        String API_KEY = System.getenv("OPENAI_API_KEY");
 
        if (API_KEY == null || API_KEY.isEmpty()) {
            throw new RuntimeException("OPENAI_API_KEY is missing!");
        }
 
        String prompt =
                "You are a DevSecOps security expert.\n" +
                "Analyze the vulnerability.\n\n" +
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
 
        ObjectMapper mapper = new ObjectMapper();
 
        JsonNode requestBody = mapper.createObjectNode()
                .put("model", "gpt-4o-mini")
                .putArray("messages")
                .addObject()
                .put("role", "user")
                .put("content", prompt);
 
        String body = mapper.writeValueAsString(requestBody);
 
        System.out.println("Calling AI...");
        System.out.println("API KEY PRESENT"+(API_KEY!=null));
         System.out.println("API KEY length"+(API_KEY!=null? API_KEY.length():0));
         String response=null;
        try{
             response =
                Request.post("https://api.openai.com/v1/chat/completions")
                        .addHeader("Authorization","Bearer "+API_KEY)
                        .addHeader("Content-Type","application/json")
                        .bodyString(body, ContentType.APPLICATION_JSON)
                        .execute()
                        .returnContent()
                        .asString();

                        System.out.println("AI response received");
        }
        catch(Exception ex){
            System.out.println("ERROR FROM AI");
            ex.printStackTrace();
        }
        
 
        JsonNode root = mapper.readTree(response);
 
        String content =
                root.get("choices")
                        .get(0)
                        .get("message")
                        .get("content")
                        .asText();
 
        return mapper.readValue(content, RiskAnalysis.class);
 
    }catch(Exception e){
        e.printStackTrace();
    }
 
    return null;
}
 
}
 
