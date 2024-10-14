package com.consoleconnect.kraken.operator.auth.security;

import com.consoleconnect.kraken.operator.auth.model.AuthDataProperty;
import java.util.Base64;
import java.util.UUID;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

public class KrakenPasswordEncoder implements PasswordEncoder {
  private final HmacUtils hmac;

  public KrakenPasswordEncoder(AuthDataProperty.Login login) {
    String secret = login.getHmacSecret();
    if (secret == null) {
      secret = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
    }
    hmac = new HmacUtils(HmacAlgorithms.HMAC_SHA_512, secret);
  }

  @Override
  @SneakyThrows
  public String encode(CharSequence rawPassword) {
    return hmac.hmacHex(rawPassword.toString());
  }

  @Override
  public boolean matches(CharSequence rawPassword, String encodedPassword) {
    return encode(rawPassword).equals(encodedPassword);
  }
}
