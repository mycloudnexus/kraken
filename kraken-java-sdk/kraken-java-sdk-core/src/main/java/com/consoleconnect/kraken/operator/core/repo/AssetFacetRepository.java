package com.consoleconnect.kraken.operator.core.repo;

import com.consoleconnect.kraken.operator.core.entity.AssetFacetEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface AssetFacetRepository
    extends PagingAndSortingRepository<AssetFacetEntity, UUID>,
        JpaRepository<AssetFacetEntity, UUID> {}
