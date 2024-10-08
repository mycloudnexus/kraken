import Text from "@/components/Text";
import { IBuyer } from "@/utils/types/component.type";
import { Flex, Modal, Typography, notification } from "antd";
import styles from "./index.module.scss";
import { get } from "lodash";
import { CopyIcon } from "@/components/Icon";

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
          corresponding environment.
        </Text.LightMedium>
      </Flex>
      <Flex
        style={{ marginTop: 24 }}
        className={styles.token}
        align="flex-start"
        gap={12}
      >
        <Typography.Paragraph
          className={styles.tokenText}
          ellipsis={{
            rows: 4,
            tooltip: get(item, "buyerToken.accessToken", ""),
          }}
        >
          {get(item, "buyerToken.accessToken", "")}
        </Typography.Paragraph>
        <CopyIcon
          style={{ marginTop: 4 }}
          role="none"
          onClick={() => {
            if (!navigator) {
              return;
            }
            navigator.clipboard
              .writeText(get(item, "buyerToken.accessToken", ""))
              .then(() => {
                notification.success({ message: "Copied!" });
              });
          }}
        />
      </Flex>
    </Modal>
  );
};

export default TokenModal;
