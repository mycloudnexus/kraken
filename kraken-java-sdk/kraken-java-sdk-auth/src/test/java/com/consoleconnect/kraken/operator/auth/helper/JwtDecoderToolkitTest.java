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
      "bearer eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ0ZXN0LXN1YmplY3QiLCJzY3AiOlsibWVzc2FnZTpyZWFkIl0sImV4cCI6NDY4Mzg5Nzc3Nn0.LtMVtIiRIwSyc3aX35Zl0JVwLTcQZAB3dyBOMHNaHCKUljwMrf20a_gT79LfhjDzE_fUVUmFiAO32W1vFnYpZSVaMDUgeIOIOpxfoe9shj_uYenAwIS-_UxqGVIJiJoXNZh_MK80ShNpvsQwamxWEEOAMBtpWNiVYNDMdfgho9n3o5_Z7Gjy8RLBo1tbDREbO9kTFwGIxm_EYpezmRCRq4w1DdS6UDW321hkwMxPnCMSWOvp-hRpmgY2yjzLgPJ6Aucmg9TJ8jloAP1DjJoF1gRR7NTAk8LOGkSjTzVYDYMbCF51YdpojhItSk80YzXiEsv1mTz4oMM49jXBmfXFMA",
      "bearer eyJhbGciOiJSUzUxMiJ9.eyJzdWIiOiJ0ZXN0LXN1YmplY3QiLCJleHAiOjE5NzQzMjYxMTl9.LKAx-60EBfD7jC1jb1eKcjO4uLvf3ssISV-8tN-qp7gAjSvKvj4YA9-V2mIb6jcS1X_xGmNy6EIimZXpWaBR3nJmeu-jpe85u4WaW2Ztr8ecAi-dTO7ZozwdtljKuBKKvj4u1nF70zyCNl15AozSG0W1ASrjUuWrJtfyDG6WoZ8VfNMuhtU-xUYUFvscmeZKUYQcJ1KS-oV5tHeF8aNiwQoiPC_9KXCOZtNEJFdq6-uzFdHxvOP2yex5Gbmg5hXonauIFXG2ZPPGdXzm-5xkhBpgM8U7A_6wb3So8wBvLYYm2245QUump63AJRAy8tQpwt4n9MvQxQgS3z9R-NK92A"
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
