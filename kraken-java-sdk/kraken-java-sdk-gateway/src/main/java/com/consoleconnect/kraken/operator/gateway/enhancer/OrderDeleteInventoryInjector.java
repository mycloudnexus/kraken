package com.consoleconnect.kraken.operator.gateway.enhancer;

import static com.consoleconnect.kraken.operator.core.toolkit.StringUtils.readWithJsonPath;

import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.dto.RenderedResponseDto;
import com.consoleconnect.kraken.operator.gateway.dto.RoutingResultDto;
import com.consoleconnect.kraken.operator.gateway.entity.HttpRequestEntity;
import com.consoleconnect.kraken.operator.gateway.filter.KrakenFilterConstants;
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
  String INVENTORY_LIST_KEY = "inventory.list";
  String INVENTORY_LIST_OUTPUT_KEY = "inventoryList";
  String DELETE_KEY = "delete";
  String ADD_KEY = "add";

  HttpRequestRepository getHttpRequestRepository();

  /**
   * handle delete order instanceId to add order id for inventory list
   *
   * @param inputs that from the last runner.
   */
  default void handleInventoryOfDeleteOrder(
      Map<String, Object> inputs, RoutingResultDto routingResultDto, Map<String, Object> outputs) {
    // handle delete order instanceId to add order id for UNI
    Object obj = inputs.getOrDefault(INVENTORY_LIST_OUTPUT_KEY, null);
    if (Objects.isNull(obj)) {
      return;
    }
    Boolean isInventoryList = (Boolean) obj;
    String productAction = routingResultDto.getProductAction();
    String instanceId = (String) readWithJsonPath(inputs, RESPONSE_ID);
    if (!canHandle(isInventoryList, productAction, instanceId)) {
      LogHolder.log.info(
          "Not matched inventory list delete, isInventoryList:{}, productAction:{}, instanceId:{}",
          isInventoryList,
          productAction,
          instanceId);
      return;
    }
    // 1. select the created order id with this instanceId
    List<HttpRequestEntity> httpRequestEntities =
        getHttpRequestRepository().findByProductInstanceId(instanceId);
    Optional<HttpRequestEntity> entityOfAddOptional =
        filterRequestByProductAction(httpRequestEntities, ADD_KEY);
    // 2. check this created order id exist or not
    if (entityOfAddOptional.isPresent()) {
      // replace entity and entity id in the inputs and go forward
      LogHolder.log.info("created order record exists for this instanceId:{}", instanceId);
      HttpRequestEntity httpRequestEntity = entityOfAddOptional.get();
      outputs.put(KrakenFilterConstants.X_ENTITY, JsonToolkit.toJson(httpRequestEntity));
      outputs.put(KrakenFilterConstants.X_ENTITY_ID, httpRequestEntity.getId().toString());
    } else {
      // read product information of delete order from database and step into mock response
      Optional<HttpRequestEntity> entityOfDeleteOptional =
          filterRequestByProductAction(httpRequestEntities, DELETE_KEY);
      LogHolder.log.info("created order record doesn't exist for this instanceId:{}", instanceId);
      if (entityOfDeleteOptional.isPresent()) {
        routingResultDto.setForwardDownstream(false);
      }
    }
  }

  default boolean canHandle(Boolean isInventoryList, String actType, String instanceId) {
    return Objects.nonNull(isInventoryList)
        && Boolean.TRUE.equals(isInventoryList)
        && DELETE_KEY.equalsIgnoreCase(actType)
        && StringUtils.isNotBlank(instanceId);
  }

  default Optional<HttpRequestEntity> filterRequestByProductAction(
      List<HttpRequestEntity> httpRequestEntities, String filterKey) {
    return httpRequestEntities.stream()
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
              return filterKey.equalsIgnoreCase(
                  renderedResponseDto.getProductOrderItem().get(0).getAction());
            })
        .findFirst();
  }
}
