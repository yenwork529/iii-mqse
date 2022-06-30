package org.iii.esd.jwt.security;

import java.util.Date;
import java.util.List;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import org.iii.esd.api.vo.Payload;

@Component
@Log4j2
public class TokenProvider {

    private final String jwtSecret = "DTIisno1!@#";

    private final int jwtExpiration = 8 * 60 * 60 * 1000;

    //private final int jwtExpiration = 60*1000;	

    public String generateToken(Authentication authentication) {

        UserinfoDetails userinfoDetails = (UserinfoDetails) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        Claims claims = Jwts.claims();
        claims.setId(userinfoDetails.getUserId().toString());
        claims.setSubject(userinfoDetails.getUserName());
        claims.setIssuedAt(now);
        claims.setExpiration(expiryDate);
        claims.put("companyId", userinfoDetails.getCompanyId());
        claims.put("qseId", userinfoDetails.getQseId());
        claims.put("qseCode", userinfoDetails.getQseCode());
        claims.put("tgId", userinfoDetails.getTxgId());
        claims.put("tgCode", userinfoDetails.getTgCode());
        claims.put("serviceType", userinfoDetails.getServiceType());
        claims.put("resId", userinfoDetails.getResId());
        claims.put("resCode", userinfoDetails.getResCode());
        claims.put("resType", userinfoDetails.getResourceType());
        claims.put("roleIds", userinfoDetails.getRoles());

        return Jwts.builder()
                   .setClaims(claims)
                   .signWith(SignatureAlgorithm.HS512, jwtSecret)
                   .compact();
    }

    @SuppressWarnings("unchecked")
    public Payload getPayload(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token != null) {
            final Claims claims = getClaimsFromToken(getTokenFromRequest(request));
            return Payload.builder()
                          .companyId(claims.get("companyId", String.class))
                          .qseId(claims.get("qseId", String.class))
                          .qseCode(claims.get("qseCode", Integer.class))
                          .txgId(claims.get("tgId", String.class))
                          .tgCode(claims.get("tgCode", Integer.class))
                          .serviceType(claims.get("serviceType", Integer.class))
                          .resId(claims.get("resId", String.class))
                          .resCode(claims.get("resCode", Integer.class))
                          .resourceType(claims.get("resType", Integer.class))
                          .roles((List<Integer>) claims.get("roleIds", List.class))
                          .build();
        } else {
            return null;
        }
    }

    public String getEmailFromToken(String token) {
        return getClaimsFromToken(token, Claims::getSubject);
    }

    public Date getExpirationFromToken(String token) {
        return getClaimsFromToken(token, Claims::getExpiration);
    }

    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.");
        }
        return false;
    }

    public <T> T getClaimsFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    public String getTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            return token.substring(7);
        }

        return null;
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
    }

}