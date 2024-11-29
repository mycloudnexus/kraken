package com.consoleconnect.kraken.operator.core;

import com.consoleconnect.kraken.operator.core.toolkit.IpUtils;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

class IpUtilsTest {
  @Test
  void givenRequest_whenGetIpAddress_thenReturnIpAddress() {
    MockServerHttpRequest request =
        MockServerHttpRequest.get("/123")
            .remoteAddress(new InetSocketAddress("localhost", 80))
            .build();
    String ip = IpUtils.getIP(request);
    Assertions.assertNotNull(ip);
  }

  @Test
  void givenECS_whenQueryFQDN_thenReturnSuccess() {
    String result = IpUtils.getFQDN();
    Assertions.assertNotEquals("unknown", result);
  }
}
