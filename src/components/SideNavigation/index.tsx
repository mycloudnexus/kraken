import {
  HomeOutlined,
  ApiOutlined,
  AlertOutlined,
  FolderAddOutlined,
  CloudServerOutlined,
  UsergroupAddOutlined,
  SettingOutlined,
  DoubleLeftOutlined,
} from "@ant-design/icons";
import { Button, Flex, Menu, Typography } from "antd";
import { Link } from "react-router-dom";
import styles from "./index.module.scss";
import { useEffect, useMemo, useState } from "react";
import Sider from "antd/es/layout/Sider";
import ETIcon from "../../assets/et.svg";
import { useAppStore } from "@/stores/app.store";
import { useGetProductEnvs } from "@/hooks/product";
import { IEnv } from "@/utils/types/env.type";
import { last } from "lodash";
import { useSessionStorage } from "usehooks-ts";

const flattenMenu = (
  menuArr: any[],
  childProp: string,
  openKeys: string[] = []
): any[] => {
  return menuArr.reduce((agg, item) => {
    agg = [
      ...agg,
      {
        key: item.key,
        path: item.path,
        matching: item.matching,
        openKeys,
      },
    ];
    if (item[childProp]) {
      const childItems = flattenMenu(item[childProp], childProp, [
        ...openKeys,
        item.key,
      ]);
      agg = [...agg, ...childItems];
    }
    return agg;
  }, []);
};

const SideNavigation = () => {
  const [collapsed, setCollapsed] = useSessionStorage("collapsed", false);
  const [currentEnvId, setCurrentEnvId] = useState<string | null>(null);
  const [activeKey, setActiveKey] = useState<string>(window.location.pathname);
  const { currentProduct } = useAppStore();
  const { data: envs, isLoading } = useGetProductEnvs(currentProduct);

  useEffect(() => {
    if (envs?.data?.length) {
      setCurrentEnvId(
        envs.data.filter((env: IEnv) => env?.name === "stage")[0].id
      );
    }
  }, [envs?.data, isLoading]);

  const items = useMemo(
    () => [
      {
        key: "/",
        label: <Link to="/">Home</Link>,
        icon: <HomeOutlined />,
      },
      {
        key: "components",
        label: <Link to="/components">Standard API mapping</Link>,
        icon: <ApiOutlined />,
        matching: ["components", "api-mapping/[a-z0-9-]+"],
      },
      {
        key: "component/",
        label: <Link to="/component/mef.sonata/list">Seller API setup</Link>,
        icon: <AlertOutlined />,
      },
      {
        key: "env",
        label: <Link to="/env">Deployment</Link>,
        icon: <FolderAddOutlined />,
      },
      {
        key: `env/${currentEnvId}`,
        label: <Link to={`/env/${currentEnvId}`}>API activity log</Link>,
        icon: <CloudServerOutlined />,
        matching: ["env/[a-z0-9-]+"],
      },
      {
        key: "buyer",
        label: <Link to="/buyer">Buyer management</Link>,
        icon: <UsergroupAddOutlined />,
      },
      {
        label: "Settings",
        key: "settings",
        icon: <SettingOutlined />,
        matching: [
          "settings",
          "audit-log",
          "mapping-template",
          "user-management",
        ],
        children: [
          {
            label: <Link to="/audit-log">Audit log</Link>,
            key: "audit-log",
          },
          {
            key: "mapping-template",
            label: (
              <Link to="/mapping-template">
                Mapping template release & Upgrade
              </Link>
            ),
          },
          {
            key: "user-management",
            label: <Link to="/user-management">User management</Link>,
          },
        ],
      },
    ],
    [currentEnvId]
  );

  const selectedKeys = useMemo(() => {
    const flattenItems = flattenMenu(items, "children");

    return last(
      flattenItems
        .filter(({ matching, key }) => {
          if (matching) {
            return matching.some((match: string) =>
              new RegExp(match).test(activeKey)
            );
          }
          return activeKey.includes(key);
        })
        .map(({ key }) => key)
    );
  }, [activeKey, items]);

  const handleNavigationSet = (key: string) => {
    setActiveKey(key);
  };

  const openKeys = useMemo(() => {
    if (collapsed) {
      return [];
    }
    if (
      ["settings", "audit-log", "mapping-template", "user-management"].some(
        (match: string) => new RegExp(match).test(activeKey)
      )
    ) {
      return ["settings"];
    }
    return [activeKey];
  }, [activeKey]);

  return (
    <Sider
      width={240}
      collapsedWidth={70}
      collapsed={collapsed}
      onCollapse={(value: boolean) => setCollapsed(value)}
      className={styles.sideBar}
    >
      <div className={styles.innerMenu}>
        <Menu
          mode="inline"
          disabledOverflow={true}
          activeKey={activeKey}
          defaultSelectedKeys={[activeKey]}
          defaultOpenKeys={openKeys}
          onSelect={(e) => handleNavigationSet(e.key)}
          items={items}
          selectedKeys={selectedKeys}
        ></Menu>
      </div>
      <Flex className={styles.siderBottom} vertical={true}>
        {!collapsed ? (
          <Flex
            justify="center"
            align="center"
            className={styles.siderBottomVersion}
            gap={4}
          >
            <Typography.Text>A product by&nbsp;</Typography.Text>
            <ETIcon />
          </Flex>
        ) : (
          <Flex
            justify="center"
            align="center"
            vertical
            gap={4}
            style={{ marginBottom: 12 }}
          >
            <ETIcon />
          </Flex>
        )}
        <Flex
          align="center"
          justify="flex-end"
          className={styles.siderBottomCollapseWrapper}
        >
          <Button
            icon={
              <DoubleLeftOutlined
                style={{
                  transform: collapsed ? "rotate(180deg)" : undefined,
                  transition: "0.3s",
                }}
              />
            }
            size="small"
            onClick={() => setCollapsed(!collapsed)}
          />
        </Flex>
      </Flex>
    </Sider>
  );
};

export default SideNavigation;
