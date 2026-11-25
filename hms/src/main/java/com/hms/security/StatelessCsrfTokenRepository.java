package com.hms.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class StatelessCsrfTokenRepository implements CsrfTokenRepository {

	public static final String HEADER_NAME = "X-CSRF-TOKEN";
	private static final String PARAMETER_NAME = "_csrf";
	private static final String HMAC_ALGORITHM = "HmacSHA256";

	private final SecureRandom secureRandom = new SecureRandom();
	private final SecretKey signingKey;

	public StatelessCsrfTokenRepository() {
		byte[] keyBytes = new byte[32];
		secureRandom.nextBytes(keyBytes);
		this.signingKey = new SecretKeySpec(keyBytes, HMAC_ALGORITHM);
	}

	@Override
	public CsrfToken generateToken(HttpServletRequest request) {
		byte[] nonceBytes = new byte[16];
		secureRandom.nextBytes(nonceBytes);
		String nonce = Base64.getUrlEncoder().withoutPadding().encodeToString(nonceBytes);
		String signature = sign(nonce);
		return new DefaultCsrfToken(HEADER_NAME, PARAMETER_NAME, nonce + "." + signature);
	}

	@Override
	public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
		if (token != null) {
			response.setHeader(HEADER_NAME, token.getToken());
		}
	}

	@Override
	public CsrfToken loadToken(HttpServletRequest request) {
		String headerToken = request.getHeader(HEADER_NAME);
		if (!isValidToken(headerToken)) {
			return null;
		}
		return new DefaultCsrfToken(HEADER_NAME, PARAMETER_NAME, headerToken);
	}

	private boolean isValidToken(String token) {
		if (token == null || token.isBlank()) {
			return false;
		}
		int dotIndex = token.indexOf('.');
		if (dotIndex <= 0 || dotIndex == token.length() - 1) {
			return false;
		}
		String nonce = token.substring(0, dotIndex);
		String providedSignature = token.substring(dotIndex + 1);
		String expectedSignature = sign(nonce);
		return MessageDigest.isEqual(
				expectedSignature.getBytes(StandardCharsets.UTF_8),
				providedSignature.getBytes(StandardCharsets.UTF_8));
	}

	private String sign(String value) {
		try {
			Mac mac = Mac.getInstance(HMAC_ALGORITHM);
			mac.init(signingKey);
			byte[] signatureBytes = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
			return Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
		} catch (GeneralSecurityException ex) {
			throw new IllegalStateException("Falha ao assinar token CSRF.", ex);
		}
	}
}
