package com.example.AnalyzerAgent;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AnalyzerAgentApplication {

	public static void main(String[] args) {
		//SpringApplication.run(AnalyzerAgentApplication.class, args);
		System.out.println("Starting Analysis");
		SnykReporterParser.parse();
		SonarReportParser.parse();
		System.out.println("Analysis Completed");
	}

	
}
