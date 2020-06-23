package com.example.demo;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
//import javax.xml.bind.DatatypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPrivateKeySpec;

import io.jsonwebtoken.*;

import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/*
    Our simple static class that demonstrates how to create and decode JWTs.
 */

@Service
public class JWTDemo {	
	@Autowired
	private Environment environment;
    //Sample method to construct a JWT
//    public static String createJWT(String id, String issuer, String subject, String SECRET_KEY, long ttlMillis) {
	

	public String createJWT(String clientId, String SECRET_KEY) {		
		
        System.out.println("Inside createJWT");
        
        String id = environment.getRequiredProperty("app."+clientId.toUpperCase()+".id");
        System.out.println("createJWT clientId: " + id);
        String issuer = environment.getProperty("app."+clientId.toUpperCase()+".issuer");
        System.out.println("createJWT client user: " + issuer);
        String subject = environment.getProperty("app."+clientId.toUpperCase()+".subject");
        System.out.println("createJWT client subject: " + subject);
        long ttlMillis = Long.parseLong(environment.getProperty("app."+clientId.toUpperCase()+".timeinmili"));
        System.out.println("createJWT client ttlMillis: " + String.valueOf(ttlMillis));
        
    	//The JWT signature algorithm we will be using to sign the token
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS512;

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        //We will sign our JWT with our ApiKey secret
//        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(SECRET_KEY);
//        byte[] apiKeySecretBytes = Base64.getDecoder().decode(SECRET_KEY);
        byte[] apiKeySecretBytes = SECRET_KEY.getBytes();
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        //Let's set the JWT Claims
        JwtBuilder builder = Jwts.builder().setId(id)
                .setIssuedAt(now)
                .setSubject(subject)
                .setIssuer(issuer)
                .signWith(signatureAlgorithm, signingKey);
        System.out.println("Before ttMillis");
        //if it has been specified, let's add the expiration
        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }
        System.out.println("After ttMillis");
        //Builds the JWT and serializes it to a compact, URL-safe string
        System.out.println("Token: "+builder.compact());
        return builder.compact();
    }

    public boolean decodeJWT(String jwt,String SECRET_KEY, String clientId) {

        try {
        	System.out.println("Inside decodeJWT");
        	System.out.println("decodeJWT clientId: " + clientId);
        	System.out.println("decodeJWT SECRET_KEY: " + SECRET_KEY);
        	
			//This line will throw an exception if it is not a signed JWS (as expected)
			
			  Claims claims = Jwts.parser()
			  .setSigningKey(Base64.getDecoder().decode(SECRET_KEY))
			  .parseClaimsJws(jwt).getBody();
			 
        	
			/*
			 * Claims claims = Jwts.parser()
			 * .setSigningKey(DatatypeConverter.parseBase64Binary(SECRET_KEY))
			 * .parseClaimsJws(jwt).getBody();
			 */
			
			String id = environment.getProperty("app."+clientId.toUpperCase()+".id");
			System.out.println("decodeJWT id: " + id);
			System.out.println("decodeJWT claims id: " + claims.getId());
			String issuer = environment.getProperty("app."+clientId.toUpperCase()+".issuer");
			System.out.println("decodeJWT issuer: " + issuer);
			System.out.println("decodeJWT claims issuer: " + claims.getIssuer());
			String subject = environment.getProperty("app."+clientId.toUpperCase()+".subject"); 
			System.out.println("decodeJWT subject: " + subject);        
			System.out.println("decodeJWT claims subject: " + claims.getSubject());
			
			if(claims.getId().equals(id)
					&& claims.getSubject().equals(subject)
					&& claims.getIssuer().equals(issuer)
					&& claims.getExpiration().after(new Date())) {
				return true;
			}
			else {
				return false;
			}
		} catch (ExpiredJwtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (UnsupportedJwtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (MalformedJwtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}         
    }
    
    public static boolean validateRSA(PublicKey publicKey, PrivateKey privateKey) {
    	System.out.println("Inside validateRSA");
//    	System.out.println("publicKey: " + publicKey.toString());
//    	System.out.println("privateKey: " + privateKey.toString());      	
    	
    	KeyPairGenerator keyGen = null;
    	boolean keyPairMatches = false;
		try {
			keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(2048);
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	

//    	KeyPair keyPair = keyGen.generateKeyPair();
//    	PublicKey publicKey = keyPair.getPublic();
//    	PrivateKey privateKey = keyPair.getPrivate();

    	// create a challenge
    	byte[] challenge = new byte[10000];
    	ThreadLocalRandom.current().nextBytes(challenge);

    	// sign using the private key
    	Signature sig;
		try {
			sig = Signature.getInstance("SHA256withRSA");
			sig.initSign(privateKey);
	    	sig.update(challenge);
	    	byte[] signature = sig.sign();

	    	// verify signature using the public key
	    	sig.initVerify(publicKey);
	    	sig.update(challenge);

	    	keyPairMatches = sig.verify(signature);
	    	
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;			
		}    	
    	return keyPairMatches;
    	
    }
    
    public static PublicKey loadPublicKey(String strPublicKey) throws GeneralSecurityException, IOException {
    	   try {
			System.out.println("Inside loadPublicKey");
			   byte[] data = Base64.getDecoder().decode((strPublicKey.getBytes()));
			   X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
			   KeyFactory fact = KeyFactory.getInstance("RSA");
			   return fact.generatePublic(spec);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

     }

    public static PrivateKey loadPrivateKey(String strPrivateKey) throws GeneralSecurityException, IOException {
    	 try {
			System.out.println("Inside loadPrivateKey");
			 byte[] clear = Base64.getDecoder().decode(strPrivateKey.getBytes());
//	     System.out.println("Inside clear: " + clear);
			 PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(clear);
//	     RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec();
			 KeyFactory fact = KeyFactory.getInstance("RSA");
			 PrivateKey priv = fact.generatePrivate(keySpec);
			 Arrays.fill(clear, (byte) 0);
			 System.out.println("End loadPrivateKey");
			 return priv;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	
	}
 

}
