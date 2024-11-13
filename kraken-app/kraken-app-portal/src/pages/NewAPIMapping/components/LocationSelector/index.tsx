import { DownOutlined } from "@ant-design/icons";
import { Button, Divider, Dropdown } from "antd";
import { ItemType } from "antd/es/menu/interface";
import styles from "./index.module.scss";

const requestItems: ItemType[] = [
  {
    label: "Hybrid",
    key: "HYBRID",
  },
  {
    label: "Path parameter",
    key: "PATH",
  },
  {
    label: "Query parameter",
    key: "QUERY",
  },
  {
    label: "Request body",
    key: "BODY",
  },
  {
    label: <Divider className={styles.optionDivider} />,
    className: styles.selectOptionDivider,
    disabled: true,
    key: "DIVIDER",
  },
  {
    label: "Constant value",
    key: "CONSTANT",
  },
];

const responseItems: ItemType[] = [
  {
    label: "Response body",
    key: "BODY",
  },
  {
    label: <Divider className={styles.optionDivider} />,
    className: styles.selectOptionDivider,
    disabled: true,
    key: "DIVIDER",
  },
  {
    label: "Constant value",
    key: "CONSTANT",
  },
];

export function LocationSelector({
  type,
  disabled,
  value,
  onChange,
}: Readonly<{
  type: "response" | "request";
  disabled?: boolean;
  value?: string;
  onChange(value: string): void;
}>) {
  const menuItems = type === "response" ? responseItems : requestItems;

  return (
    <Dropdown
      data-testid="locationSelector"
      className={styles.locationDropdown}
      disabled={disabled}
      menu={{ items: menuItems, onClick: (info) => onChange(info.key) }}
    >
      <Button
        data-testid="btnSelectLocation"
        className={styles.locationSelector}
        type="link"
        danger={!value}
        icon={!disabled && <DownOutlined />}
        iconPosition="end"
        onClick={(e) => e.preventDefault()}
      >
        {value || "Please select location"}
      </Button>
    </Dropdown>
  );
}
