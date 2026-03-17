package com.example.AnalyzerAgent;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AnalyzerAgentApplication {

	public static void main(String[] args) {
		//SpringApplication.run(AnalyzerAgentApplication.class, args);
		SnykReporterParser.parse();
		SonarReportParser.parse();
		System.out.println("Analysis Completed");
	}

	
}
