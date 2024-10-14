package com.consoleconnect.kraken.operator.core.model;

import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class UnifiedAsset {
  private String kind;
  private String apiVersion;
  private Metadata metadata;

  @JsonAlias("spec")
  private Map<String, Object> facets;

  private List<AssetLink> links;

  public static <T> T getFacets(UnifiedAsset asset, Class<T> classOfT) {
    return JsonToolkit.fromJson(JsonToolkit.toJson(asset.getFacets()), classOfT);
  }

  public static <T> T getFacets(UnifiedAsset asset, TypeReference<T> valueTypeRef) {
    return JsonToolkit.fromJson(JsonToolkit.toJson(asset.getFacets()), valueTypeRef);
  }

  public static UnifiedAsset of(String kind, String key, String name) {
    UnifiedAsset asset = new UnifiedAsset();
    asset.setKind(kind);
    asset.setApiVersion("v1");
    Metadata metadata = new Metadata();
    metadata.setKey(key);
    metadata.setName(name);
    metadata.setLabels(new HashMap<>());
    asset.setMetadata(metadata);
    asset.setFacets(new HashMap<>());
    return asset;
  }
}
