package com.example.demo;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.google.gson.Gson;

import io.jsonwebtoken.Claims;

@RestController
@RequestMapping(path = "/OAuth/token")

public class SpringJWTCreateController{
	
	@Autowired
	@Value("${app.jwt.secret}")
	private List<String> secrets;
	Gson gson = new Gson();
//	private String secretPrivateKey = "";
//	String clientID = "";
	private boolean validateKeySuccess = false;
	
	@Autowired
	private JWTDemo jwtDemo;
	
	@GetMapping(path="/generate", produces = "application/json")
	public ResponseEntity<String> jwtToken(@RequestHeader("secret_key") String secret_key){
		System.out.println("Inside GetMapping");
		ResponseEntity<String> tokenJWT = null;
		    		
		if(secret_key!=null) {	
			try {
				for(String secret:secrets) {
					System.out.println("loop: "+secrets.size());
					System.out.println("loop: "+secret);
					String[] currentSecret = secret.split("::");
//					clientID = currentSecret[0].trim();
//					System.out.println("Current clientID: " +clientID);
					if(JWTDemo.validateRSA(JWTDemo.loadPublicKey(secret_key), JWTDemo.loadPrivateKey(currentSecret[1]))) {
//						return new ResponseEntity<String>(gson.toJson(JWTDemo.createJWT(id, issuer, subject, secret.toString(), ttlMillis)), HttpStatus.OK);			
						tokenJWT = new ResponseEntity<String>(gson.toJson(jwtDemo.createJWT(currentSecret[0].trim(), currentSecret[1].trim())), HttpStatus.OK);
						validateKeySuccess = true;
//						secretPrivateKey = currentSecret[1].trim();
					}	
					if(validateKeySuccess) {						
						break;
					}						
				}
				if(!validateKeySuccess) {
					//Throws back the failure response message					
					throw new ResponseStatusException(
					           HttpStatus.NOT_ACCEPTABLE, "Secret key is not valid");
				}
				else {
					validateKeySuccess = false;
				}
			} catch (GeneralSecurityException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new ResponseStatusException(
				           HttpStatus.NOT_ACCEPTABLE, "Secret key is not valid");
			} catch(Exception ex) {
				ex.printStackTrace();
				throw new ResponseStatusException(
				           HttpStatus.NOT_ACCEPTABLE, "Secret key is not valid");				
			}
			
			
			return tokenJWT;
		}
		else {			
			throw new ResponseStatusException(
			           HttpStatus.NOT_ACCEPTABLE, "Secret key is needed in header");
		}
		
	}
	@Autowired
	@Value("${app.jwt.secret}")
	private List<String> secret_keys;
	@PostMapping(path= "/validate", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> validateJWTToken(@RequestHeader("Authorization") String token, @RequestHeader("clientid")String client_Id){
		String secretPrivateKey = "";		
		try {
			if(token!=null && client_Id!=null ) {			
				try {
					for(String decodeSecret:secret_keys) {
						System.out.println("Inside for: ");
						System.out.println("decodeSecret: "+decodeSecret);
						if(decodeSecret.contains(client_Id.toUpperCase())) {
							String[] secretKey = decodeSecret.split("::");
							secretPrivateKey = secretKey[1].trim();
							break;
						}					
					}
					boolean returnValidation = jwtDemo.decodeJWT(token, secretPrivateKey, client_Id.toUpperCase());				
					if(returnValidation) {
						return new ResponseEntity<String>(gson.toJson(true), HttpStatus.OK);
					}
					else {			
						throw new ResponseStatusException(
						           HttpStatus.NOT_ACCEPTABLE, "Token is not valid");
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new ResponseStatusException(
					           HttpStatus.NOT_ACCEPTABLE, "Token validation failed");
				}
				
			}
			else {			
				throw new ResponseStatusException(
				           HttpStatus.NOT_ACCEPTABLE, "Both Authorization token & client id are needed in header");
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ResponseStatusException(
			           HttpStatus.NOT_ACCEPTABLE, "Missing key for decode");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ResponseStatusException(
			           HttpStatus.NOT_ACCEPTABLE, "Missing or wrong request header Authorization or clientid");
		}
		
		
	}
}

