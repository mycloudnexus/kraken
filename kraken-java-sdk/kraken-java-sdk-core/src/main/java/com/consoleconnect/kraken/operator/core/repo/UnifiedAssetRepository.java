package com.consoleconnect.kraken.operator.core.repo;

import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UnifiedAssetRepository
    extends PagingAndSortingRepository<UnifiedAssetEntity, UUID>,
        CrudRepository<UnifiedAssetEntity, UUID>,
        JpaRepository<UnifiedAssetEntity, UUID>,
        JpaSpecificationExecutor<UnifiedAssetEntity> {
  Optional<UnifiedAssetEntity> findOneByKey(String key);

  @Query(
      value =
          "select e from #{#entityName} e "
              + " where ( (:parentId) is null or  e.parentId = :parentId )"
              + " and ( (:kind) is null or  e.kind = :kind )"
              + " and ( (:q) is null or LOWER(e.name) like %:q% )")
  @Transactional(readOnly = true)
  Page<UnifiedAssetEntity> search(
      @Param("parentId") String parentId,
      @Param("kind") String kind,
      @Param("q") String q,
      Pageable pageable);

  List<UnifiedAssetEntity> findAllByParentId(String parentId);

  List<UnifiedAssetEntity> findAllByKeyIn(List<String> keys);

  List<UnifiedAssetEntity> findAllByIdIn(List<UUID> ids);

  Optional<UnifiedAssetEntity> findTopOneByParentIdAndKindOrderByCreatedAtDesc(
      String parentId, String kind);

  Optional<UnifiedAssetEntity> findTopOneByParentIdAndKindOrderByUpdatedAtDesc(
      String parentId, String kind);

  List<UnifiedAssetEntity> findByKindOrderByCreatedAtDesc(String kind);

  @Query(
      value =
          """
select e.* from kraken_asset e
where  ( (:kind) is null  or  e.kind = :kind )
and  ((:envId) is null or jsonb_extract_path_text(cast( e.labels as jsonb),'envId') =  :envId)
and  ((:releaseKind) is null or jsonb_extract_path_text(cast( e.labels as jsonb) ,'releaseKind') = :releaseKind)
and  ( (:status) is null or  e.status = :status )
and  ( (:tags) is null or cast( tags as jsonb) @> jsonb(:tags) or  cast( tags as jsonb) <@ jsonb(:tags))

""",
      nativeQuery = true)
  @Transactional(readOnly = true)
  Page<UnifiedAssetEntity> findDeployments(
      @Param("envId") String envId,
      @Param("status") String status,
      @Param("releaseKind") String releaseKind,
      @Param("kind") String kind,
      @Param("tags") String tags,
      Pageable pageable);

  @Query(
      value =
          """
        select e.* from kraken_asset e
        where ((:parentId) is null or e.parent_id = :parentId)
        and  ((:kind) is null  or  e.kind = :kind)
        and  ((:envId) is null or jsonb_extract_path_text(cast( e.labels as jsonb),'envId') =  :envId)
        and  ((:buyerId) is null or jsonb_extract_path_text(cast( e.labels as jsonb) ,'buyerId') = :buyerId)
        and  ((:status) is null or  e.status = :status)
        and  ((cast(:updatedAt as timestamp) is null) or e.updated_at >= :updatedAt)
        """,
      nativeQuery = true)
  @Transactional(readOnly = true)
  Page<UnifiedAssetEntity> findBuyers(
      @Param("parentId") String parentId,
      @Param("kind") String kind,
      @Param("envId") String envId,
      @Param("buyerId") String buyerId,
      @Param("status") String status,
      @Param("updatedAt") ZonedDateTime updatedAt,
      Pageable pageable);

  boolean existsByKey(String key);

  boolean existsByParentIdAndKind(String parentId, String kind);
}
