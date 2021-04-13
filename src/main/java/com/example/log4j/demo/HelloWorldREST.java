package com.example.log4j.demo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldREST {
	
	static final Logger logger = LogManager.getLogger(HelloWorldREST.class);
	
	@GetMapping("/test")
	void hello() {
		logger.info("this is a Hello message from Spring");
		
	}

}
