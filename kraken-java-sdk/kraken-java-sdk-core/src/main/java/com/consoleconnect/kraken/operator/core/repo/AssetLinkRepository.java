package com.consoleconnect.kraken.operator.core.repo;

import com.consoleconnect.kraken.operator.core.entity.AssetLinkEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface AssetLinkRepository
    extends PagingAndSortingRepository<AssetLinkEntity, UUID>,
        CrudRepository<AssetLinkEntity, UUID> {
  List<AssetLinkEntity> findAllByTargetAssetKeyAndRelationship(
      String targetAssetKey, String relationship);
}
