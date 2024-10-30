import { IEnv } from "@/utils/types/env.type";
import styles from "./index.module.scss";
import { Text } from "@/components/Text";
import { Button, notification } from "antd";
import { PRODUCT_CACHE_KEYS, useCreateApiKey } from "@/hooks/product";
import { useCallback, useRef } from "react";
import { useAppStore } from "@/stores/app.store";
import { showModalShowNew } from "../ModalShowAPIKey";
import dayjs from "dayjs";
import { get } from "lodash";
import { queryClient } from "@/utils/helpers/reactQuery";

type Props = {
  env?: IEnv;
};

const NoAPIKey = ({ env }: Props) => {
  const { currentProduct } = useAppStore();
  const { mutateAsync: createApiKeyMutate } = useCreateApiKey();
  const modalConfirmRef = useRef<any>();

  const generateApiKey = useCallback(
    async (envId: string, evName: string, closeConfirm = false) => {
      const name = `${evName}_${dayjs.utc().format("YYYY-MM-DD HH:mm:ss")}`;
      try {
        const res = await createApiKeyMutate({
          productId: currentProduct,
          name,
          envId,
        } as any);

        closeConfirm && modalConfirmRef?.current?.destroy();
        showModalShowNew(res?.data?.token);
        queryClient.invalidateQueries({
          queryKey: [PRODUCT_CACHE_KEYS.get_all_api_key, currentProduct],
        });
      } catch (e) {
        notification.error({ message: get(e, "reason", "generate failed") });
      }
    },
    [currentProduct]
  );
  return (
    <div className={styles.root}>
      <Text.NormalLarge>Connect to data plane</Text.NormalLarge>
      <div className={styles.content}>
        <Text.LightMedium color="#00000073">
          Please Create API Key for Data Plane to connect here!
        </Text.LightMedium>
      </div>
      <Button
        type="primary"
        onClick={() => {
          if (!env) return;
          generateApiKey(env.id, env.name);
        }}
      >
        Create API Key
      </Button>
    </div>
  );
};

export default NoAPIKey;
