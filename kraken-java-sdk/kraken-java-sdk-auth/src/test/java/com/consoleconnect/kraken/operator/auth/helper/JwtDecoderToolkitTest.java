package com.consoleconnect.kraken.operator.auth.helper;

import com.consoleconnect.kraken.operator.auth.dto.JwtTokenDto;
import com.consoleconnect.kraken.operator.auth.jwt.JwtDecoderToolkit;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
class JwtDecoderToolkitTest {
  public static String[] illegalDataSetForDecoding() {
    return new String[] {"", "  ", "xxswewwew.sssss", "xxswewwew"};
  }

  public static String[] legalDataSetForDecoding() {
    return new String[] {
      "bearer eyJraWQiOiJrcmFrZW4iLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjb25zb2xlY29ubmVjdDIwMjQxMDI0IiwiaWF0IjoxNzI5NzU4MzEzLCJleHAiOjE3MzA2MjIzMTMsImlzcyI6Imh0dHBzOi8va3Jha2VuLmNvbnNvbGVjb25uZWN0LmNvbS9pc3N1ZXIifQ.8fP9SP_cJkcKNnZo-Fb3Qes7-5Ec_ztPp08dESd4fVQ"
    };
  }

  @ParameterizedTest
  @MethodSource(value = "illegalDataSetForDecoding")
  void givenIllegalToken_whenDecode_thenReturnEmpty(String token) {
    Optional<JwtTokenDto> result = JwtDecoderToolkit.decodeJWTToken(token);
    Assertions.assertTrue(result.isEmpty());
  }

  @ParameterizedTest
  @MethodSource(value = "legalDataSetForDecoding")
  void givenLegalJwtToken_whenDecode_thenReturnOK(String token) {
    Optional<JwtTokenDto> result = JwtDecoderToolkit.decodeJWTToken(token);
    Assertions.assertTrue(result.isPresent());
    Assertions.assertNotNull(result.get().getHeader());
    Assertions.assertNotNull(result.get().getPayload());
  }
}
