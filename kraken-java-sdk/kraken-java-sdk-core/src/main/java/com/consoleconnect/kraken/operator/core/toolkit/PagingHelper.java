package com.consoleconnect.kraken.operator.core.toolkit;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.*;

public class PagingHelper {

  private PagingHelper() {}

  public static final int ALL = -1;
  public static final int DEFAULT_PAGE = 0;
  public static final int DEFAULT_SIZE = 20;

  public static final String DEFAULT_PAGE_STR = DEFAULT_PAGE + "";
  public static final String DEFAULT_SIZE_STR = DEFAULT_SIZE + "";

  public static <T> Paging<T> toPage(List<T> data, Integer page, Integer size) {
    return toPage(data, page, size, (long) data.size());
  }

  public static <T> Paging<T> toPage(List<T> data, Integer page, Integer size, Long total) {
    if (data == null) {
      data = Collections.emptyList();
    }

    if (page == null) {
      page = DEFAULT_PAGE;
    }
    if (size == null) {
      size = DEFAULT_SIZE;
    }
    var result = new Paging<T>();
    result.setData(toList(data, page, size));
    result.setSize(size);
    result.setPage(page);
    result.setTotal(total);
    return result;
  }

  public static <T> Paging<T> toPageNoSubList(
      List<T> data, Integer page, Integer size, Long total) {
    if (data == null) {
      data = Collections.emptyList();
    }

    if (page == null) {
      page = DEFAULT_PAGE;
    }
    if (size == null) {
      size = DEFAULT_SIZE;
    }
    var result = new Paging<T>();
    result.setData(data);
    result.setSize(size);
    result.setPage(page);
    result.setTotal(total);
    return result;
  }

  public static <T> Page<T> paginateList(List<T> data, PageRequest pageRequest) {
    int start = (int) pageRequest.getOffset();
    int end = Math.min(start + pageRequest.getPageSize(), data.size());
    List<T> paginatedList = (start < end) ? data.subList(start, end) : Collections.emptyList();
    return new PageImpl<>(paginatedList, pageRequest, data.size());
  }

  public static <T> List<T> toList(List<T> data, Integer page, Integer size) {
    if (page == null) {
      page = DEFAULT_PAGE;
    }
    if (size == null) {
      size = DEFAULT_SIZE;
    }

    if (data == null || data.isEmpty()) {
      return Collections.emptyList();
    }
    int fromIndex = page * size;
    int toIndex = fromIndex + size;
    if (fromIndex >= data.size()) {
      return Collections.emptyList();
    }
    if (toIndex > data.size()) {
      toIndex = data.size();
    }
    return data.subList(fromIndex, toIndex);
  }

  public static <T, I> Paging<T> toPaging(Page<I> entities, Function<I, T> func) {

    Paging<T> paging = new Paging<>();
    paging.setTotal(entities.getTotalElements());
    paging.setSize(entities.getSize());
    paging.setPage(entities.getNumber());
    paging.setData(entities.getContent().stream().map(func).toList());
    return paging;
  }

  public static <T, I> Paging<T> toPaging(List<I> entities, Function<I, T> func) {

    var paging = new Paging<T>();
    paging.setTotal((long) entities.size());
    paging.setSize(entities.size());
    paging.setPage(0);
    paging.setData(entities.stream().map(func).toList());
    return paging;
  }

  public static Pageable toPageable(int page, int size) {
    return toPageable(page, size, Sort.Direction.DESC, "createdAt");
  }

  public static Pageable toPageable(
      int page, int size, Sort.Direction direction, String... properties) {
    return PageRequest.of(page, size, direction, properties);
  }
}
