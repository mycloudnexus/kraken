import { render } from "@testing-library/react";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/utils/helpers/reactQuery";
import { BrowserRouter } from "react-router-dom";
import MappingDetailsList from "..";
import groupByPath from "@/utils/helpers/groupByPath";
test("test Mapping Details list", () => {
  const groupedPaths = groupByPath([
    {
      requiredMapping: true,
      targetKey: "mef.sonata.api-target.quote.eline.add",
      targetMapperKey: "mef.sonata.api-target-mapper.quote.eline.add",
      description: "This operation creates a Quote entity",
      path: "/api/son/x/v8/quote",
      method: "post",
      orderBy: "<1,1>",
      productType: "access_line",
      actionType: "add",
      mappingStatus: "incomplete",
      mappingMatrix: {
        quoteLevel: "firm",
        syncMode: false,
        productType: "access_e_line",
        actionType: "add",
      },
      updatedAt: "2021-07-22T08:21:00",
      lastDeployedAt: "2021-07-22 07:47:33",
      diffWithStage: true,
    },
    {
      requiredMapping: false,
      targetKey: "mef.son.api-target.quote.uni.read",
      targetMapperKey: "mef.sonata.api-target-mapper.quote.uni.read",
      description:
        "This operation retrieves a Quote entity. Attribute selection is enabled for all first level attributes.",
      path: "/api/son/x/v8/quote/{id}",
      method: "get",
      orderBy: "<2,1>",
      productType: "uni",
      mappingStatus: "incomplete",
      mappingMatrix: {
        quoteLevel: "firm",
        syncMode: false,
        productType: "access_e_line",
        actionType: "add",
      },
      updatedAt: "2024-07-18T01:59:05.410355Z",
      lastDeployedAt: "2024-07-22 07:53:50",
      diffWithStage: false,
    },
    {
      requiredMapping: true,
      targetKey: "mef.sonata.api-target.quote.eline.read",
      targetMapperKey: "mef.sonata.api-target-mapper.quote.eline.read",
      description:
        "This operation retrieves a Quote entity. Attribute selection is enabled for all first level attributes.",
      path: "/api/son/x/v8/quote/{id}",
      method: "get",
      orderBy: "<2,2>",
      productType: "access_line",
      mappingStatus: "incomplete",
      mappingMatrix: {
        quoteLevel: "firm",
        syncMode: false,
        productType: "access_e_line",
        actionType: "add",
      },
      updatedAt: "2024-07-18T01:59:05.714967Z",
      diffWithStage: true,
    },
    {
      requiredMapping: false,
      targetKey: "mef.sonata.api-target.quote.uni.add",
      targetMapperKey: "mef.sonata.api-target-mapper.quote.uni.add",
      description: "This operation creates a Quote entity",
      path: "/api/son/x/v8/quote",
      method: "post",
      orderBy: "<1,2>",
      productType: "uni",
      actionType: "add",
      mappingStatus: "incomplete",
      mappingMatrix: {
        quoteLevel: "firm",
        syncMode: false,
        productType: "access_e_line",
        actionType: "add",
      },
      updatedAt: "2024-07-22T08:13:45.808911Z",
      lastDeployedAt: "2024-07-22 07:26:09",
      diffWithStage: false,
    },
  ]);
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <MappingDetailsList
          groupedPaths={groupedPaths}
          setActiveSelected={() => {}}
        />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});
