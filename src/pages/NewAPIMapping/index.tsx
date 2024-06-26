import Flex from "@/components/Flex";
import StepBar from "@/components/StepBar";
import Text from "@/components/Text";
import { useUpdateTargetMapper } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { EStep } from "@/utils/constants/common";
import { ROUTES } from "@/utils/constants/route";
import { EnumRightType } from "@/utils/types/common.type";
import { LeftOutlined } from "@ant-design/icons";
import {
  Breadcrumb,
  BreadcrumbProps,
  Button,
  Tabs,
  TabsProps,
  notification,
} from "antd";
import { cloneDeep, get, isEmpty, uniqBy } from "lodash";
import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate, useParams } from "react-router-dom";
import RequestMapping from "./components/RequestMapping";
import ResponseMapping from "./components/ResponseMapping";
import RightAddSellerProp from "./components/RightAddSellerProp";
import RightAddSonataProp from "./components/RightAddSonataProp";
import SelectAPI from "./components/SelectAPI";
import SelectResponseProperty from "./components/SelectResponseProperty";
import useGetApiSpec from "./components/useGetApiSpec";
import styles from "./index.module.scss";

const NewAPIMapping = () => {
  const { componentId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  const { currentProduct } = useAppStore();
  const {
    query,
    rightSide,
    serverKey,
    requestMapping,
    responseMapping,
    rightSideInfo,
    sellerApi,
    setRequestMapping,
    setRightSide,
    setRightSideInfo,
    setResponseMapping,
    reset,
  } = useNewApiMappingStore();
  const { mutateAsync: updateTargetMapper, isPending } =
    useUpdateTargetMapper();
  const queryData = JSON.parse(query ?? "{}");
  const [activeKey, setActiveKey] = useState<string | string[]>("0");
  const [step] = useState(0);
  const breadcrumb: BreadcrumbProps["items"] = [
    {
      title: (
        <Link
          to={ROUTES.API_MAPPING(componentId!)}
          style={{ color: "rgba(0, 0, 0, 0.88)" }}
        >
          <LeftOutlined /> Standard API
        </Link>
      ),
    },
    {
      title: (
        <Text.NormalMedium color="rgba(0, 0, 0, 0.45)">
          Add new mapping
        </Text.NormalMedium>
      ),
    },
  ];
  const { jsonSpec, mappers, mapperResponse } = useGetApiSpec(
    currentProduct,
    query ?? "{}"
  );
  useEffect(() => {
    if (!requestMapping.length && mappers?.request?.length) {
      setRequestMapping(mappers?.request ?? []);
    }
  }, [mappers, requestMapping, setRequestMapping]);
  useEffect(() => {
    if (!isEmpty(mappers?.response) && isEmpty(responseMapping)) {
      setResponseMapping(mappers?.response);
    }
  }, [mappers?.response, responseMapping]);
  const [tabActiveKey, setTabActiveKey] = useState("request");
  const items: TabsProps["items"] = [
    {
      key: "request",
      label: "Request mapping",
      children: <RequestMapping />,
    },
    {
      key: "response",
      label: "Response mapping",
      children: <ResponseMapping />,
    },
  ];
  const handleSelectSonataProp = (selected: any) => {
    if (rightSideInfo?.method === "add") {
      const updatedMapping = uniqBy(
        [
          ...requestMapping,
          {
            sourceLocation: selected.location,
            source: selected.name,
            title: selected.title,
          },
        ],
        (rm) =>
          `${rm.source}_${rm.sourceLocation}_${rm.target}_${rm.targetLocation}`
      );
      setRequestMapping(updatedMapping);
    }
    if (rightSideInfo?.method === "update") {
      const updatedMapping = uniqBy(
        requestMapping.map((rm) => {
          if (
            rm.source === rightSideInfo?.previousData?.source &&
            rm.sourceLocation === rightSideInfo?.previousData?.sourceLocation
          ) {
            return {
              ...rm,
              source: selected.name,
              sourceLocation: selected.location,
            };
          }
          return rm;
        }),
        (rm) =>
          `${rm.source}_${rm.sourceLocation}_${rm.target}_${rm.targetLocation}`
      );
      setRequestMapping(updatedMapping);
    }
    setRightSideInfo(undefined);
    setRightSide(undefined);
  };
  const handleSelectSellerProp = (selected: any) => {
    const updatedMapping = uniqBy(
      requestMapping.map((rm) => {
        if (
          rm.target === rightSideInfo?.previousData?.target &&
          rm.targetLocation === rightSideInfo?.previousData?.targetLocation
        ) {
          return {
            ...rm,
            target: selected.name,
            targetLocation: selected.location,
          };
        }
        return rm;
      }),
      (rm) =>
        `${rm.source}_${rm.sourceLocation}_${rm.target}_${rm.targetLocation}`
    );
    setRequestMapping(updatedMapping);
    setRightSideInfo(undefined);
    setRightSide(undefined);
  };
  const handleCancel = () => {
    navigate(-1);
  };
  const handleNext = () => {
    setTabActiveKey("response");
  };
  const handlePrev = () => {
    setTabActiveKey("request");
  };
  const handleSave = async (isExit?: boolean) => {
    try {
      const data = cloneDeep(mapperResponse.data[0]);
      data.facets.endpoints[0] = {
        ...data.facets.endpoints[0],
        serverKey,
        method: sellerApi.method,
        path: sellerApi.url,
        mappers: {
          request: requestMapping,
          response: responseMapping,
        },
      };
      const res = await updateTargetMapper({
        productId: currentProduct,
        componentId: data.metadata.id,
        data,
      } as any);
      notification.success({ message: res.message });
      if (isExit) {
        navigate(-1);
      }
      return true;
    } catch (error) {
      notification.error({
        message: get(error, "message", "Error on creating/updating mapping"),
      });
    }
  };

  useEffect(() => {
    return () => {
      reset();
    };
  }, [location]);
  return (
    <Flex
      flexDirection="column"
      alignItems="stretch"
      justifyContent="flex-start"
      style={{ backgroundColor: "#f0f2f5", height: "100%" }}
    >
      <StepBar
        type={EStep.MAPPING}
        currentStep={step}
        activeKey={activeKey}
        setActiveKey={setActiveKey}
      />
      <Breadcrumb items={breadcrumb} className={styles.breadcrumb} />
      <Flex gap={20} className={styles.mainWrapper}>
        <div className={styles.center}>
          <Tabs
            items={items}
            activeKey={tabActiveKey}
            onChange={(ak) => setTabActiveKey(ak)}
          />
        </div>
        <div
          className={styles.right}
          style={
            rightSide === EnumRightType.SelectSellerAPI ? { padding: 0 } : {}
          }
        >
          {rightSide === EnumRightType.AddSonataProp && (
            <RightAddSonataProp
              spec={jsonSpec}
              method={queryData?.method}
              onSelect={handleSelectSonataProp}
            />
          )}
          {rightSide === EnumRightType.SelectSellerAPI && (
            <SelectAPI save={() => handleSave(false)} />
          )}
          {rightSide === EnumRightType.AddSellerProp && (
            <RightAddSellerProp onSelect={handleSelectSellerProp} />
          )}
          {rightSide === EnumRightType.AddSellerResponse && (
            <SelectResponseProperty />
          )}
        </div>
      </Flex>
      <Flex justifyContent="flex-end" gap={8} className={styles.bottomWrapper}>
        <Button type="text" onClick={handleCancel}>
          Cancel
        </Button>
        {tabActiveKey === "request" && (
          <Button type="primary" onClick={handleNext}>
            Next
          </Button>
        )}
        {tabActiveKey === "response" && (
          <>
            <Button onClick={handlePrev}>Previous</Button>
            <Button
              type="primary"
              onClick={() => handleSave(true)}
              loading={isPending}
            >
              Save and exit
            </Button>
          </>
        )}
      </Flex>
    </Flex>
  );
};

export default NewAPIMapping;
