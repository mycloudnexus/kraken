import { Flex, Select, Typography } from "antd";
import styles from "./index.module.scss";
import { useMemo } from "react";
import ContactIcon from "@/assets/standardAPIMapping/contact.svg";
import { useNavigate } from "react-router-dom";
import { IComponent } from "@/utils/types/product.type";
import { isEmpty } from "lodash";
import { useMappingUiStore } from "@/stores/mappingUi.store";

type ComponentSelectProps = {
  componentList: any;
  componentName: string;
};

const Label = ({ value }: { value: string }) => (
  <Flex
    style={{
      textOverflow: "ellipsis",
      overflow: "hidden",
      whiteSpace: "nowrap",
      flexWrap: "wrap",
    }}
    align="center"
    gap={10}
  >
    <div className={styles.componentIconWrapper}>
      <ContactIcon />
    </div>
    <Typography.Text ellipsis={{ tooltip: value }} style={{ maxWidth: 253 }}>
      {value}
    </Typography.Text>
  </Flex>
);

const ComponentSelect = ({
  componentList,
  componentName,
}: ComponentSelectProps) => {
  const navigate = useNavigate();

  const { resetUiStore } = useMappingUiStore();
  const parsedOptions = useMemo(() => {
    if (!componentList?.data?.length) return [];
    return componentList.data
      .filter(
        (i: IComponent) => !isEmpty(i?.facets?.supportedProductTypesAndActions)
      )
      .map((el: any) => ({
        value: el.metadata.key,
        label: <Label value={el.metadata.name} />,
      }));
  }, [componentList]);

  const value = {
    value: componentName,
    label: <Label value={componentName} />,
  };

  const handleSelect = (e: { value: string }) => {
    resetUiStore();
    navigate(`/api-mapping/${e.value}`);
  };
  return (
    <Select
      className={styles.selectStyle}
      onSelect={handleSelect}
      size="large"
      value={value}
      labelInValue={true}
      options={parsedOptions}
    />
  );
};

export default ComponentSelect;
