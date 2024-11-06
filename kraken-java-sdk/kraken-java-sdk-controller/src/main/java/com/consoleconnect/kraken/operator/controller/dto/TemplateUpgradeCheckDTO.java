package com.consoleconnect.kraken.operator.controller.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class TemplateUpgradeCheckDTO {
  Boolean compatible;
  Boolean mapperCompleted;
  Boolean newerTemplate;
  List<String> errorMessages = new ArrayList<>();
}
