package com.consoleconnect.kraken.operator.core.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class ResponseTargetMapperDto {
  /** target location */
  private List<String> targetPathMapper = new ArrayList<>();

  /** <k, v> style k is the seller's state, v is mef state. */
  private Map<String, String> targetValueMapper = new HashMap<>();

  /** delete specified field when key filed is empty */
  private Map<String, String> targetCheckPathMapper = new HashMap<>();
}
