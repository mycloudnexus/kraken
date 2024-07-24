import { useGetProductEnvs } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { Button, Dropdown, Flex, MenuProps, Radio, Space } from "antd";
import Text from "../Text";
import { DownOutlined } from "@ant-design/icons";
import { useEffect, useMemo, useState } from "react";
import { get, isEmpty, sortBy } from "lodash";
import styles from "./index.module.scss";

type Props = {
  value: any;
  onChange: (value: any) => void;
};

const EnvSelect = ({ value, onChange }: Props) => {
  const { currentProduct } = useAppStore();
  const { data } = useGetProductEnvs(currentProduct);
  const [open, setOpen] = useState(false);
  const [currentValue, setCurrentValue] = useState(value);

  const items: MenuProps["items"] = useMemo(() => {
    const envList = sortBy(get(data, "data", []), "name").reverse();
    return envList.map((item) => ({
      key: item.name,
      label: (
        <Radio value={item.id} style={{ textTransform: "capitalize" }}>
          {item.name}
        </Radio>
      ),
    }));
  }, [data]);

  useEffect(() => {
    const envList = sortBy(get(data, "data", []), "name");
    const stage = envList.find(
      (env: any) => env.name?.toLowerCase() === "stage"
    );
    if (isEmpty(value)) {
      onChange(stage?.id);
      setCurrentValue(stage?.id);
    } else {
      setCurrentValue(value);
    }
  }, [value, data]);

  useEffect(() => {
    const handleOutsideClick = (e: any) => {
      if (e?.target?.closest(".select-modal") === null) {
        setOpen(false);
      }
    };

    if (open) {
      document.addEventListener("click", handleOutsideClick);
    } else {
      document.removeEventListener("click", handleOutsideClick);
    }

    return () => {
      document.removeEventListener("click", handleOutsideClick);
    };
  }, [open]);

  const mapValue = useMemo(() => {
    const envList = sortBy(get(data, "data", []), "name");
    const env = envList.find((env: any) => env.id === value);
    return env?.name;
  }, [data, value]);

  return (
    <Flex gap={6} align="center">
      <Text.LightMedium color="#8C8C8C">Environment</Text.LightMedium>
      <Dropdown
        destroyPopupOnHide
        overlayClassName={styles.dropdown}
        open={open}
        menu={{ items }}
        dropdownRender={(em) => (
          <div className="select-modal">
            <Radio.Group
              className={styles.radio}
              value={currentValue}
              onChange={(e) => {
                setCurrentValue(e.target.value);
              }}
            >
              {em}
            </Radio.Group>
            <Flex align="center" justify="center" className={styles.footer}>
              <Button
                type="primary"
                onClick={() => {
                  onChange(currentValue);
                  setOpen(false);
                }}
              >
                OK
              </Button>
            </Flex>
          </div>
        )}
      >
        <Space onClick={() => setOpen(true)} className="select-modal">
          <Text.LightMedium
            style={{ textTransform: "capitalize", cursor: "pointer" }}
          >
            {mapValue}
          </Text.LightMedium>{" "}
          <DownOutlined style={{ fontSize: 8 }} />
        </Space>
      </Dropdown>
    </Flex>
  );
};

export default EnvSelect;
