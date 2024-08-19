import LogMethodTag from '@/components/LogMethodTag';
import { parseObjectDescriptionToTreeData } from '@/utils/helpers/schema';
import { Flex, Typography, Table, Switch, Tree, Button } from 'antd';
import styles from '../index.module.scss';
import { IActivityLog } from '@/utils/types/env.type';
import { Dispatch, SetStateAction, useState } from 'react';

interface ActivitySwitchProps {
  value: boolean;
  handleChange: Dispatch<SetStateAction<boolean>>;
  jsonValue: string;
}

const ActivitySwitch = ({ value, handleChange, jsonValue }: ActivitySwitchProps) => {
  const [copyButtonText, setCopyButtonText] = useState<string>("Copy all")

  const handleCopy = () => {
    navigator.clipboard.writeText(jsonValue).then(() => {
      setCopyButtonText("Copied!");
      setTimeout(function () {
        setCopyButtonText("Copy all");
      }, 3000);
    })
  }

  return <Flex gap={4} align='center'>
    {!!value && <Button
      type='link'
      className={styles.copyButton}
      onClick={handleCopy}
    >
      {copyButtonText}
    </Button>
    }
    JSON <Switch checked={value} onChange={handleChange} />
  </Flex>
};

interface ActivityDetailItemProps {
  title: string;
  activity: IActivityLog;
  collapseItems: (activity: IActivityLog) => any;
}

const ActivityDetailItem = ({ title, activity, collapseItems }: ActivityDetailItemProps) => {
  const [requestJsonEnabled, setRequestJsonEnabled] = useState<boolean>(true);
  const [responseJsonEnabled, setResponseJsonEnabled] = useState<boolean>(true);

  return (
    <div className={styles.activity} key={activity.requestId}>
      <h1>{title}</h1>
      <div className={styles.activityWrapper} key={`${activity.method}_${activity.path}`}>
        <Flex vertical gap={24} className={styles.activityHeader}>
          <Flex gap={8} align="center">
            <LogMethodTag method={activity.method} />
            <Typography.Text ellipsis={{ tooltip: true }}>
              {activity.path}
            </Typography.Text>
          </Flex>
        </Flex>
        <div className={styles.activityBody}>
          <h3>Parameters</h3>
          <Table
            columns={collapseItems(activity)?.parameterColumns}
            dataSource={collapseItems(activity)?.parameterList}
            pagination={false}
          />
          {activity?.request && <>
            <Flex align="center" justify="space-between">
              <h3>Request body</h3>
              <ActivitySwitch value={requestJsonEnabled} handleChange={setRequestJsonEnabled} jsonValue={JSON.stringify(activity?.request)} />
            </Flex>
            <div className={styles.tree}>
              {requestJsonEnabled ? (
                <pre>
                  <Typography.Text>{JSON.stringify(activity?.request, undefined, 2)}</Typography.Text>
                </pre>
              ) : (
                <Tree
                  treeData={parseObjectDescriptionToTreeData(
                    activity.request,
                    styles.treeTitle,
                    styles.treeExample
                  )}
                />
              )}
            </div>
          </>}
          {activity?.response &&
            <>
              <Flex align="center" justify="space-between">
                <h3>Response</h3>
                <ActivitySwitch value={responseJsonEnabled} handleChange={setResponseJsonEnabled} jsonValue={JSON.stringify(activity?.response)} />
              </Flex>
              <div className={styles.tree}>
                {responseJsonEnabled ? (
                  <pre>
                    <Typography.Text>{JSON.stringify(activity?.response, undefined, 2)}</Typography.Text>
                  </pre>
                ) : (
                  <Tree
                    treeData={parseObjectDescriptionToTreeData(
                      activity.response,
                      styles.treeTitle,
                      styles.treeExample
                    )}
                  />
                )}
              </div>
            </>
          }
        </div>
      </div>
    </div>
  );
};

export default ActivityDetailItem;
