package com.hms.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.hms.model.UserAccount;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private final SecretKey signingKey;
	private final long accessExpirationMs;

	public JwtService(
			@Value("${hms.jwt.secret:}") String configuredSecret,
			@Value("${hms.jwt.access-expiration-ms:900000}") long accessExpirationMs) {
		this.accessExpirationMs = accessExpirationMs;
		if (configuredSecret != null && configuredSecret.length() >= 32) {
			this.signingKey = Keys.hmacShaKeyFor(configuredSecret.getBytes(StandardCharsets.UTF_8));
		} else {
			this.signingKey = Jwts.SIG.HS256.key().build();
		}
	}

	public String generateAccessToken(UserAccount user) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + accessExpirationMs);
		return Jwts.builder()
				.subject(user.getEmail())
				.claim("userId", user.getId())
				.claim("name", user.getName())
				.claim("role", user.getRole().name())
				.issuedAt(now)
				.expiration(expiry)
				.signWith(signingKey)
				.compact();
	}

	public Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(signingKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	public boolean isTokenValid(String token) {
		try {
			Claims claims = parseClaims(token);
			return claims.getExpiration().after(new Date());
		} catch (Exception ex) {
			return false;
		}
	}

	public long getAccessExpirationSeconds() {
		return accessExpirationMs / 1000L;
	}
}
