import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { render } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import DropdownOption from "../components/DropdownOption";

test("test Mapping Details list", () => {
  const optionProps = {
    targetKey: "mock_targetKey",
    targetMapperKey: "mock_targetMapperKey",
    description: "mock_description",
    path: "/path1",
    method: "post",
    mappingStatus: "complete",
    updatedAt: "updatedAt",
    updatedBy: "updatedBy",
    lastDeployedAt: "lastDeployedAt",
    lastDeployedBy: "lastDeployedBy",
    lastDeployedStatus: "SUCCESS",
    requiredMapping: true,
    diffWithStage: false,
    productType: "uni",
    actionType: "add",
    mappingMatrix: {
      productType: "uni",
      actionType: "add",
      provideAlternative: false,
      syncMode: true,
    },
    orderBy: "<1,1>",
    supportedCase: "supportedCase",
    runningMappingType: "runningMappingType",
    size: 2,
    value: "mock_value",
  };
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <DropdownOption {...optionProps} />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});
