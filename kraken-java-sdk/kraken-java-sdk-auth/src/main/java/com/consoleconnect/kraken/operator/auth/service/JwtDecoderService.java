package com.consoleconnect.kraken.operator.auth.service;

import com.consoleconnect.kraken.operator.auth.jwt.JwtDecoderToolkit;
import com.consoleconnect.kraken.operator.auth.model.AuthDataProperty;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

@Service
public class JwtDecoderService {

  private final Map<AuthDataProperty.JwtDecoderProperty, NimbusJwtDecoder> config2Decoder =
      new ConcurrentHashMap<>();

  public JwtDecoder getDecoder(AuthDataProperty.JwtDecoderProperty decodeConfig) {
    return this.config2Decoder.computeIfAbsent(
        decodeConfig, JwtDecoderToolkit::createJwtDecoderInstance);
  }
}
