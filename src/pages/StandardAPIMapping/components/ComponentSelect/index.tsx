import { Flex, Select, Typography } from "antd";
import { useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { isEmpty } from "lodash";
import { useMappingUiStore } from "@/stores/mappingUi.store";
import ContactIcon from "@/assets/standardAPIMapping/contact.svg";
import OrderIcon from "@/assets/standardAPIMapping/order.svg";
import InventoryIcon from "@/assets/standardAPIMapping/inventory.svg";
import QuoteIcon from "@/assets/standardAPIMapping/quote.svg";
import { IComponent } from "@/utils/types/product.type";
import styles from "./index.module.scss";

type ComponentSelectProps = {
  componentList: any;
  componentName: string;
};

type LabelProps = {
  value: string;
};

const ComponentIcon = ({ name = "" }) => {
  const text = name?.toLowerCase();
  switch (true) {
    case text.includes("address"):
      return <ContactIcon />;
    case text.includes("order"):
      return <OrderIcon />;
    case text.includes("inventory"):
      return <InventoryIcon />;
    case text.includes("quote"):
      return <QuoteIcon />;
    default:
      return <ContactIcon />;
  }
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
    gap={4}
  >
    <div className={styles.componentIconWrapper}>
      <ComponentIcon name={value} />
    </div>
    <Typography.Text
      ellipsis={{ tooltip: value }}
      style={{ fontSize: 16, maxWidth: 253 }}
    >
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
      .filter((el: IComponent) => el.metadata.name !== componentName)
      .map((el: IComponent) => ({
        value: el.metadata.key,
        label: <Label value={el.metadata.name} />,
      }));
  }, [componentList, componentName]);

  const value = useMemo(
    () => ({
      value: componentName,
      label: <Label value={componentName} />,
    }),
    [componentName]
  );

  const handleSelect = (e: { value: string }) => {
    resetUiStore();
    navigate(`/api-mapping/${e.value}`);
  };

  return (
    <div className={styles.selectStyle}>
      <Select
        className={styles.componentSelect}
        onSelect={handleSelect}
        dropdownStyle={{ width: "338px" }}
        size="large"
        value={value}
        labelInValue
        options={parsedOptions}
        suffixIcon={<></>}
      />
    </div>
  );
};

export default ComponentSelect;
