package com.consoleconnect.kraken.operator.auth.security;

import com.consoleconnect.kraken.operator.auth.model.AuthDataProperty;
import com.consoleconnect.kraken.operator.auth.service.JwtDecoderService;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Slf4j
public class AuthenticationManager implements ReactiveAuthenticationManager {

  private final AuthDataProperty.JwtDecoderProperty jwtDecoderProperty;
  private final JwtDecoderService decoderProvider;

  @Override
  public Mono<Authentication> authenticate(Authentication authentication) {
    String jwtToken = authentication.getCredentials().toString();
    JwtDecoder jwtDecoder = decoderProvider.getDecoder(jwtDecoderProperty);
    if (jwtDecoder == null) {
      log.warn("No jwtDecoder found,jwtDecoderProperty:{}", jwtDecoderProperty.getIssuer());
      return Mono.just(authentication);
    }

    Jwt jwt = null;
    try {
      jwt = jwtDecoder.decode(jwtToken);
    } catch (Exception e) {
      log.error("Error: {}", e.getMessage());
      authentication.setAuthenticated(false);
      return Mono.just(authentication);
    }

    List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
    @SuppressWarnings("unchecked")
    var roles = (List<String>) jwt.getClaims().get(UserContext.TOKEN_CLAIM_ROLES);
    if (roles != null) {
      for (String role : roles) {
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role));
      }
    }
    log.info("grantedAuthorities:{}", grantedAuthorities);
    return Mono.just(new JwtAuthenticationToken(jwt, grantedAuthorities));
  }
}
