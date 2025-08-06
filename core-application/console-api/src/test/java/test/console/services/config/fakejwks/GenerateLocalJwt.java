package test.console.services.config.fakejwks;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.*;


public class GenerateLocalJwt {
    public static void main(String[] args) throws Exception {
        // From another previous private key
        JWKSet localKeys = JWKSet.load(GenerateLocalJwt.class.getResourceAsStream("/local/jwks.all.json"));
        System.out.println(localKeys);
        RSAKey rsaKey = localKeys.getKeys().get(0).toRSAKey();
        PrivateKey privateKey = rsaKey.toPrivateKey();
        generateJWTForBobXSS((RSAPrivateKey) privateKey);
        generateJWTForBobWCC((RSAPrivateKey) privateKey);
    }

    private static String generateJWTForBobXSS(RSAPrivateKey keyPairPrivate) {
        Map<String, Object> claims = new HashMap<>();
        String givenName = "Bob XSS";
        String email = "bob.local@accor-dev.local";
        List<String> azurAdGroups = Arrays.asList("user1", "XSS");

        return generateJWT(keyPairPrivate, claims, givenName, email, azurAdGroups);
    }

    private static String generateJWTForBobWCC(RSAPrivateKey keyPairPrivate) {
        Map<String, Object> claims = new HashMap<>();
        String givenName = "Bob WCC";
        String email = "bob.local@accor-dev.local";
        List<String> azurAdGroups = Arrays.asList("user1", "WCC");

        return generateJWT(keyPairPrivate, claims, givenName, email, azurAdGroups);
    }

    private static String generateJWT(RSAPrivateKey keyPairPrivate, Map<String, Object> claims, String givenName, String email, List<String> azurAdGroups) {
        String username = "ActiveDirectory" + email;
        claims.put(Claims.SUBJECT, email);
        claims.put("ROLES", new String[] {"ROLE_USER"});
        claims.put("iss", "http://localhost:8083/ihm_api/public");
        claims.put("given_name", givenName);
        claims.put("family_name", "Local");
        claims.put("email", email);
        claims.put("cognito:groups", "eu-west-1_vWX0Y3wMd_ActiveDirectory");
        claims.put("username", username);
        claims.put("scope", "openid profile email");
        claims.put("custom:adgroups", azurAdGroups);

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
