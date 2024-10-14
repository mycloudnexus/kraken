package com.consoleconnect.kraken.operator.core.toolkit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.server.ServerWebExchange;

public class DataBufferUtil {

  private DataBufferUtil() {}

  public static String convert2String(DataBuffer dataBuffer, ServerWebExchange exchange)
      throws IOException {
    List<String> encodingHeaders =
        exchange.getResponse().getHeaders().getOrEmpty(HttpHeaders.CONTENT_ENCODING);
    return copyDataBuffer(encodingHeaders, dataBuffer);
  }

  public static String copyDataBuffer(List<String> encodingHeaders, DataBuffer dataBuffer)
      throws IOException {
    if (encodingHeaders.contains("gzip")) {
      byte[] readBytes = new byte[dataBuffer.readableByteCount()];
      ByteBuffer byteBuffer = ByteBuffer.wrap(readBytes, 0, readBytes.length);
      dataBuffer.toByteBuffer(byteBuffer);
      ByteArrayInputStream bis = new ByteArrayInputStream(byteBuffer.array());
      GZIPInputStream gis = new GZIPInputStream(bis);
      byte[] bytes = FileCopyUtils.copyToByteArray(gis);
      return new String(bytes, StandardCharsets.UTF_8);
    } else {
      return dataBuffer.toString(Charset.defaultCharset());
    }
  }
}
