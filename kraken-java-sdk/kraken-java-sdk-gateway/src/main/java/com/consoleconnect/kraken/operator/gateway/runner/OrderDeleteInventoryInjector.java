package com.consoleconnect.kraken.operator.gateway.runner;

import static com.consoleconnect.kraken.operator.core.toolkit.StringUtils.readWithJsonPath;

import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.dto.RenderedResponseDto;
import com.consoleconnect.kraken.operator.gateway.entity.HttpRequestEntity;
import com.consoleconnect.kraken.operator.gateway.repo.HttpRequestRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public interface OrderDeleteInventoryInjector {

  @Slf4j
  final class LogHolder {}

  String RESPONSE_ID = "$.entity.response.id";
  String INVENTORY_LIST_KEY = "listInventory";
  String ACT_TYPE_KEY = "actType";
  String PRODUCT_TYPE_KEY = "productType";
  String DELETE_KEY = "delete";
  String ADD_KEY = "add";
  String UNI_KEY = "uni";

  HttpRequestRepository getHttpRequestRepository();

  /**
   * handle delete order instanceId to add order id for inventory uni list
   *
   * @param inputs that from the last runner.
   */
  default void handleInventoryOfDeleteOrder(Map<String, Object> inputs) {
    // handle delete order instanceId to add order id for UNI
    Boolean inventoryListKey = (Boolean) inputs.getOrDefault(INVENTORY_LIST_KEY, Boolean.FALSE);
    String actType = (String) inputs.getOrDefault(ACT_TYPE_KEY, "");
    String productType = (String) inputs.getOrDefault(PRODUCT_TYPE_KEY, "");
    String instanceId = (String) readWithJsonPath(inputs, RESPONSE_ID);
    if (!canHandle(inventoryListKey, actType, productType)) {
      LogHolder.log.info("Not request for inventory list delete");
      return;
    }
    if (StringUtils.isBlank(instanceId)) {
      // step into mock response with empty array
      LogHolder.log.info("instanceId is blank");
      return;
    }
    // 1. select the created order id with this instanceId
    List<HttpRequestEntity> httpRequestEntities =
        getHttpRequestRepository().findByProductInstanceId(instanceId);
    Optional<HttpRequestEntity> entityOfAddOptional =
        httpRequestEntities.stream()
            .filter(
                item -> {
                  if (Objects.isNull(item.getRenderedResponse())) {
                    return false;
                  }
                  RenderedResponseDto renderedResponseDto =
                      JsonToolkit.fromJson(
                          JsonToolkit.toJson(item.getRenderedResponse()),
                          new TypeReference<RenderedResponseDto>() {});
                  if (CollectionUtils.isEmpty(renderedResponseDto.getProductOrderItem())) {
                    return false;
                  }
                  return ADD_KEY.equalsIgnoreCase(
                      renderedResponseDto.getProductOrderItem().get(0).getAction());
                })
            .findFirst();
    // 2. check this created order id exist or not
    if (entityOfAddOptional.isPresent()) {
      LogHolder.log.info("created order record exists for this instanceId:{}", instanceId);
      // replace entity and entity id in the inputs and go forward
      HttpRequestEntity httpRequestEntity = entityOfAddOptional.get();
      inputs.put("entity", httpRequestEntity);
      inputs.put("entity-id", httpRequestEntity.getId().toString());
    } else {
      // read product information of delete order from database and step into mock response
      Optional<HttpRequestEntity> entityOfDeleteOptional =
          httpRequestEntities.stream()
              .filter(
                  item -> {
                    if (Objects.isNull(item.getRenderedResponse())) {
                      return false;
                    }
                    RenderedResponseDto renderedResponseDto =
                        JsonToolkit.fromJson(
                            JsonToolkit.toJson(item.getRenderedResponse()),
                            new TypeReference<RenderedResponseDto>() {});
                    if (CollectionUtils.isEmpty(renderedResponseDto.getProductOrderItem())) {
                      return false;
                    }
                    return DELETE_KEY.equalsIgnoreCase(
                        renderedResponseDto.getProductOrderItem().get(0).getAction());
                  })
              .findFirst();
      LogHolder.log.info("created order record doesn't exist for this instanceId:{}", instanceId);
      if (entityOfDeleteOptional.isPresent()) {
        HttpRequestEntity httpRequestEntity = entityOfDeleteOptional.get();
      }
    }
  }

  default boolean canHandle(Boolean inventoryListKey, String actType, String productType) {
    return Objects.nonNull(inventoryListKey)
        && Boolean.TRUE.equals(inventoryListKey)
        && DELETE_KEY.equalsIgnoreCase(actType)
        && UNI_KEY.equalsIgnoreCase(productType);
  }
}
