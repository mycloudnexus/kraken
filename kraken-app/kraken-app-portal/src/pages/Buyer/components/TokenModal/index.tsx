import Text from "@/components/Text";
import { IBuyer } from "@/utils/types/component.type";
import { Flex, Modal, Typography } from "antd";
import styles from "./index.module.scss";
import { get } from "lodash";
import { CopyOutlined } from "@ant-design/icons";

type Props = {
  open: boolean;
  onClose: () => void;
  item: IBuyer;
};

const TokenModal = ({ open, onClose, item }: Props) => {
  return (
    <Modal
      open={open}
      closable={false}
      className={styles.modal}
      okText="Done"
      onOk={onClose}
      onCancel={onClose}
    >
      <Flex vertical gap={4}>
        <Text.NormalLarge>Here’s your generated token</Text.NormalLarge>
        <Text.LightMedium>
          This token will be used by buyer to call Sonata API in the
          corresponding environment. Please copy to use it.
        </Text.LightMedium>
      </Flex>
      <Flex style={{ marginTop: 24 }} className={styles.token}>
        <Typography.Text
          className={styles.tokenText}
          ellipsis={{ tooltip: get(item, "buyerToken.accessToken", "") }}
          copyable={{
            icon: (
              <Flex align="center" justify="flex-end" gap={2}>
                <CopyOutlined />
                <Text.LightMedium>Copy</Text.LightMedium>
              </Flex>
            ),
          }}
        >
          {get(item, "buyerToken.accessToken", "")}
        </Typography.Text>
      </Flex>
    </Modal>
  );
};

export default TokenModal;
