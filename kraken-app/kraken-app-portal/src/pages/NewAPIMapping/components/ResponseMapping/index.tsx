import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { IResponseMapping } from "@/utils/types/component.type";
import { Button, Flex } from "antd";
import { isEmpty } from "lodash";
import { nanoid } from "nanoid";
import { useEffect } from "react";
import ResponseItem from "../ResponseItem";
import styles from "./index.module.scss";

export interface IMapping {
  key: React.Key;
  from?: string;
  to?: string[];
  name?: string;
  groupId: string;
}

const ResponseMapping = () => {
  const {
    responseMapping,
    setListMappingStateResponse: setListMapping,
    setResponseMapping,
  } = useNewApiMappingStore();

  const handleAdd = () => {
    setResponseMapping([
      ...responseMapping,
      {
        title: "Title of Property Mapping",
        description: "description",
        customizedField: true,
        source: "",
        sourceLocation: "",
        target: "",
        targetLocation: "",
        id: nanoid(),
      } as IResponseMapping,
    ]);
  };

  useEffect(() => {
    if (isEmpty(responseMapping)) {
      setListMapping([]);
    }
  }, [responseMapping, setListMapping]);

  return (
    <div className={styles.root}>
      <Flex vertical gap={26}>
        {responseMapping?.map((item: IResponseMapping, index: number) => (
          <ResponseItem key={item.id} item={item} index={index} />
        ))}
        <Button
          type="link"
          style={{ width: "fit-content" }}
          onClick={handleAdd}
        >
          Add mapping property
        </Button>
      </Flex>
    </div>
  );
};

export default ResponseMapping;
