import RequestItem from "@/pages/NewAPIMapping/components/RequestItem";
import ResponseItem from "@/pages/NewAPIMapping/components/ResponseItem";
import ResponseMapping from "@/pages/NewAPIMapping/components/ResponseMapping";
import RightAddSellerProp from "@/pages/NewAPIMapping/components/RightAddSellerProp";
import RightAddSonataProp, {
  getCorrectSpec,
} from "@/pages/NewAPIMapping/components/RightAddSonataProp";
import { APIItem } from "@/pages/NewAPIMapping/components/SelectAPI";
import SelectResponseProperty from "@/pages/NewAPIMapping/components/SelectResponseProperty";
import SonataPropMapping from "@/pages/NewAPIMapping/components/SonataPropMapping";
import SonataResponseMapping from "@/pages/NewAPIMapping/components/SonataResponseMapping";
import StatusIcon from "@/pages/NewAPIMapping/components/StatusIcon";
import { useMappingUiStore } from "@/stores/mappingUi.store";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import buildInitListMapping from "@/utils/helpers/buildInitListMapping";
import groupByPath from "@/utils/helpers/groupByPath";
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { fireEvent, render, renderHook } from "@testing-library/react";
import { omit } from "lodash";
import { BrowserRouter } from "react-router-dom";

beforeAll(() => {
  const { result: resultUiMapping } = renderHook(() => useMappingUiStore());
  resultUiMapping.current.setActivePath(
    "http://localhost:5173/component/mef.sonata.api.poq/new"
  );
  resultUiMapping.current.setSelectedKey("1");
  resultUiMapping.current.setActiveTab("/tab");

  const { result } = renderHook(() => useNewApiMappingStore());
  result.current.setActiveResponseName(
    `mapper.quote.uni.add.state-@{{responseBody.id}}"`
  );
  result.current.setResponseMapping([
    {
      name: "mapper.quote.uni.add.state",
      title: "Quote State Mapping",
      source: "@{{responseBody.id}}",
      target: "@{{quoteItem[*].state}}",
      targetType: "enum",
      description: "quote state mapping",
      targetValues: [
        "accepted",
        "acknowledged",
        "answered",
        "approved.orderable",
        "approved.orderableAlternate",
        "inProgress",
        "inProgress.draft",
        "abandoned",
        "rejected",
        "unableToProvide",
      ],
      valueMapping: {
        "1": "accepted",
        "2": "accepted",
        "3": "accepted",
        "4": "accepted",
        a: "inProgress.draft",
        b: "inProgress.draft",
        c: "inProgress.draft",
        d: "inProgress.draft",
        dd: "accepted",
        ee: "inProgress",
        fff: "inProgress",
      },
      sourceLocation: "BODY",
      targetLocation: "BODY",
      requiredMapping: true,
    },
    {
      name: "mapper.quote.uni.add.price.value",
      title: "Quote Price Value Mapping",
      source: "@{{responseBody.externalId}}",
      target: "@{{quoteItem.quoteItemPrice.price.dutyFreeAmount.value}}",
      description: "quote price mapping",
      sourceLocation: "BODY",
      targetLocation: "BODY",
      requiredMapping: true,
    },
    {
      name: "mapper.quote.uni.add.price.unit",
      title: "Quote Price Unit Mapping",
      source: "@{{responseBody.orderDate}}",
      target: "@{{quoteItem.quoteItemPrice.price.dutyFreeAmount.unit}}",
      description: "quote price mapping",
      sourceLocation: "BODY",
      targetLocation: "BODY",
      requiredMapping: true,
    },
    {
      name: "mapper.quote.uni.add.duration.amount",
      title: "Quote Duration Amount Mapping",
      source: "@{{responseBody.cancellationDate}}",
      target: "@{{quoteItem.requestedQuoteItemTerm.duration.amount}}",
      description: "quote duration amount mapping",
      sourceLocation: "BODY",
      targetLocation: "BODY",
      requiredMapping: true,
    },
    {
      name: "mapper.quote.uni.add.duration.units",
      title: "Quote Duration Units Mapping",
      source: "@{{responseBody.state}}",
      target: "@{{quoteItem[*].requestedQuoteItemTerm.duration.units}}",
      targetType: "enum",
      description: "quote duration units mapping",
      sourceLocation: "BODY",
      targetLocation: "BODY",
      requiredMapping: true,
    },
  ]);
  result.current.setListMappingStateResponse([
    {
      from: "accepted",
      to: ["1", "2", "3", "4", "dd"],
      key: 1,
      name: "mapper.quote.uni.add.state",
    },
    {
      from: "inProgress.draft",
      to: ["a", "b", "c", "d"],
      key: 2,
      name: "mapper.quote.uni.add.state",
    },
    {
      from: "inProgress",
      to: ["ee", "fff"],
      key: 3,
      name: "mapper.quote.uni.add.state",
    },
  ]);
  result.current.setSellerApi({
    name: "console connect application api",
    url: "/v2/pricing/l3vpn",
    method: "post",
    spec: {
      requestBody: {
        description: "",
        required: true,
        content: {
          "application/json": {
            schema: {
              type: "object",
              properties: {
                companyId: {
                  type: "",
                  description: "",
                },
                bandwidth: {
                  type: "string",
                  description: "",
                },
                duration: {
                  type: "object",
                  properties: {
                    value: {
                      type: "number",
                      description:
                        "The duration the service was ordered for, in conjunction with durationUnit. Max duration by unit is: days: 6; weeks: 3; month: 36; years: 3",
                    },
                    unit: {
                      type: "string",
                      enum: ["d", "w", "m", "y"],
                      description:
                        "The unit of duration the service was ordered for, in conjunction with duration. 'd' for days, 'w' for weeks, 'm' for months, 'y' for years.",
                    },
                  },
                  $$ref:
                    "http://localhost:5173/api-mapping/mef.sonata.api.serviceability.address/new#/components/schemas/Duration",
                },
                sites: {
                  anyOf: [
                    {
                      type: "object",
                      properties: {
                        priceRefId: {
                          type: "string",
                          description: "",
                        },
                        portId: {
                          type: "string",
                          description: "",
                        },
                        dcfId: {
                          type: "string",
                          description: "",
                        },
                        rateLimit: {
                          type: "object",
                          description: "",
                          properties: {
                            unit: {
                              type: "string",
                              description: "",
                            },
                            value: {
                              type: "number",
                              description: "",
                            },
                          },
                        },
                        classOfService: {
                          type: "object",
                          description: "",
                          properties: {
                            name: {
                              type: "string",
                              description: "",
                              enum: ["BRONZE", "SILVER", "GOLD"],
                            },
                            value: {
                              type: "number",
                              description: "",
                            },
                          },
                        },
                        discountId: {
                          type: "string",
                          description: "",
                        },
                      },
                      required: ["dcfId", "rateLimit", "classOfService"],
                      $$ref:
                        "http://localhost:5173/api-mapping/mef.sonata.api.serviceability.address/new#/components/schemas/CloudRouterPriceCalcSiteStandard",
                    },
                    {
                      type: "object",
                      properties: {
                        priceRefId: {
                          type: "string",
                          description: "",
                        },
                        simPool: {
                          type: "boolean",
                          description:
                            "If true, site will be processed as an IoT SIM Pool site",
                        },
                      },
                      required: ["simPool"],
                      $$ref:
                        "http://localhost:5173/api-mapping/mef.sonata.api.serviceability.address/new#/components/schemas/CloudRouterPriceCalcSiteSIMPool",
                    },
                  ],
                },
                discountCode: {
                  type: "string",
                  description: "",
                },
              },
              required: ["companyId", "bandwidth", "duration", "sites"],
            },
          },
        },
      },
      responses: {
        "200": {
          description: "OK",
          headers: {},
          content: {
            "application/json": {
              schema: {
                type: "object",
                properties: {
                  baseL3vpnCost: {
                    type: "number",
                    description: "",
                  },
                  totalContractCost: {
                    type: "number",
                    description: "",
                  },
                  duration: {
                    type: "object",
                    properties: {
                      value: {
                        type: "number",
                        description:
                          "The duration the service was ordered for, in conjunction with durationUnit. Max duration by unit is: days: 6; weeks: 3; month: 36; years: 3",
                      },
                      unit: {
                        type: "string",
                        enum: ["d", "w", "m", "y"],
                        description:
                          "The unit of duration the service was ordered for, in conjunction with duration. 'd' for days, 'w' for weeks, 'm' for months, 'y' for years.",
                      },
                    },
                    $$ref:
                      "http://localhost:5173/api-mapping/mef.sonata.api.serviceability.address/new#/components/schemas/Duration",
                  },
                  sites: {
                    type: "array",
                    items: {
                      type: "object",
                      properties: {
                        hourlyCost: {
                          type: "number",
                        },
                        dailyCost: {
                          type: "number",
                        },
                        monthlyCost: {
                          type: "number",
                        },
                        site: {
                          type: "object",
                          properties: {
                            priceRefId: {
                              type: "string",
                              description: "",
                            },
                            portId: {
                              type: "string",
                              description: "",
                            },
                            dcfId: {
                              type: "string",
                              description: "",
                            },
                            rateLimit: {
                              type: "object",
                              description: "",
                              properties: {
                                unit: {
                                  type: "string",
                                  description: "",
                                },
                                value: {
                                  type: "number",
                                  description: "",
                                },
                              },
                            },
                            classOfService: {
                              type: "object",
                              description: "",
                              properties: {
                                name: {
                                  type: "string",
                                  description: "",
                                  enum: ["BRONZE", "SILVER", "GOLD"],
                                },
                                value: {
                                  type: "number",
                                  description: "",
                                },
                              },
                            },
                            discountId: {
                              type: "string",
                              description: "",
                            },
                          },
                          required: ["dcfId", "rateLimit", "classOfService"],
                          $$ref:
                            "http://localhost:5173/api-mapping/mef.sonata.api.serviceability.address/new#/components/schemas/CloudRouterPriceCalcSiteStandard",
                        },
                      },
                    },
                  },
                  baseL3vpnCostAmountSaved: {
                    type: "number",
                    description: "",
                  },
                  totalContractCostAmountSaved: {
                    type: "number",
                    description: "",
                  },
                  discount: {
                    type: "object",
                    properties: {
                      discountType: {
                        type: "string",
                        description: "",
                      },
                      marketingCode: {
                        type: "string",
                        description: "",
                      },
                      duration: {
                        type: "string",
                        description: "",
                      },
                      applicableProducts: {
                        type: "array",
                        description: "",
                        items: {
                          type: "string",
                          enum: ["PORT", "L2", "L3VPN"],
                        },
                      },
                      redemptions: {
                        type: "number",
                        description: "",
                      },
                      value: {
                        type: "number",
                        description: "",
                      },
                      tierType: {
                        type: "string",
                        description: "",
                      },
                      id: {
                        type: "string",
                        description: "",
                      },
                      deletedAt: {
                        type: "string",
                        description: "",
                        readOnly: true,
                      },
                      createdAt: {
                        type: "string",
                        description: "",
                        readOnly: true,
                      },
                      updatedAt: {
                        type: "string",
                        description: "",
                        readOnly: true,
                      },
                    },
                    $$ref:
                      "http://localhost:5173/api-mapping/mef.sonata.api.serviceability.address/new#/components/schemas/Discount",
                  },
                },
              },
              examples: {
                "price-on-application": {
                  summary: "price on application",
                  value: {
                    totalContractCost: null,
                    baseL3vpnCost: null,
                    duration: {
                      unit: "m",
                      value: 6,
                    },
                    sites: [
                      {
                        site: {
                          dcfId: "6324134427d4717b6b226722",
                          rateLimit: {
                            unit: "mbps",
                            value: 100,
                          },
                          providerInfo: {
                            dcfName: "brisbaneDcf",
                            dcfId: "6324134427d4717b6b226722",
                            metroId: "6324134427d4717b6b226719",
                            city: "Brisbane",
                            country: "AUS",
                          },
                        },
                        priceRefId: 1,
                        prices: {
                          BRONZE: {
                            hourlyCost: null,
                            dailyCost: null,
                            monthlyCost: null,
                          },
                          SILVER: {
                            hourlyCost: null,
                            dailyCost: null,
                            monthlyCost: null,
                          },
                          GOLD: {
                            hourlyCost: null,
                            dailyCost: null,
                            monthlyCost: null,
                          },
                        },
                      },
                      {
                        site: null,
                        dcfId: "6324134427d4717b6b226723",
                        rateLimit: {
                          unit: "mbps",
                          value: 200,
                        },
                        providerInfo: {
                          dcfName: "londonDcf",
                          dcfId: "6324134427d4717b6b226723",
                          metroId: "6324134427d4717b6b22671a",
                          city: "London",
                          country: "UK",
                        },
                        priceRefId: 2,
                        prices: {
                          BRONZE: {
                            hourlyCost: null,
                            dailyCost: null,
                            monthlyCost: null,
                          },
                          SILVER: {
                            hourlyCost: null,
                            dailyCost: null,
                            monthlyCost: null,
                          },
                          GOLD: {
                            hourlyCost: null,
                            dailyCost: null,
                            monthlyCost: null,
                          },
                        },
                      },
                    ],
                    bandwidth: "500MB",
                  },
                },
                "with-sites": {
                  summary: "with sites",
                  value: {
                    baseL3vpnCost: 100,
                    totalContractCost: 200,
                    duration: {
                      "1": "m",
                    },
                    sites: [
                      {
                        site: null,
                        dcfId: "63241b384a42ce7e676819eb",
                        rateLimit: {
                          unit: "mbps",
                          value: 100,
                        },
                        providerInfo: {
                          dcfName: "brisbaneDcf",
                          dcfId: "63241b384a42ce7e676819eb",
                          metroId: "63241b384a42ce7e676819e2",
                          city: "Brisbane",
                          country: "AUS",
                        },
                        priceRefId: 1,
                        prices: {
                          BRONZE: {
                            hourlyCost: 0,
                            dailyCost: 0,
                            monthlyCost: 0,
                          },
                          SILVER: {
                            hourlyCost: 0,
                            dailyCost: 0,
                            monthlyCost: 0,
                          },
                          GOLD: {
                            hourlyCost: 0,
                            dailyCost: 0,
                            monthlyCost: 0,
                          },
                        },
                      },
                    ],
                    bandwidth: "500MB",
                  },
                },
                "with-sites-and-discount": {
                  value: {
                    summary: "with sites and discounts",
                    baseL3vpnCost: 100,
                    totalContractCost: 200,
                    duration: {
                      "1": "m",
                    },
                    sites: [
                      {
                        site: null,
                        dcfId: "63241b384a42ce7e676819eb",
                        rateLimit: {
                          unit: "mbps",
                          value: 100,
                        },
                        providerInfo: {
                          dcfName: "brisbaneDcf",
                          dcfId: "63241b384a42ce7e676819eb",
                          metroId: "63241b384a42ce7e676819e2",
                          city: "Brisbane",
                          country: "AUS",
                        },
                        priceRefId: 1,
                        prices: {
                          BRONZE: {
                            hourlyCost: 0,
                            dailyCost: 0,
                            monthlyCost: 0,
                          },
                          SILVER: {
                            hourlyCost: 0,
                            dailyCost: 0,
                            monthlyCost: 0,
                          },
                          GOLD: {
                            hourlyCost: 0,
                            dailyCost: 0,
                            monthlyCost: 0,
                          },
                        },
                      },
                    ],
                    bandwidth: "500MB",
                    baseL3vpnCostAmountSaved: 20,
                    totalContractCostAmountSaved: 120,
                    discount: {
                      discountType: "percentage",
                      marketingCode: "L3VPNDISCOUNT",
                      duration: "recurring",
                      applicableProducts: ["L3VPN"],
                      redemptions: 10,
                      value: 10,
                      tierType: "none",
                      id: "6327e43d773e329b467f1ee4",
                      deletedAt: null,
                      createdAt: "2022-09-19T03:38:37.812Z",
                      updatedAt: "2022-09-19T03:38:37.813Z",
                    },
                  },
                },
                "without-sites": {
                  summary: "without sites",
                  value: {
                    totalContractCost: null,
                    baseL3vpnCost: null,
                    duration: {
                      unit: "m",
                      value: 6,
                    },
                    sites: [],
                    bandwidth: "500MB",
                  },
                },
              },
            },
          },
        },
        "400": {
          description: "validations",
          content: {
            "application/json": {
              schema: {
                properties: {
                  message: {
                    type: "string",
                  },
                  status: {
                    type: "integer",
                  },
                  statusCode: {
                    type: "integer",
                  },
                },
                $$ref:
                  "http://localhost:5173/api-mapping/mef.sonata.api.serviceability.address/new#/components/schemas/ErrorResponseSchema",
              },
              examples: {
                "bandwidth-required": {
                  summary: "bandwidth required",
                  value: {
                    error: {
                      statusCode: 400,
                      name: "Error",
                      message: "bandwidth is a required argument",
                    },
                  },
                },
                "bandwidth-invalid": {
                  summary: "bandwidth invalid",
                  value: {
                    error: {
                      statusCode: 400,
                      name: "Error",
                      message:
                        "Bandwidth must be one of (500MB,1GB,2.5GB,5GB,10GB,>10GB)",
                    },
                  },
                },
                "duration-required": {
                  summary: "duration required",
                  value: {
                    error: {
                      statusCode: 400,
                      name: "Error",
                      message: "duration is a required argument",
                    },
                  },
                },
                "duration-invalid": {
                  summary: "duration invalid",
                  value: {
                    error: {
                      statusCode: 400,
                      name: "Error",
                      message:
                        'Duration should be in the format { unit: String, value: Number } where unit is one of \\"m\\" | \\"y\\" and value is a number greater than 0',
                    },
                  },
                },
                "duration-unit-invalid": {
                  summary: "duration unit invalid",
                  value: {
                    error: {
                      statusCode: 400,
                      name: "Error",
                      message:
                        'd is not a valid duration unit. Valid units are: \\"m\\", \\"y\\"',
                    },
                  },
                },
                "duration-value-must-be-positive": {
                  summary: "duration value must be positive",
                  value: {
                    error: {
                      statusCode: 400,
                      name: "Error",
                      message:
                        "-6 is not a positive integer. Duration value must be a positive integer",
                    },
                  },
                },
                "duration-value-invalid": {
                  summary: "duration value invalid",
                  value: {
                    error: {
                      statusCode: 400,
                      name: "Error",
                      message:
                        "100 is not a valid quantity for duration unit: m. The maximum quantity for this unit is 36",
                    },
                  },
                },
                "dcfId-required": {
                  summary: "dcfId required",
                  value: {
                    error: {
                      statusCode: 400,
                      name: "Error",
                      message: "dcfId is a required site field",
                    },
                  },
                },
                "rate-limit-not-valid": {
                  summary: "rate limit not valid",
                  value: {
                    error: {
                      statusCode: 400,
                      name: "Error",
                      message:
                        'Requested rateLimit is not valid. Please provide rateLimit in this format: {\\"unit\\": \\"Mbps\\", \\"value\\": 200}',
                    },
                  },
                },
                "rate-limit-must-be-a-positive-integer": {
                  summary: "rate limit must be a positive integer",
                  value: {
                    error: {
                      statusCode: 400,
                      name: "Error",
                      message:
                        "Requested rateLimit.value must be a number greater than 0",
                    },
                  },
                },
                "price-ref-id-must-a-string": {
                  summary: "price ref id must be a string",
                  value: {
                    error: {
                      statusCode: 400,
                      name: "Error",
                      message: "priceRefId must be a valid string",
                    },
                  },
                },
                "price-ref-id-must-be-unique": {
                  summary: "price ref id must be unique",
                  value: {
                    error: {
                      statusCode: 400,
                      name: "Error",
                      message: "priceRefId for each site must be unique",
                    },
                  },
                },
                "discount-code-invalid": {
                  summary: "discount code invalid",
                  value: {
                    error: {
                      statusCode: 400,
                      name: "Error",
                      message: "Invalid code.",
                    },
                  },
                },
              },
            },
          },
        },
        "401": {
          description: "unauthorized",
          content: {
            "application/json": {
              schema: {
                properties: {
                  message: {
                    type: "string",
                  },
                  status: {
                    type: "integer",
                  },
                  statusCode: {
                    type: "integer",
                  },
                },
                $$ref:
                  "http://localhost:5173/api-mapping/mef.sonata.api.serviceability.address/new#/components/schemas/ErrorResponseSchema",
              },
              examples: {
                "bandwidth-required": {
                  summary: "bandwidth required",
                  value: {
                    error: {
                      statusCode: 400,
                      name: "Error",
                      message: "You are not authorized to do that.",
                    },
                  },
                },
              },
            },
          },
        },
        "404": {
          description: "entity not found",
          content: {
            "application/json": {
              schema: {
                properties: {
                  message: {
                    type: "string",
                  },
                  status: {
                    type: "integer",
                  },
                  statusCode: {
                    type: "integer",
                  },
                },
                $$ref:
                  "http://localhost:5173/api-mapping/mef.sonata.api.serviceability.address/new#/components/schemas/ErrorResponseSchema",
              },
              examples: {
                "datacenter-facilities": {
                  summary: "datacenter facilities",
                  value: {
                    error: {
                      statusCode: 400,
                      name: "Error",
                      message:
                        "One or more requested datacenter facilities could not be found.",
                    },
                  },
                },
              },
            },
          },
        },
      },
      tags: ["Pricing"],
      description: "Find pricing for CloudRouters",
      operationId: "cloudrouter_pricing",
      summary: "CloudRouter",
      __originalOperationId: "cloudrouter-pricing",
      security: [
        {
          "api-key": [],
        },
      ],
    },
  });
});

test("groupPath fnc", () => {
  const result = groupByPath([
    {
      requiredMapping: true,
      targetKey: "mef.sonata.api-target.quote.eline.add",
      targetMapperKey: "mef.sonata.api-target-mapper.quote.eline.add",
      description: "This operation creates a Quote entity",
      path: "/api/son/x/v8/quote",
      method: "post",
      orderBy: "<1,2>",
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
      orderBy: "<2,2>",
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
      orderBy: "<2,1>",
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
      orderBy: "<1,1>",
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

  expect(result).toEqual({
    "/api/son/x/v8/quote": [
      {
        requiredMapping: false,
        targetKey: "mef.sonata.api-target.quote.uni.add",
        targetMapperKey: "mef.sonata.api-target-mapper.quote.uni.add",
        description: "This operation creates a Quote entity",
        path: "/api/son/x/v8/quote",
        method: "post",
        orderBy: "<1,1>",
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
      {
        requiredMapping: true,
        targetKey: "mef.sonata.api-target.quote.eline.add",
        targetMapperKey: "mef.sonata.api-target-mapper.quote.eline.add",
        description: "This operation creates a Quote entity",
        path: "/api/son/x/v8/quote",
        method: "post",
        orderBy: "<1,2>",
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
    ],
    "/api/son/x/v8/quote/{id}": [
      {
        requiredMapping: true,
        targetKey: "mef.sonata.api-target.quote.eline.read",
        targetMapperKey: "mef.sonata.api-target-mapper.quote.eline.read",
        description:
          "This operation retrieves a Quote entity. Attribute selection is enabled for all first level attributes.",
        path: "/api/son/x/v8/quote/{id}",
        method: "get",
        orderBy: "<2,1>",
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
        targetKey: "mef.son.api-target.quote.uni.read",
        targetMapperKey: "mef.sonata.api-target-mapper.quote.uni.read",
        description:
          "This operation retrieves a Quote entity. Attribute selection is enabled for all first level attributes.",
        path: "/api/son/x/v8/quote/{id}",
        method: "get",
        orderBy: "<2,2>",
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
    ],
  });
});

test("parse fnc", () => {
  const result = buildInitListMapping(
    [
      {
        name: "mapper.quote.uni.add.state",
        title: "Quote State Mapping",
        source: "@{{responseBody.id}}",
        target: "@{{quoteItem[*].state}}",
        targetType: "enum",
        description: "quote state mapping",
        targetValues: [
          "accepted",
          "acknowledged",
          "answered",
          "approved.orderable",
          "approved.orderableAlternate",
          "inProgress",
          "inProgress.draft",
          "abandoned",
          "rejected",
          "unableToProvide",
        ],
        valueMapping: {
          "1": "accepted",
          "2": "accepted",
          a: "inProgress.draft",
          b: "inProgress.draft",
          dd: "accepted",
          ee: "inProgress",
          fff: "inProgress",
        },
        sourceLocation: "BODY",
        targetLocation: "BODY",
        requiredMapping: true,
      },
    ],
    "response"
  );
  expect(result.map((value) => omit(value, "key"))).toEqual([
    {
      from: "accepted",
      name: "mapper.quote.uni.add.state",
      to: ["1", "2", "dd"],
    },
    {
      from: "inProgress.draft",
      name: "mapper.quote.uni.add.state",
      to: ["a", "b"],
    },
    {
      from: "inProgress",
      name: "mapper.quote.uni.add.state",
      to: ["ee", "fff"],
    },
  ]);
});

describe("select prop", () => {
  test("component new api map page", async () => {
    const { container, getByTestId, getAllByTestId } = render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <ResponseMapping />
          <SelectResponseProperty />
        </BrowserRouter>
      </QueryClientProvider>
    );
    expect(container).toBeInTheDocument();
    const element = getByTestId("btn-add-state");
    fireEvent.click(element);
    const select = getAllByTestId("select-sonata-state");
    expect(select.length).toBeGreaterThanOrEqual(1);
  });
  test("test ok btn", async () => {
    const { result } = renderHook(() => useNewApiMappingStore());
    result.current.setActiveResponseName(
      `mapper.quote.uni.add.state-@{{responseBody.id}}`
    );
    const { getByTestId, getByText } = render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <SelectResponseProperty />
        </BrowserRouter>
      </QueryClientProvider>
    );
    const treeItem = getByTestId("tree-item");
    expect(treeItem).toBeInTheDocument();
    const item = getByText("totalContractCost");
    fireEvent.click(item);
    const okButton = getByTestId("ok-btn");
    fireEvent.click(okButton);
  });
});

test("component RightAddSellerProp", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <RightAddSellerProp />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});

test("component RightAddSonataProp", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <RightAddSonataProp spec={undefined} method="GET" />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});

test("function getCorrectSpec", () => {
  const spec = {
    paths: {
      "/a/b/c/d": {
        get: {
          a: 1,
        },
        post: {
          a: 2,
        },
      },
    },
  };
  expect(getCorrectSpec(spec, "get")).toEqual({ a: 1 });
  expect(getCorrectSpec(spec, "post")).toEqual({ a: 2 });
});

test("APIItem", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <APIItem
          isOneItem={false}
          item={
            {
              kind: "kraken.component.api-target-spec",
              metadata: {
                id: "b7d7b3ee-f336-4066-97f9-23bd39ec1a82",
                name: "console connect 12",
                version: 0,
                key: "mef.sonata.api-target-spec.con1718940696857",
                description: "ABC",
              },
              facets: {
                environments: {},
                selectedAPIs: ["/productOrder/{id} patch"],
                baseSpec: {
                  path: "http://localhost:5173/component/mef.sonata.api.poq/new",
                },
              },
            } as any
          }
          setSellerApi={vi.fn()}
          selectedAPI={"ABC"}
          setSelectedServer={vi.fn()}
        />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});

test("SonataPropMapping", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <SonataPropMapping
          list={
            [
              {
                title: "Property mapping",
                source: "@{{query.buyerId}}",
                target: "@{{path.companyName}}",
                sourceLocation: "QUERY",
                targetLocation: "PATH",
              },
            ] as any
          }
          title={"Property mapping"}
        />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});

test("SonataResponseMapping", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <SonataResponseMapping spec={undefined} method={"GET"} />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});

// test("requestItem render", () => {
//   const { container, getByTestId } = render(
//     <QueryClientProvider client={queryClient}>
//       <BrowserRouter>
//         <RequestItem
//           index={2}
//           item={{
//             id: "id",
//             description: "",
//             name: "mapper.order.eline.add.duration.unit",
//             title: "order item Term unit",
//             source: "@{{productOrderItem[0].requestedItemTerm.duration.units}}",
//             target: "@{{requestBody.durationUnit}}",
//             sourceValues: [
//               "calendarMonths",
//               "calendarDays",
//               "calendarHours",
//               "calendarMinutes",
//               "businessDays",
//               "businessHours",
//             ],
//             valueMapping: {
//               calendarMonths: "calendarMonths",
//               calendarDays: "calendarDays",
//             },
//             sourceLocation: "BODY",
//             targetLocation: "BODY",
//             customizedField: true,
//             requiredMapping: true,
//           }}
//         />
//       </BrowserRouter>
//     </QueryClientProvider>
//   );
//   expect(container).toBeInTheDocument();
//   const btnAdd = getByTestId("btn-add-state");
//   fireEvent.click(btnAdd);
// });

describe("requestItem render", () => {
  it("add value mapping", () => {
    const { container, getByTestId } = render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <RequestItem
            index={2}
            item={{
              id: "id",
              description: "",
              name: "mapper.order.eline.add.duration.unit",
              title: "order item Term unit",
              source:
                "@{{productOrderItem[0].requestedItemTerm.duration.units}}",
              target: "@{{requestBody.durationUnit}}",
              sourceValues: [
                "calendarMonths",
                "calendarDays",
                "calendarHours",
                "calendarMinutes",
                "businessDays",
                "businessHours",
              ],
              valueMapping: {
                calendarMonths: "calendarMonths",
                calendarDays: "calendarDays",
              },
              sourceLocation: "BODY",
              targetLocation: "BODY",
              customizedField: true,
              requiredMapping: true,
            }}
          />
        </BrowserRouter>
      </QueryClientProvider>
    );
    expect(container).toBeInTheDocument();
    const btnAdd = getByTestId("btn-add-state");
    fireEvent.click(btnAdd);
  });
  it("delete request value mapping", () => {
    const { container, getByTestId } = render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <RequestItem
            index={2}
            item={{
              id: "id",
              description: "",
              name: "mapper.order.eline.add.duration.unit",
              title: "order item Term unit",
              source:
                "@{{productOrderItem[0].requestedItemTerm.duration.units}}",
              target: "@{{requestBody.durationUnit}}",
              sourceValues: [
                "calendarMonths",
                "calendarDays",
                "calendarHours",
                "calendarMinutes",
                "businessDays",
                "businessHours",
              ],
              valueMapping: {
                calendarMonths: "calendarMonths",
              },
              sourceLocation: "BODY",
              targetLocation: "BODY",
              customizedField: true,
              requiredMapping: true,
            }}
          />
        </BrowserRouter>
      </QueryClientProvider>
    );
    expect(container).toBeInTheDocument();
    const btnDeleteMapping = getByTestId("btn-req-delete-mapping-items");
    fireEvent.click(btnDeleteMapping);
  });
  it("add value limit integer discrete", () => {
    const { container, getByTestId } = render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <RequestItem
            index={2}
            item={{
              id: "id",
              description: "",
              name: "mapper.order.eline.add.duration.unit",
              title: "order item Term unit",
              source:
                "@{{productOrderItem[0].requestedItemTerm.duration.units}}",
              target: "@{{requestBody.durationUnit}}",
              valueMapping: {
                calendarMonths: "calendarMonths",
                calendarDays: "calendarDays",
              },
              sourceLocation: "BODY",
              targetLocation: "BODY",
              customizedField: true,
              requiredMapping: true,
              allowValueLimit: true,
              sourceType: "integer",
            }}
          />
        </BrowserRouter>
      </QueryClientProvider>
    );
    expect(container).toBeInTheDocument();
    const btnAdd = getByTestId("btn-add-valuelimit-int");
    fireEvent.click(btnAdd);
  });
  it("add value limit integer with sourceValue", () => {
    const { container } = render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <RequestItem
            index={2}
            item={{
              id: "id",
              description: "",
              name: "mapper.order.eline.add.duration.unit",
              title: "order item Term unit",
              source:
                "@{{productOrderItem[0].requestedItemTerm.duration.units}}",
              target: "@{{requestBody.durationUnit}}",
              sourceValues: ["0", "100"],
              valueMapping: {
                calendarMonths: "calendarMonths",
                calendarDays: "calendarDays",
              },
              sourceLocation: "BODY",
              targetLocation: "BODY",
              customizedField: true,
              requiredMapping: true,
              allowValueLimit: true,
              sourceType: "integer",
            }}
          />
        </BrowserRouter>
      </QueryClientProvider>
    );
    expect(container).toBeInTheDocument();
  });
  it("add value limit integer with sourceValue", () => {
    const { container } = render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <RequestItem
            index={2}
            item={{
              id: "id",
              description: "",
              name: "mapper.order.eline.add.duration.unit",
              title: "order item Term unit",
              source:
                "@{{productOrderItem[0].requestedItemTerm.duration.units}}",
              target: "@{{requestBody.durationUnit}}",
              sourceValues: ["0", "100"],
              valueMapping: {
                calendarMonths: "calendarMonths",
                calendarDays: "calendarDays",
              },
              sourceLocation: "BODY",
              targetLocation: "BODY",
              customizedField: true,
              requiredMapping: true,
              allowValueLimit: true,
              sourceType: "integer",
              discrete: true,
            }}
          />
        </BrowserRouter>
      </QueryClientProvider>
    );
    expect(container).toBeInTheDocument();
  });
  it("add value limit string with sourceValue", () => {
    const { container } = render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <RequestItem
            index={2}
            item={{
              id: "id",
              description: "",
              name: "mapper.order.eline.add.duration.unit",
              title: "order item Term unit",
              source:
                "@{{productOrderItem[0].requestedItemTerm.duration.units}}",
              target: "@{{requestBody.durationUnit}}",
              sourceValues: ["value1", "value2", "value3"],
              valueMapping: {
                calendarMonths: "calendarMonths",
                calendarDays: "calendarDays",
              },
              sourceLocation: "BODY",
              targetLocation: "BODY",
              customizedField: true,
              requiredMapping: true,
              allowValueLimit: true,
              sourceType: "string",
            }}
          />
        </BrowserRouter>
      </QueryClientProvider>
    );
    expect(container).toBeInTheDocument();
  });
});

describe("responseItem render", () => {
  it("add value mapping", () => {
    const { container, getByTestId } = render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <ResponseItem
            index={2}
            item={{
              id: "id",
              description: "",
              name: "mapper.order.eline.add.duration.unit",
              title: "order item Term unit",
              source:
                "@{{productOrderItem[0].requestedItemTerm.duration.units}}",
              target: "@{{requestBody.durationUnit}}",
              targetType: "enum",
              targetValues: [
                "calendarMonths",
                "calendarDays",
                "calendarHours",
                "calendarMinutes",
                "businessDays",
                "businessHours",
              ],
              valueMapping: {
                calendarMonths: "calendarMonths",
                calendarDays: "calendarDays",
              },
              sourceLocation: "BODY",
              targetLocation: "BODY",
              customizedField: true,
              requiredMapping: true,
            }}
          />
        </BrowserRouter>
      </QueryClientProvider>
    );
    expect(container).toBeInTheDocument();
    const btnAdd = getByTestId("btn-add-state");
    fireEvent.click(btnAdd);
  });
  it("delete response value mapping", () => {
    const { container, getByTestId } = render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <ResponseItem
            index={2}
            item={{
              id: "id",
              description: "",
              name: "mapper.order.eline.add.duration.unit",
              title: "order item Term unit",
              source:
                "@{{productOrderItem[0].requestedItemTerm.duration.units}}",
              target: "@{{requestBody.durationUnit}}",
              targetType: "enum",
              targetValues: [
                "calendarMonths",
                "calendarDays",
                "calendarHours",
                "calendarMinutes",
                "businessDays",
                "businessHours",
              ],
              valueMapping: {
                calendarMonths: "calendarMonths",
              },
              sourceLocation: "BODY",
              targetLocation: "BODY",
              customizedField: true,
              requiredMapping: true,
            }}
          />
        </BrowserRouter>
      </QueryClientProvider>
    );
    expect(container).toBeInTheDocument();
    const btnDeleteMapping = getByTestId("btn-resp-delete-mapping-items");
    fireEvent.click(btnDeleteMapping);
  });
});

describe("should render status icon component", () => {
  it("should render success status icon", () => {
    const { getByTestId } = render(<StatusIcon status="SUCCESS" />);
    const icon = getByTestId("deploymentStatus");
    expect(icon).toHaveStyle({ color: "#389E0D" });
  });

  it("should render failed status icon", () => {
    const { getByTestId } = render(<StatusIcon status="FAILED" />);
    const icon = getByTestId("deploymentStatus");
    expect(icon).toHaveStyle({ color: "#CF1322" });
  });

  it("should render loading status icon", () => {
    const { container } = render(<StatusIcon status="IN_PROGRESS" />);
    expect(container).toBeInTheDocument();
  });
});
