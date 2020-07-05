package com.example.demorhdg;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.SaslQop;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;



@RestController
@RequestMapping(path = "/rhdg")
public class RHDGMessageController {
	
	@Autowired
	@Value("${application.url}")
	private String urlServerAddress;
	@Value("${jdg.host}")
	private String JDG_HOST;
	@Value("${jdg.hotrod.port}")
	private Integer HOTROD_PORT;
	@Value("${jdg.hotrod.HOTROD_ROUTE_HOSTNAME}")
	private String HOTROD_ROUTE_HOSTNAME;
	@Value("${jdg.hotrod.USERNAME}")
	private String USERNAME;
	@Value("${jdg.hotrod.PASSWORD}")
	private String PASSWORD;
	@Value("${jdg.hotrod.APPLICATION_NAME}")
	private String APPLICATION_NAME;
			
	Gson gson = new Gson();
	private RemoteCacheManager cacheManager;
    private RemoteCache<String, Object> cache;
	
	
	@PutMapping(path= "/insert", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> rhdgInnsertData(@RequestBody String value){
		
		
		
		HttpURLConnection connection = null;
		try {
			  System.out.println("----------------------------------------");
		      System.out.println("Executing PUT");
		      System.out.println("Executing urlServerAddress: " + urlServerAddress);
		      System.out.println("----------------------------------------");
		      URL address = new URL(urlServerAddress);
		      System.out.println("executing request " + urlServerAddress);
		      connection = (HttpURLConnection) address.openConnection();
		      System.out.println("Executing put method of value: " + value);
		      connection.setRequestMethod("PUT");
		      connection.setRequestProperty("Content-Type", "text/plain");
		      connection.setDoOutput(true);

		      OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
		      outputStreamWriter.write(value.toString());
		         
		      connection.connect();
		      outputStreamWriter.flush();
		       
		      System.out.println("----------------------------------------");
		      System.out.println(connection.getResponseCode() + " " + connection.getResponseMessage());
		      System.out.println("----------------------------------------");
			
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ResponseStatusException(
			           HttpStatus.NOT_ACCEPTABLE, "Token validation failed");
		}
		finally {
			connection.disconnect();
			
		} 
		
		return new ResponseEntity<String>("successfully inserted into RHDG cache", HttpStatus.OK);		
	}
	
	@GetMapping(path="/fetch", produces = "application/json")
	public ResponseEntity<String> rhdgGetData(){
		
		HttpURLConnection connection = null;
		
		try {
			String line = new String();
		      StringBuilder stringBuilder = new StringBuilder();

		      System.out.println("----------------------------------------");
		      System.out.println("Executing GET");
		      System.out.println("----------------------------------------");

		      URL address = new URL(urlServerAddress);
		      System.out.println("executing request " + urlServerAddress);

		      connection = (HttpURLConnection) address.openConnection();
		      connection.setRequestMethod("GET");
		      connection.setRequestProperty("Content-Type", "text/plain");
		      connection.setDoOutput(true);

		      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

		      connection.connect();

		      while ((line = bufferedReader.readLine()) != null) {
		         stringBuilder.append(line + '\n');
		      }

		      System.out.println("Executing get method of value: " + stringBuilder.toString());

		      System.out.println("----------------------------------------");
		      System.out.println(connection.getResponseCode() + " " + connection.getResponseMessage());
		      System.out.println("----------------------------------------");		      

		      
		      return new ResponseEntity<String>(gson.toJson(stringBuilder.toString()), HttpStatus.OK);
			
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ResponseStatusException(
			           HttpStatus.NOT_ACCEPTABLE, "Token validation failed");
		}
		finally {
			connection.disconnect();
			
		} 		
	}
	
	@PutMapping(path= "/insert/hotrod", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> rhdghotrodInnsertData(@RequestBody String value){
		ConfigurationBuilder builder = new ConfigurationBuilder();
		try {
			  System.out.println("----------------------------------------");
		      System.out.println("Executing PUT");
		      System.out.println("----------------------------------------");
//		      builder = new ConfigurationBuilder();
//		        builder.addServer()
//		              .host(JDG_HOST)
//		              .port(Integer.parseInt(HOTROD_PORT));
//		        cacheManager = new RemoteCacheManager(builder.build());
//		        cache = cacheManager.getCache("default");
//		        cache.put("a", value);	
//		        ConfigurationBuilder builder = new ConfigurationBuilder();
		        builder.addServer()
		        	// Connection
		        	.host(JDG_HOST).port(HOTROD_PORT)
		        	// Use BASIC client intelligence.
		        	.clientIntelligence(ClientIntelligence.BASIC)
		        	.security()
		                // Authentication
		                .authentication().enable()
		                .username(USERNAME)
		                .password(PASSWORD)
		                .serverName(APPLICATION_NAME)
		                .saslQop(SaslQop.AUTH)
		                // Encryption
		                .ssl()
		                .sniHostName(HOTROD_ROUTE_HOSTNAME)
		                .trustStorePath(".\\tls.crt");
		        
		       
		      System.out.println("----------------------------------------");
		      System.out.println("End hot rod connection");
		      System.out.println("----------------------------------------");
			
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ResponseStatusException(
			           HttpStatus.NOT_ACCEPTABLE, "Token validation failed");
		}
		
		return new ResponseEntity<String>("successfully inserted into RHDG hot rod cache", HttpStatus.OK);
		
	}
	

}
