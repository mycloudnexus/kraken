import Text from "@/components/Text";
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
import { Link, useParams } from "react-router-dom";
import styles from "./index.module.scss";
import RequestMapping from "./components/RequestMapping";
import ResponseMapping from "./components/ResponseMapping";

const NewAPIMapping = () => {
  const { componentId } = useParams();
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
      children: <RequestMapping />,
    },
    {
      key: "response",
      label: "Response mapping",
      children: <ResponseMapping />,
    },
  ];
  return (
    <Flex vertical style={{ backgroundColor: "#f0f2f5", height: "100%" }}>
      <Breadcrumb items={breadcrumb} className={styles.breadcrumb} />
      <Flex gap={20} className={styles.mainWrapper}>
        <div className={styles.center}>
          <Tabs items={items} />
        </div>
        <div className={styles.left}>Left data</div>
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