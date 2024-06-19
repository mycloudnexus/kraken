import StepBar from "@/components/StepBar";
import Text from "@/components/Text";
import { useAppStore } from "@/stores/app.store";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { EStep } from "@/utils/constants/common";
import { ROUTES } from "@/utils/constants/route";
import { LeftOutlined } from "@ant-design/icons";
import {
  Breadcrumb,
  BreadcrumbProps,
  Button,
  Flex,
  Tabs,
  TabsProps,
} from "antd";
import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import RequestMapping from "./components/RequestMapping";
import ResponseMapping from "./components/ResponseMapping";
import RightAddSonataProp from "./components/RightAddSonataProp";
import RightSelection from "./components/RightSelection";
import useGetApiSpec from "./components/useGetApiSpec";
import styles from "./index.module.scss";
import { EnumRightType } from "@/utils/types/common.type";

const NewAPIMapping = () => {
  const { componentId } = useParams();
  const { currentProduct } = useAppStore();
  const { query } = useNewApiMappingStore();
  const queryData = JSON.parse(query ?? "{}");
  const [activeKey, setActiveKey] = useState<string | string[]>("0");
  const [step] = useState(0);
  const [rightType, setRightType] = useState<EnumRightType | undefined>(
    EnumRightType.AddSonataProp
  );
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
  const items: TabsProps["items"] = [
    {
      key: "request",
      label: "Request mapping",
      children: <RequestMapping openRight={setRightType} />,
    },
    {
      key: "response",
      label: "Response mapping",
      children: <ResponseMapping />,
    },
  ];
  const { jsonSpec } = useGetApiSpec(currentProduct, query ?? "{}");

  return (
    <Flex vertical style={{ backgroundColor: "#f0f2f5", height: "100%" }}>
      <StepBar
        type={EStep.MAPPING}
        currentStep={step}
        activeKey={activeKey}
        setActiveKey={setActiveKey}
      />
      <Breadcrumb items={breadcrumb} className={styles.breadcrumb} />
      <Flex gap={20} className={styles.mainWrapper}>
        <div className={styles.center}>
          <Tabs items={items} />
        </div>
        <div className={styles.right}>
          {rightType === EnumRightType.AddSonataProp && (
            <RightAddSonataProp spec={jsonSpec} method={queryData?.method} />
          )}
          {rightType === EnumRightType.SelectSellerAPI && <RightSelection />}
        </div>
      </Flex>
      <Flex
        align="center"
        justify="flex-end"
        gap={8}
        className={styles.bottomWrapper}
      >
        <Button>Cancel</Button>
        <Button>Save and exit</Button>
        <Button type="primary" disabled>
          Next
        </Button>
      </Flex>
    </Flex>
  );
};

export default NewAPIMapping;
