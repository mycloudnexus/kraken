import { PageLayout } from "@/components/Layout";
import { useGetProductEnvs } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { Flex, Tabs, Input } from "antd";
import { startCase } from "lodash";
import { useMemo, useRef, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import ActivityDetailModal from "./components/ActivityDetailModal";
import EnvironmentActivityTable from "./components/EnvironmentActivityTable";
import styles from "./index.module.scss";

const { Search } = Input;

const EnvironmentActivityLog = () => {
    const { envId } = useParams();
    const { currentProduct } = useAppStore();
    const navigate = useNavigate();
    const { data: envData } = useGetProductEnvs(currentProduct);
    const refWrapper = useRef<any>();
    const [mainTabKey, setMainTabKey] = useState<string>("activityLog");
    const [pathQuery, setPathQuery] = useState("");

    const [modalActivityId, setModalActivityId] = useState<string | undefined>();
    const [modalOpen, setModalOpen] = useState(false);

    const openActionModal = (requestId: string) => {
        setModalActivityId(requestId);
        setModalOpen(true);
    };

    const envTabs = useMemo(() => {
        return (
            envData?.data?.map((env) => ({
                key: env.id,
                label: `${startCase(env.name)} Environment`,
                children: (
                    <EnvironmentActivityTable
                        openActionModal={openActionModal}
                        pathQuery={pathQuery}
                    />
                ),
            })) ?? []
        );
    }, [envData, pathQuery]);

    const searchPathQuery = (value: string) => {
        setPathQuery(value);
    };

    return (
        <PageLayout
            title={
                <Flex
                    align="center"
                    justify="space-between"
                    vertical={false}
                    style={{ width: "100%" }}
                >
                    <Tabs
                        activeKey={mainTabKey}
                        hideAdd
                        onChange={setMainTabKey}
                        items={[
                            {
                                label: "Activity log",
                                key: "activityLog",
                            }
                        ]}
                    />
                </Flex>
            }
        >
            <div className={styles.contentWrapper} ref={refWrapper}>

                <div className={styles.tableWrapper}>
                    <Tabs
                        type="card"
                        activeKey={envId}
                        items={envTabs}
                        onChange={(key) => {
                            navigate(`/env/${key}`);
                        }}
                        tabBarExtraContent={
                            <Search
                                placeholder="Please enter path keywords"
                                style={{ width: "250px" }}
                                onSearch={searchPathQuery}
                                allowClear
                            />
                        }
                    />
                </div>
            </div>

            <ActivityDetailModal
                envId={String(envId)}
                activityId={modalActivityId ?? ""}
                open={modalOpen}
                setOpen={(value) => setModalOpen(value)}
            />
        </PageLayout>
    );
};

export default EnvironmentActivityLog;