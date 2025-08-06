package test.console.services.config.fakejwks;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;


public class GenerateLocalJwks {
    public static void main(String[] args) throws Exception {
        // Generate the RSA key pair
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair keyPair = gen.generateKeyPair();

        // Convert to JWK format
        RSAPrivateKey keyPairPrivate = (RSAPrivateKey) keyPair.getPrivate();
        RSAPublicKey keyPairPublic = (RSAPublicKey) keyPair.getPublic();
        JWK jwk = new RSAKey.Builder(keyPairPublic)
                .privateKey(keyPairPrivate)
                .keyUse(KeyUse.SIGNATURE)
                .keyID(UUID.randomUUID().toString())
                .build();

        System.out.println(jwk);

        String jwt = generateJWT(keyPairPrivate);

        Claims parsed = Jwts.parser()
                .setSigningKey(keyPairPublic)
                .parseClaimsJws(jwt)
                .getBody();
        System.out.println("parsed: " + parsed);

        // Bad key test case
        gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        keyPair = gen.generateKeyPair();
        try {
            Jwts.parser()
                    .setSigningKey(keyPair.getPublic())
                    .parseClaimsJws(jwt)
                    .getBody();
        } catch (SignatureException e) {
            // Normal case
        }
    }

    private static String generateJWT(RSAPrivateKey keyPairPrivate) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(Claims.SUBJECT, "bob.local@accor-dev.local");
        claims.put("ROLES", new String[] {"ROLE_USER"});
        claims.put("iss", "http://localhost:8083/ihm_api/public");
        claims.put("given_name", "Bob");
        claims.put("family_name", "Local");
        claims.put("email", "bob.local@accor-dev.local");
        claims.put("cognito:groups", "eu-west-1_vWX0Y3wMd_ActiveDirectory");
        claims.put("username", "ActiveDirectory_bob.local@accor-dev.local");
        claims.put("scope", "openid profile email");
        claims.put("custom:adgroups", Arrays.asList("user1", "WCC"));

        Date expiration = new Date(new Date().getTime() + 3600L * 24L * 5000L * 1000L);
        String jwt = Jwts.builder()
                .setClaims(claims)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.RS256, keyPairPrivate)
                .compact();
        System.out.println(jwt);
        return jwt;
    }
}
