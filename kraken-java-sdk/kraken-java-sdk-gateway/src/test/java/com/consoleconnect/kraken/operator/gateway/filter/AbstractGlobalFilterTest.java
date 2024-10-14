package com.consoleconnect.kraken.operator.gateway.filter;

import static org.hamcrest.MatcherAssert.assertThat;

import com.consoleconnect.kraken.operator.core.toolkit.DataBufferUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

class AbstractGlobalFilterTest {

  @SneakyThrows
  @Test
  void testCopyDataBuffer() {
    List<String> encodingHeaders = new ArrayList<>();
    encodingHeaders.add("gzip");
    String str = "Hello World!";
    byte[] bytes = constructGzipBytes(str);

    DataBufferFactory bufferFactory = new DefaultDataBufferFactory();
    DataBuffer dataBuffer = bufferFactory.wrap(bytes);
    String convert2String = DataBufferUtil.copyDataBuffer(encodingHeaders, dataBuffer);
    assertThat(convert2String, Matchers.equalTo("Hello World!"));
  }

  @Test
  void testConvertString() throws IOException {
    MockServerRequest serverRequest =
        MockServerRequest.builder()
            .exchange(MockServerWebExchange.from(MockServerHttpRequest.get("/1233")))
            .build();
    serverRequest.exchange().getAttributes().put(HttpHeaders.CONTENT_ENCODING, "gzip");
    serverRequest
        .exchange()
        .getResponse()
        .getHeaders()
        .put(HttpHeaders.CONTENT_ENCODING, List.of("gzip"));
    String str = "Hello World!";
    byte[] bytes = constructGzipBytes(str);
    DataBufferFactory bufferFactory = new DefaultDataBufferFactory();
    DataBuffer dataBuffer = bufferFactory.wrap(bytes);
    String convert2String = DataBufferUtil.convert2String(dataBuffer, serverRequest.exchange());
    String convert2String2 = DataBufferUtil.convert2String(dataBuffer, serverRequest.exchange());
    assertThat(convert2String, Matchers.equalTo("Hello World!"));
    assertThat(convert2String2, Matchers.equalTo("Hello World!"));
  }

  private byte[] constructGzipBytes(String str) throws IOException {
    ByteArrayOutputStream obj = new ByteArrayOutputStream();
    GZIPOutputStream gzip = new GZIPOutputStream(obj);
    gzip.write(str.getBytes(StandardCharsets.UTF_8));
    gzip.close();
    return obj.toByteArray();
  }
}
