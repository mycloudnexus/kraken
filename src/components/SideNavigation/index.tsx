import { HomeOutlined, ApiOutlined, AlertOutlined, FolderAddOutlined, CloudServerOutlined, UsergroupAddOutlined, SettingOutlined, DoubleLeftOutlined } from '@ant-design/icons'
import { Button, Flex, Menu, Typography } from 'antd'
import { Link } from 'react-router-dom'
import styles from './index.module.scss'
import { useEffect, useState } from 'react'
import Sider from 'antd/es/layout/Sider'
import ETIcon from '../../assets/et.svg'
import { useAppStore } from '@/stores/app.store'
import { useGetProductEnvs } from '@/hooks/product'
import { IEnv } from '@/utils/types/env.type'

const SideNavigation = () => {
  const [collapsed, setCollapsed] = useState<boolean>(false);
  const [currentEnvId, setCurrentEnvId] = useState<string | null>(null)
  const [activeKey, setActiveKey] = useState<string>(window.location.pathname)
  const { currentProduct } = useAppStore();
  const { data: envs, isLoading } = useGetProductEnvs(currentProduct);

  useEffect(() => {
    if(envs?.data?.length) {
      setCurrentEnvId(envs.data.filter((env: IEnv) => env?.name === 'stage')[0].id)
    }
  }, [isLoading])

  return <Sider
    width={240}
    collapsedWidth={70}
    collapsed={collapsed}
    onCollapse={(value: boolean) => setCollapsed(value)}
    className={styles.sideBar}
  >
    <div className={styles.innerMenu} >
      <Menu
        mode='vertical'
        disabledOverflow={true}
        activeKey={activeKey}
        defaultSelectedKeys={[activeKey]}
        defaultOpenKeys={collapsed ? [] : [activeKey]}
        onSelect={(e) => setActiveKey(e.key)}
        items={[
          {
            key: '/',
            label: <Link to='/'>Home</Link>,
            icon: <HomeOutlined />
          },
          {
            key: '/components',
            label: <Link to='/components'>Standard API mapping</Link>,
            icon: <ApiOutlined />
          },
          {
            key: '/component/mef.sonata/list',
            label: <Link to='/component/mef.sonata/list'>Seller API setup</Link>,
            icon: <AlertOutlined />
          },
          {
            key: '/env',
            label: <Link to='/env'>Deployment</Link>,
            icon: <FolderAddOutlined />
          },
          {
            key: `/env/${currentEnvId}`,
            label: <Link to={`/env/${currentEnvId}`}>API activity log</Link>,
            icon: <CloudServerOutlined />
          },
          {
            key: '/buyer',
            label: <Link to='/buyer'>Buyer management</Link>,
            icon: <UsergroupAddOutlined />
          },
          {
            key: '/settings',
            label: <Link to='/'>Settings</Link>,
            icon: <SettingOutlined />
          }
        ]}
      >

      </Menu>
    </div>
    <Flex className={styles.siderBottom} vertical={true}>
      {!collapsed ? (
        <Flex justify='center' align='center' className={styles.siderBottomVersion} gap={4}>
          <Typography.Text>A product by&nbsp;</Typography.Text>
          <ETIcon />
        </Flex>
      ) : (
        <Flex justify='center' align='center' vertical gap={4} style={{ marginBottom: 12 }}>
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
                transform: collapsed ? 'rotate(180deg)' : undefined,
                transition: '0.3s',
              }}
            />
          }
          size="small"
          onClick={() => setCollapsed(!collapsed)}
        />
      </Flex>
    </Flex>
  </Sider>
}

export default SideNavigation