import LogMethodTag from "@/components/LogMethodTag";
import Text from "@/components/Text";
import { DoubleLeftOutlined } from "@ant-design/icons";
import { Button, Divider, Flex, List } from "antd";
import clsx from "clsx";
import { useState } from "react";
import { showModalConfirmCreateVersion } from "./components/ModalConfirmCreateVersion";
import styles from "./index.module.scss";

const listVersion = [
  {
    key: "current",
    label: "Current Mapping",
  },
];
const envAndVersion = [
  {
    env: "Dev",
    version: "0.10.0",
  },
  {
    env: "UAT",
    version: "0.10.0",
  },
  {
    env: "Stage",
  },
  {
    env: "Production",
  },
];
const mappings = [
  {
    id: "1",
    method: "POST",
    path: "/geographicAddressValidation",
    description: "Creates a geographicAddressValidation",
  },
  {
    id: "2",
    method: "GET",
    path: "/geographicAddress/{id}",
    description: "Retrieves a GeographicAddress entity",
  },
];
const StandardAPIMapping = () => {
  const [activeVersion, setActiveVersion] = useState("current");
  return (
    <Flex align="stretch" className={styles.pageWrapper}>
      <Flex vertical justify="space-between" className={styles.leftWrapper}>
        <List
          itemLayout="vertical"
          dataSource={listVersion}
          className={styles.list}
          renderItem={(item) => {
            const highlighted = activeVersion === item.key;
            return (
              <List.Item
                key={item.key}
                onClick={
                  !highlighted
                    ? () => {
                        setActiveVersion(item.key);
                      }
                    : undefined
                }
              >
                <div
                  className={clsx(styles.item, {
                    [styles.activeItem]: highlighted,
                  })}
                >
                  {item.label}
                </div>
              </List.Item>
            );
          }}
        />
        <Flex
          vertical
          align="center"
          gap={8}
          className={styles.leftBottomWrapper}
        >
          <Button type="primary">Create new version</Button>
          <Text.NormalSmall color="#bfbfbf">Version 1.0</Text.NormalSmall>
          <Divider style={{ margin: 0 }} />
          <Button className={styles.switcherBtn}>
            <DoubleLeftOutlined />
          </Button>
        </Flex>
      </Flex>
      <Flex vertical gap={12} className={styles.mainWrapper}>
        <Flex align="center" justify="space-between">
          <Text.Custom size="20">Standard API Mapping</Text.Custom>
          <Button
            type="primary"
            onClick={() => {
              showModalConfirmCreateVersion();
            }}
          >
            Create new version
          </Button>
        </Flex>
        <Flex align="center" justify="space-between">
          <Flex align="center" gap={24}>
            <Text.NormalLarge>Current configure</Text.NormalLarge>
            <Text.NormalMedium>
              Created at: 2024-05-15 04:34:56
            </Text.NormalMedium>
            <Text.NormalMedium>By user name</Text.NormalMedium>
          </Flex>
          <Flex align="center" gap={16}>
            {envAndVersion.map((item) => (
              <Flex key={item.env} gap={4} style={{ padding: "4px 8px" }}>
                <Text.NormalSmall>{item.env}</Text.NormalSmall>
                <Text.NormalSmall color="rgba(0,0,0,0.45)">
                  {item.version ?? "n/a"}
                </Text.NormalSmall>
              </Flex>
            ))}
          </Flex>
        </Flex>
        {mappings.map((mapping) => (
          <Flex
            align="center"
            justify="space-between"
            key={mapping.id}
            className={styles.mappingWrapper}
          >
            <Flex align="center" gap={16}>
              <LogMethodTag method={mapping.method} />
              <Text.NormalMedium>{mapping.path}</Text.NormalMedium>
              <Text.NormalMedium color="rgba(0,0,0,0.45)">
                {mapping.description}
              </Text.NormalMedium>
            </Flex>
            <Flex align="center" gap={8}>
              <Button>View</Button>
              <Button type="primary">Mapping</Button>
            </Flex>
          </Flex>
        ))}
      </Flex>
    </Flex>
  );
};

export default StandardAPIMapping;
