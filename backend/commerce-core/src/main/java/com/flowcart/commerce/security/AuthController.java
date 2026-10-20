package com.flowcart.commerce.security;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final JwtEncoder jwtEncoder;

	public AuthController(AuthenticationManager authenticationManager, JwtEncoder jwtEncoder) {
		this.authenticationManager = authenticationManager;
		this.jwtEncoder = jwtEncoder;
	}

	public record LoginRequest(String username, String password) {
	}

	public record LoginResponse(String accessToken, String tokenType, long expiresInSeconds, List<String> roles) {
	}

	@PostMapping("/login")
	public LoginResponse login(@RequestBody LoginRequest request) {
		try {
			Authentication auth = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.username(), request.password()));
			List<String> roles = auth.getAuthorities().stream()
					.map(GrantedAuthority::getAuthority)
					.map(a -> a.replace("ROLE_", ""))
					.collect(Collectors.toList());

			Instant now = Instant.now();
			long expiresIn = 3600;
			JwtClaimsSet claims = JwtClaimsSet.builder()
					.issuer("flowcart-ai")
					.issuedAt(now)
					.expiresAt(now.plusSeconds(expiresIn))
					.subject(auth.getName())
					.claim("roles", roles)
					.build();
			JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
			String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
			return new LoginResponse(token, "Bearer", expiresIn, roles);
		}
		catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
		}
	}
}
