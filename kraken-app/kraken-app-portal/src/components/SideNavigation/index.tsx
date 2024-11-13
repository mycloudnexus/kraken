import ActivityLogIcon from "@/assets/icon/activity-log.svg";
import BuyerIcon from "@/assets/icon/buyer.svg";
import SellerAPISetupIcon from "@/assets/icon/seller-api-setup.svg";
import ServiceCatalogIcon from "@/assets/icon/service-catalog.svg";
import { useGetProductEnvs } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { IEnv } from "@/utils/types/env.type";
import {
  HomeOutlined,
  ApiOutlined,
  SettingOutlined,
  DoubleLeftOutlined,
  ExclamationCircleOutlined,
} from "@ant-design/icons";
import { Button, Divider, Flex, Menu, Tooltip, Typography } from "antd";
import Sider from "antd/es/layout/Sider";
import { last } from "lodash";
import { useEffect, useMemo, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { useSessionStorage } from "usehooks-ts";
import ETIcon from "../../assets/et.svg";
import styles from "./index.module.scss";
import { ISystemInfo } from "@/utils/types/user.type";
import { Text } from "../Text";
import classNames from "classnames";

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

const SideNavigation = ({ info }: Readonly<{ info?: ISystemInfo }>) => {
  const location = useLocation();
  const [collapsed, setCollapsed] = useSessionStorage("collapsed", false);
  const [activeKey, setActiveKey] = useState<string>(location.pathname);
  const { currentProduct } = useAppStore();
  const { data: envs } = useGetProductEnvs(currentProduct);

  const stageId = useMemo(() => {
    const stage = envs?.data?.find(
      (env: IEnv) => env.name?.toLowerCase() === "stage"
    );
    return stage?.id;
  }, [envs]);

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
        icon: <SellerAPISetupIcon />,
      },
      {
        key: "env",
        label: <Link to="/env">Deployment</Link>,
        icon: <ServiceCatalogIcon />,
      },
      {
        key: `env/${stageId}`,
        label: <Link to={`/env/${stageId}`}>API activity log</Link>,
        icon: <ActivityLogIcon />,
        matching: ["env/[a-z0-9-]+"],
      },
      {
        key: "buyer",
        label: <Link to="/buyer">Buyer management</Link>,
        icon: <BuyerIcon />,
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
          "user-management-v2",
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
            key: "mapping-template-v2",
            label: (
              <Link to="/mapping-template-v2">
                Mapping template release & Upgrade v2
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
    [stageId]
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

  useEffect(() => {
    handleNavigationSet(location.pathname);
  }, [location.pathname]);

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
        />
      </div>
      <Flex className={styles.siderBottom} vertical={true}>
        <Flex
          justify="center"
          align="center"
          className={classNames(styles.siderBottomVersion, collapsed && styles.collapsed)}
          gap={4}
        >
          {info?.productionAppVersion && (
            <div className={classNames(styles.appVersion, collapsed && styles.collapsed)}>
              <Typography.Text data-testid="productionAppVersion" ellipsis style={{ color: 'var(--text-secondary)' }}>
                {info.productionAppVersion}
              </Typography.Text>
              <Tooltip title={<KrakenVersion info={info} />}>
                <ExclamationCircleOutlined data-testid="appVersionsIndicator" style={{ color: 'var(--text-secondary)' }} />
              </Tooltip>
              {!collapsed && (<Divider type="vertical" style={{ margin: '0 6px' }} />)}
            </div>
          )}
            {!collapsed ? (
              <span className={styles.productionBy}>A product by <ETIcon /></span>
            ) : <ETIcon />}
            
        </Flex>

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

function KrakenVersion({ info }: Readonly<{ info: ISystemInfo }>) {
  return <section>
    <Text.NormalMedium data-testid="headline">Kraken version</Text.NormalMedium>
    <ul className={styles.envVersions}>
      <li data-testid="vProductionAppVersion">Production: {info.productionAppVersion}</li>
      <li data-testid="vStageAppVersion">Stage: {info.stageAppVersion}</li>
      <li data-testid="vControlPlaneAppVersion">Control plane: {info.controlAppVersion}</li>
    </ul>
  </section>
}

export default SideNavigation;
