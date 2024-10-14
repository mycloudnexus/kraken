package com.consoleconnect.kraken.operator.core.dto;

import java.util.List;
import lombok.Data;

@Data
public class ComposedHttpRequest {
  ApiActivityLog main;
  List<ApiActivityLog> branches;
}
