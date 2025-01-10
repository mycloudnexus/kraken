package com.consoleconnect.kraken.operator.core.service;

import static com.consoleconnect.kraken.operator.core.enums.AssetLinkKindEnum.*;

import com.consoleconnect.kraken.operator.core.dto.ApiUseCaseDto;
import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.model.AssetLink;
import com.consoleconnect.kraken.operator.core.toolkit.AssetRelationshipHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;

public interface ApiUseCaseSelector {

  UnifiedAssetService getUnifiedAssetService();

  default Map<String, List<Tuple2>> findApiUseCase() {
    // groupKey,<componentKey,assetLinkKind>
    Map<String, List<Tuple2>> result = new HashMap<>();
    getUnifiedAssetService().findByKind(AssetKindEnum.COMPONENT_API.getKind()).stream()
        .filter(component -> CollectionUtils.isNotEmpty(component.getLinks()))
        .forEach(
            component -> {
              Map<String, List<Tuple2>> groupMap =
                  component.getLinks().stream()
                      .filter(assetLink -> assetLink.getGroup() != null)
                      .collect(
                          Collectors.groupingBy(
                              AssetLink::getGroup,
                              Collectors.mapping(
                                  t -> Tuple2.of(t.getTargetAssetKey(), t.getRelationship()),
                                  Collectors.toList())));
              groupMap.forEach(
                  (key, members) -> {
                    members.add(
                        Tuple2.of(component.getMetadata().getKey(), COMPONENT_API.getKind()));
                    result.put(key, members);
                  });
            });
    return result;
  }

  default Optional<ApiUseCaseDto> findRelatedApiUse(
      String key, Map<String, List<Tuple2>> apiUseCaseMap) {
    return apiUseCaseMap.values().stream()
        .map(AssetRelationshipHelper::createReferences)
        .filter(t -> t.membersExcludeApiKey().contains(key))
        .findFirst();
  }
}
