import { Deployment } from "@/utils/types/product.type";

export function getUpgradeSteps(deployments: Deployment[]): Deployment[] {
  let controlPlane: Deployment = {} as Deployment,
    stage: Deployment = {} as Deployment,
    production: Deployment = {} as Deployment;

  for (const dep of deployments) {
    if (dep.envName === "CONTROL_PLANE") {
      controlPlane = dep;
    } else if (dep.envName === "stage") {
      stage = dep;
    } else if (dep.envName === "production") {
      production = dep;
    }
  }

  return [controlPlane, stage, production];
}

export const LONG_POLLING_TIME = 15 * 1000 // 15 secs
