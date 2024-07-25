import { useNewApiMappingStore } from "@/stores/newApiMapping.store";

import { isEmpty } from "lodash";
import { useEffect } from "react";
import styles from "./index.module.scss";
import { IResponseMapping } from "@/utils/types/component.type";
import ResponseItem from "../ResponseItem";
import { Flex } from "antd";

export interface IMapping {
  key: number;
  from?: string;
  to?: string[];
  name?: string;
}

const ResponseMapping = () => {
  const { responseMapping, setListMappingStateResponse: setListMapping } =
    useNewApiMappingStore();

  useEffect(() => {
    if (isEmpty(responseMapping)) {
      setListMapping([]);
    }
  }, [responseMapping, setListMapping]);

  return (
    <div className={styles.root}>
      <Flex vertical gap={26}>
        {responseMapping?.map((item: IResponseMapping, index: number) => (
          <ResponseItem item={item} index={index} />
        ))}
      </Flex>
    </div>
  );
};

export default ResponseMapping;
