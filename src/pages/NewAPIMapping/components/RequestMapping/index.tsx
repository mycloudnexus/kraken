import Text from "@/components/Text";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";

import { Collapse, CollapseProps } from "antd";
import { groupBy } from "lodash";
import SonataPropMapping from "../SonataPropMapping";
import styles from "./index.module.scss";

import { useEffect, useMemo, useState } from "react";
import { IRequestMapping } from "@/utils/types/component.type";

const RequestMapping = () => {
  const { requestMapping } = useNewApiMappingStore();
  const items: CollapseProps["items"] = useMemo(() => {
    if (requestMapping.length === 0) {
      return [
        {
          label: <Text.NormalLarge>Property mapping</Text.NormalLarge>,
          key: "Property mapping",
          children: <SonataPropMapping list={[]} title="Property mapping" />,
        },
      ];
    }
    const requestMappingGroupedByTitle = groupBy(
      requestMapping,
      (request) => request.title
    );
    return Object.entries(requestMappingGroupedByTitle).map(
      ([title, listMapping]) => ({
        key: title,
        label: <Text.NormalLarge>{title}</Text.NormalLarge>,
        children: (
          <SonataPropMapping
            list={listMapping as IRequestMapping[]}
            title={title}
          />
        ),
      })
    );
  }, [requestMapping]);
  const [activeKey, setActiveKey] = useState<string[]>([]);

  const handleChangeKey = (key: string | string[]) => {
    if (typeof key === "string") {
      setActiveKey([key]);
      return;
    }
    setActiveKey(key);
  };
  useEffect(() => {
    if (requestMapping.length === 0) {
      setActiveKey(["Property mapping"]);
      return;
    }
    setActiveKey(requestMapping.map((rm) => rm.title));
  }, [requestMapping]);
  return (
    <Collapse
      ghost
      items={items}
      activeKey={activeKey}
      onChange={handleChangeKey}
      className={styles.collapse}
    />
  );
};

export default RequestMapping;
