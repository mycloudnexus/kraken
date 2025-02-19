package com.consoleconnect.kraken.operator.gateway.func;

import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.gateway.entity.HttpRequestEntity;
import com.consoleconnect.kraken.operator.gateway.repo.HttpRequestRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component("spelFunc")
public class SpelFunc {
  private final HttpRequestRepository httpRequestRepository;

  public SpelFunc(HttpRequestRepository httpRequestRepository) {
    this.httpRequestRepository = httpRequestRepository;
  }

  public static List<Object> appendSellerInformation(
      String role, String name, String emailAddress, String number, List<Object> list) {
    log.info("append seller information: name={}", name);
    Map<String, String> contact = new HashMap<>();
    contact.put("role", role);
    contact.put("name", name);
    contact.put("emailAddress", emailAddress);
    contact.put("number", number);
    list.add(contact);
    return list;
  }

  public String renderId(String sellerOrderId) {
    List<HttpRequestEntity> entities = httpRequestRepository.findByExternalId(sellerOrderId);
    if (CollectionUtils.isEmpty(entities)) {
      throw KrakenException.notFound("not found externalId in Kraken");
    }
    return entities.get(0).getId().toString();
  }
}
