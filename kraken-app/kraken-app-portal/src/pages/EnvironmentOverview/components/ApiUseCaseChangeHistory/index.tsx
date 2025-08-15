import {Drawer, Flex, Table, Tag} from 'antd';
import styles from "@/pages/EnvironmentOverview/components/RunningAPIMapping/index.module.scss";
import {IApiUseCaseChangeHistory} from "@/utils/types/env.type.ts";
import {ColumnsType} from "antd/es/table";
import {Text} from "@/components/Text";
import {toDateTime} from "@/libs/dayjs.ts";

const FetchHistoryDrawer = ({data, open, onClose}: {data: IApiUseCaseChangeHistory[] | undefined, open: boolean, onClose: () => void}) => {
    const columns: ColumnsType<IApiUseCaseChangeHistory> = [
        {
            title: 'Timestamp',
            width: 240,
            key: 'timestamp',
            align: "center",
            render: item => (
                <Flex vertical gap={2}>
                    <Text.LightMedium data-testid="createdAt">{toDateTime(item?.updatedAt)}</Text.LightMedium>
                </Flex>
            )
        },
        {
            title: 'Action',
            width: 110,
            align: "center",
            key: 'action',
            render: item => (
                <Flex vertical gap={2}>
                    <Tag color={item.available? '#edebe6': '#f0de97'}>
                    <Text.LightMedium data-testid="Action"
                                      style={{fontWeight:'bolder'}}
                                      color={item.available?'black':'red'}
                    >
                        {item.available?'Enabled':'Disabled'}
                    </Text.LightMedium>
                    </Tag>
                </Flex>
            )
        },
        {
            title: 'Created By',
            width: 240,
            key: 'createdBy',
            align: "center",
            render: item => (
                <Flex vertical gap={2}>
                    <Text.LightMedium data-testid="createdBy">{item.updatedBy}</Text.LightMedium>
                </Flex>
            )
        },
    ]
    return (
    <>
    <Drawer width={640} placement="right" closable={false} open={open} onClose={onClose}>
    <p className="site-description-item-profile-p" style={{ marginBottom: 24, fontSize: "larger"}}>
    Change History
    </p>
    <Table
        data-testid='history-table'
        columns={columns}
        dataSource={data}
        tableLayout="fixed"
        rowClassName={styles.mappingRow}
        rowKey={(item) => JSON.stringify(item)}
        pagination={false}
    >
    </Table>
    </Drawer>
    </>
    );
};

export default FetchHistoryDrawer;