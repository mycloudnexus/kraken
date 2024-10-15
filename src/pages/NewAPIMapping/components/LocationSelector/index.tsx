import { Button, Divider, Dropdown } from "antd";
import styles from "./index.module.scss";
import { DownOutlined } from "@ant-design/icons";
import { ItemType } from "antd/es/menu/interface";

const items: ItemType[] = [
  {
    label: 'Path parameter',
    key: 'PATH',
  },
  {
    label: 'Query parameter',
    key: 'QUERY',
  },
  {
    label: 'Request body',
    key: 'BODY'
  },
  {
    label: <Divider className={styles.optionDivider} />,
    className: styles.selectOptionDivider,
    disabled: true,
    key: 'DIVIDER'
  },
  {
    label: 'Constant value',
    key: 'CONSTANT',
  }
]

export function LocationSelector({ type, onChange }: Readonly<{ type: 'response' | 'request'; onChange(value: string): void }>) {
  const menuItems = type === 'response' ? items.slice(-3) : items

  return (
    <Dropdown
      data-testid="locationSelector"
      menu={{ items: menuItems, onClick: (info) => onChange(info.key) }}>
      <Button
        data-testid="btnSelectLocation"
        className={styles.locationSelector}
        type="link"
        danger
        icon={<DownOutlined />}
        iconPosition="end"
        onClick={(e) => e.preventDefault()}>
        Please select location
      </Button>
    </Dropdown>
  )
}
