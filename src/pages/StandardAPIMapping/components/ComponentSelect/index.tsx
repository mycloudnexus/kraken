import { Flex, Select, Typography } from "antd";
import { useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { isEmpty } from "lodash";
import { useMappingUiStore } from "@/stores/mappingUi.store";
import ContactIcon from "@/assets/standardAPIMapping/contact.svg";
import { IComponent } from "@/utils/types/product.type";
import styles from "./index.module.scss";

type ComponentSelectProps = {
  componentList: any;
  componentName: string;
};

type LabelProps = {
  value: string;
};

const Label = ({ value }: LabelProps) => (
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

const ComponentSelect = ({ componentList, componentName }: ComponentSelectProps) => {
  const navigate = useNavigate();
  const { resetUiStore } = useMappingUiStore();

  const parsedOptions = useMemo(() => {
    if (!componentList?.data?.length) return [];
    return componentList.data
      .filter((i: IComponent) => !isEmpty(i?.facets?.supportedProductTypesAndActions))
      .filter((el: IComponent) => el.metadata.name !== componentName)
      .map((el: IComponent) => ({
        value: el.metadata.key,
        label: <Label value={el.metadata.name} />,
      }));
  }, [componentList, componentName]);

  const value = useMemo(() => ({
    value: componentName,
    label: <Label value={componentName} />,
  }), [componentName]);

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
      labelInValue
      options={parsedOptions}
    />
  );
};

export default ComponentSelect;
