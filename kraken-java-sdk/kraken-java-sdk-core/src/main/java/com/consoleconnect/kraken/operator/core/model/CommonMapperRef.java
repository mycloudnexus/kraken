package com.consoleconnect.kraken.operator.core.model;

import java.util.List;
import lombok.Data;

@Data
public class CommonMapperRef {
  private String ref;
  private List<KVPair> params;
}
