package com.consoleconnect.kraken.operator.core.toolkit;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

public class IpUtils {

  private IpUtils() {}

  private static final String IP_UNKNOWN = "unknown";
  private static final String IP_LOCAL = "127.0.0.1";
  private static final int IP_LEN = 15;

  public static String getIP(ServerHttpRequest request) {
    HttpHeaders headers = request.getHeaders();
    String ipAddress = headers.getFirst("x-forwarded-for");
    if (ipAddress == null || ipAddress.length() == 0 || IP_UNKNOWN.equalsIgnoreCase(ipAddress)) {
      ipAddress = headers.getFirst("Proxy-Client-IP");
    }
    if (ipAddress == null || ipAddress.length() == 0 || IP_UNKNOWN.equalsIgnoreCase(ipAddress)) {
      ipAddress = headers.getFirst("WL-Proxy-Client-IP");
    }
    if (ipAddress == null || ipAddress.length() == 0 || IP_UNKNOWN.equalsIgnoreCase(ipAddress)) {
      ipAddress =
          Optional.ofNullable(request.getRemoteAddress())
              .map(address -> address.getAddress().getHostAddress())
              .orElse("");
      if (IP_LOCAL.equals(ipAddress)) {
        return Constants.IP_LOCAL_HOSTNAME;
      }
    }
    if (ipAddress != null && ipAddress.length() > IP_LEN) {
      int index = ipAddress.indexOf(Constants.COMMA);
      if (index > 0) {
        ipAddress = ipAddress.substring(0, index);
      }
    }
    return ipAddress;
  }

  public static String getHostAddress() {
    try {
      return InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      return IP_UNKNOWN;
    }
  }
}
