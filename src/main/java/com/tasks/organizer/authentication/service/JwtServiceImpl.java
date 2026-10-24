package com.tasks.organizer.authentication.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.tasks.organizer.organization.context.OrgContext;
import com.tasks.organizer.organizationmember.entity.OrgPermissions;
import com.tasks.organizer.organizationmember.entity.OrgRoles;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${token.signing.key}")
    private String jwtSigningKey;

    @Value("${token.expiration}")
    private int EXPIRATION_TOKEN;

    @Override
    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return generateTokenIdentifiers(extraClaims, userDetails);
    }

    @Override
    public UUID extractUserId(String token) {
        String id = extractClaim(token, claims -> claims.get("id", String.class));
        return UUID.fromString(id);
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String userName = extractUserName(token);
        return (userName.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    public OrgContext extractOrgContext(String token) {
        Claims claims = extractAllClaims(token); // use whatever your service calls it
        String orgId = claims.get("organizationId", String.class);
        if (orgId == null) return null;

        OrgRoles role = OrgRoles.valueOf(claims.get("role", String.class));
        List<?> raw = claims.get("orgPermissions", List.class);
        Set<OrgPermissions> perms = raw == null ? Set.of()
            : raw.stream().map(Object::toString).map(OrgPermissions::valueOf).collect(Collectors.toSet());

        String userId = claims.get("id", String.class);

        return new OrgContext(UUID.fromString(userId), UUID.fromString(orgId), role, perms);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    private String generateTokenIdentifiers(Map<String, Object> extraClaims, UserDetails userDetails) {
        try{
            return Jwts.builder().setClaims(extraClaims).setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TOKEN))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
        }catch (Exception e){
            throw new JwtException("Error in generate token: [ " + e.getMessage() + " ]");
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token)
            .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
