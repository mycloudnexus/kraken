package com.consoleconnect.kraken.operator.workflow.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowDeploymentFacets {

  private Integer assetVersion;

  private Integer targetVersion;

  private String targetTag;
}
