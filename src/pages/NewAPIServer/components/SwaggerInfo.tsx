import Text from "@/components/Text";
import styles from "./index.module.scss";
import { isEmpty } from "lodash";
import { Table } from "antd";
import { useMemo } from "react";
import Response from "./Response";
import RequestBody from "./RequestBody";
import RequestMethod from "@/components/Method";

type Props = {
  item: {
    key: string;
    title: string;
    info: any;
    description: string;
  };
  schemas: any;
};

const SwaggerInfo = ({ item, schemas }: Props) => {
  const columns = useMemo(
    () => [
      {
        title: "Name",
        dataIndex: "name",
      },
      {
        title: "Type",
        dataIndex: "schema",
        render: (schema: any) => schema?.type,
      },
      {
        title: "Description",
        dataIndex: "description",
      },
    ],
    []
  );

  return (
    <div className={styles.infoWrapper}>
      <div>
        <Text.Custom size="20px">API details</Text.Custom>
        {!isEmpty(item) && (
          <>
            <table className={styles.tableSwagger} role="none">
              <tr>
                <td className={styles.specialTd}>Name</td>
                <td>
                  {item?.title} - {item?.description}
                </td>
              </tr>
              <tr>
                <td className={styles.specialTd}>Method</td>
                <td>
                  <RequestMethod method={item?.description} />
                </td>
              </tr>
              <tr>
                <td className={styles.specialTd}>Path</td>
                <td>{item?.title}</td>
              </tr>
              <tr>
                <td className={styles.specialTd}>Description</td>
                <td>{item?.info?.description}</td>
              </tr>
            </table>
            {!isEmpty(item?.info?.parameters) && (
              <div className={styles.tableParams}>
                <Text.LightLarge>Parameters</Text.LightLarge>
                <Table
                  rowKey={(item: any) => item.name}
                  dataSource={item?.info?.parameters}
                  columns={columns}
                  pagination={false}
                />
              </div>
            )}
            <div className={styles.tableParams}>
              <RequestBody item={item?.info?.requestBody} schemas={schemas} />
            </div>
            <div className={styles.tableParams}>
              <Text.LightLarge>Response</Text.LightLarge>
              <Response item={item?.info?.responses} schemas={schemas} />
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default SwaggerInfo;
