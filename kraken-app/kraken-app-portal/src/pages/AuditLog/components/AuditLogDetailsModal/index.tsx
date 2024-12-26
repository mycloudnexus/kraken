import { Flex, Drawer, Table, Typography } from "antd";
import { Text } from "@/components/Text";
import styles from "./index.module.scss";
import { ILogActivity } from "@/utils/types/env.type";
import LogMethodTag from "@/components/LogMethodTag";
import TrimmedPath from "@/components/TrimmedPath";
import { CloseOutlined } from "@ant-design/icons";
import { get } from "lodash";

type Props = {
  open: boolean;
  onClose: () => void;
  item: ILogActivity;
};

const AuditLogDetailsModal = ({ open, onClose, item }: Props) => {
  if (!item) return null;

  const paramsTableData = () => {
    const hash = item.pathVariables as any;
    return Object.keys(hash).map((key: string) => ({
      name: key,
      value: hash[key],
    }));
  };

  return (
    <Drawer
      className={styles.modal}
      open={open}
      maskClosable
      onClose={onClose}
      footer={false}
      width="40vw"
      title={
        <Flex justify="space-between">
          <span>View details</span>
          <CloseOutlined onClick={onClose} />
        </Flex>
      }
    >
      <Flex vertical gap={8}>
        <Flex gap={12}>
          <LogMethodTag method={item?.method} />
          <TrimmedPath trimLevel={3} path={item?.path} />
        </Flex>
        <Text.LightSmall color="#00000073">
          {get(item, "description", "")}
        </Text.LightSmall>

        <Text.BoldMedium style={{ marginTop: 4 }}>Parameters</Text.BoldMedium>

        <Table
          style={{ marginTop: 12 }}
          rowKey={(item: any) => item.name}
          dataSource={paramsTableData()}
          columns={[
            {
              title: "Name",
              dataIndex: "name",
            },
            {
              title: "Value",
              dataIndex: "value",
            },
          ]}
          pagination={false}
        />
        <Text.BoldMedium>Request body:</Text.BoldMedium>
        <pre>
          <Typography.Text>
            {JSON.stringify(item?.request, undefined, 2)}
          </Typography.Text>
        </pre>
        <Text.BoldMedium>Response:</Text.BoldMedium>
        <pre>
          <Typography.Text>
            {JSON.stringify(item?.response, undefined, 2)}
          </Typography.Text>
        </pre>
      </Flex>
    </Drawer>
  );
};

export default AuditLogDetailsModal;
