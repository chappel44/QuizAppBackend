package com.tasks.organizer.authentication;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class HmacCryptoService {

    private final String secretKey;

    public HmacCryptoService(@Value("${app.security.token-secret}") String secretKey) {
      this.secretKey = secretKey;
    }

    public String hash(String rawToken) {
      try{
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(
          secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"
        );
        sha256Hmac.init(secretKeySpec);
        
        byte[] hashBytes = sha256Hmac.doFinal(rawToken.getBytes(StandardCharsets.UTF_8));
        
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
      } 
      catch (NoSuchAlgorithmException | InvalidKeyException e){
        throw new IllegalStateException("Failed to initialize HMAC-SHA256 cryptography", e);
      }
    }
}
