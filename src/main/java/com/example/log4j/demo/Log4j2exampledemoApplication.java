package com.example.log4j.demo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Log4j2exampledemoApplication {

	static final Logger logger = LogManager.getLogger(Log4j2exampledemoApplication.class);
	
	public static void main(String[] args) {
		logger.info("this is a info message");
	    logger.warn("this is a warn message");
	    logger.error("this is a error message");
		SpringApplication.run(Log4j2exampledemoApplication.class, args);
	}

}
