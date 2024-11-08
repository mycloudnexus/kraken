import { describe, it, expect, vi } from "vitest";
import * as service from "../../services/products";
import request from "@/utils/helpers/request";
import { PRODUCT } from "@/utils/constants/api";

// Mock request function
vi.mock("@/utils/helpers/request");

describe("Service Tests", () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it("should call getListComponents with correct parameters", async () => {
    const productId = "testProduct";
    const params = { key: "value" };
    await service.getListComponents(productId, params);

    expect(request).toHaveBeenCalledWith(`${PRODUCT}/${productId}/components`, {
      params,
    });
  });

  it("should call getListComponentsV2 with correct parameters", async () => {
    const productId = "testProduct";
    const targetMapperKey = "testKey";
    await service.getListComponentsV2(productId, targetMapperKey);

    expect(request).toHaveBeenCalledWith(`${PRODUCT}/${productId}/components/${targetMapperKey}`);
  });

  it("should call createNewComponent with correct parameters", async () => {
    const productId = "testProduct";
    const data = { name: "testComponent" };
    await service.createNewComponent(productId, data);

    expect(request).toHaveBeenCalledWith(`${PRODUCT}/${productId}/components`, {
      method: "POST",
      data,
    });
  });

  it("should call getListEnvs with correct parameters", async () => {
    const productId = "testProduct";
    await service.getListEnvs(productId);

    expect(request).toHaveBeenCalledWith(`${PRODUCT}/${productId}/envs`);
  });

  it("should call getComponentDetail with correct parameters", async () => {
    const productId = "testProduct";
    const componentId = "testComponent";
    await service.getComponentDetail(productId, componentId);

    expect(request).toHaveBeenCalledWith(`${PRODUCT}/${productId}/components/${componentId}`);
  });

  it("should call editComponentDetail with correct parameters", async () => {
    const productId = "testProduct";
    const componentId = "testComponent";
    const data = { description: "updated description" };
    await service.editComponentDetail(productId, componentId, data);

    expect(request).toHaveBeenCalledWith(`${PRODUCT}/${productId}/components/${componentId}`, {
      method: "PATCH",
      data,
    });
  });

  it("should call deployProduct with correct parameters", async () => {
    const productId = "testProduct";
    const envId = "testEnv";
    const data = { status: "deploy" };
    await service.deployProduct(productId, envId, data);

    expect(request).toHaveBeenCalledWith(`${PRODUCT}/${productId}/envs/${envId}/deployment`, {
      method: "POST",
      data,
    });
  });

  it("should call getAllApiKeyList with correct parameters", async () => {
    const productId = "testProduct";
    const params = { page: 1, limit: 10, size: 10 };
    await service.getAllApiKeyList(productId, params);

    expect(request).toHaveBeenCalledWith(`${PRODUCT}/${productId}/env-api-tokens`, {
      method: "GET",
      params,
    });
  });
});
