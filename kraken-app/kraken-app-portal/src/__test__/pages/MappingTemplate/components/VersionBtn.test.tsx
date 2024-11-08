import VersionBtn from "@/pages/MappingTemplate/components/VersionBtn";
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { render } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";

test(`VersionBtn component`, async () => {
  const item = {
    templateUpgradeId: "6ad80329-eadd-405b-837d-cca27f4219df",
    name: "V1.2.6",
    productVersion: "V1.2.6",
    productSpec: "grace",
    publishDate: "2024-09-11",
    description:
      "Add matrix validation:\n  add validation in address validation\nnew feature:\n",
    deployments: [
      {
        deploymentId: "c563511c-df0d-4cd3-95e4-8efd9d860e90",
        templateUpgradeId: "6ad80329-eadd-405b-837d-cca27f4219df",
        envName: "stage",
        productVersion: "V1.2.6",
        upgradeBy: "a24ed61b-ce25-42b4-b597-3bc9d22b56e5",
        status: "SUCCESS",
        createdAt: "2024-09-12T02:16:26.816835Z",
      },
      {
        deploymentId: "82a25162-f21c-4a78-83c5-0ccdca5d942f",
        templateUpgradeId: "6ad80329-eadd-405b-837d-cca27f4219df",
        envName: "production",
        productVersion: "V1.2.6",
        upgradeBy: "a24ed61b-ce25-42b4-b597-3bc9d22b56e5",
        status: "SUCCESS",
        createdAt: "2024-09-12T02:23:44.016666Z",
      },
    ],
    showStageUpgradeButton: true,
    showProductionUpgradeButton: true,
  };
  const item1 = {
    templateUpgradeId: "6ad80329-eadd-405b-837d-cca27f4219df",
    name: "V1.2.6",
    productVersion: "V1.2.6",
    productSpec: "grace",
    publishDate: "2024-09-11",
    description:
      "Add matrix validation:\n  add validation in address validation\nnew feature:\n",
    deployments: [],
    showStageUpgradeButton: true,
    showProductionUpgradeButton: true,
  };
  const item2 = {
    templateUpgradeId: "6ad80329-eadd-405b-837d-cca27f4219df",
    name: "V1.2.6",
    productVersion: "V1.2.6",
    productSpec: "grace",
    publishDate: "2024-09-11",
    description:
      "Add matrix validation:\n  add validation in address validation\nnew feature:\n",
    deployments: [
      {
        deploymentId: "c563511c-df0d-4cd3-95e4-8efd9d860e90",
        templateUpgradeId: "6ad80329-eadd-405b-837d-cca27f4219df",
        envName: "stage",
        productVersion: "V1.2.6",
        upgradeBy: "a24ed61b-ce25-42b4-b597-3bc9d22b56e5",
        status: "SUCCESS",
        createdAt: "2024-09-12T02:16:26.816835Z",
      },
    ],
    showStageUpgradeButton: true,
    showProductionUpgradeButton: true,
  };
  const item3 = {
    templateUpgradeId: "6ad80329-eadd-405b-837d-cca27f4219df",
    name: "V1.2.6",
    productVersion: "V1.2.6",
    productSpec: "grace",
    publishDate: "2024-09-11",
    description:
      "Add matrix validation:\n  add validation in address validation\nnew feature:\n",
    deployments: [
      {
        deploymentId: "c563511c-df0d-4cd3-95e4-8efd9d860e90",
        templateUpgradeId: "6ad80329-eadd-405b-837d-cca27f4219df",
        envName: "stage",
        productVersion: "V1.2.6",
        upgradeBy: "a24ed61b-ce25-42b4-b597-3bc9d22b56e5",
        status: "FAILED",
        createdAt: "2024-09-12T02:16:26.816835Z",
      },
    ],
    showStageUpgradeButton: true,
    showProductionUpgradeButton: true,
  };
  const { container } = render(
    <BrowserRouter>
      <QueryClientProvider client={queryClient}>
        <VersionBtn item={item as any} />
        <VersionBtn item={item1 as any} />
        <VersionBtn item={item2 as any} />
        <VersionBtn item={item3 as any} />
      </QueryClientProvider>
    </BrowserRouter>
  );
  expect(container).toBeInTheDocument();
});
