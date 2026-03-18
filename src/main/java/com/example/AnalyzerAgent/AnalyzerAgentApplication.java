package com.example.AnalyzerAgent;


public class AnalyzerAgentApplication {

	public static void main(String[] args) {
		//SpringApplication.run(AnalyzerAgentApplication.class, args);
		System.out.println("Starting Analysis");
		try{
			SnykReporterParser.parse();
		//SonarReportParser.parse();
		
		
		}
		catch(Exception e){
			System.err.println("Error");
			e.printStackTrace();
		}
		System.out.println("Analysis Completed");
		System.exit(0);
	}

	
}
