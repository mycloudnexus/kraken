import Text from "@/components/Text";
import styles from "./index.module.scss";
import { get, isEmpty } from "lodash";
import { Collapse, Table } from "antd";
import { useMemo } from "react";
import Response from "./Response";
import RequestBody from "./RequestBody";
import RequestMethod from "@/components/Method";
import TitleIcon from "@/assets/title-icon.svg";
import Flex from "@/components/Flex";

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
    <div className={styles.infoRoot}>
      <div className={styles.infoHeader}>
        <Text.NormalLarge>
          {get(
            item,
            "info.summary",
            get(
              item,
              "info.description",
              `${item?.title} - ${item?.description}`
            )
          )}
        </Text.NormalLarge>
      </div>
      <div className={styles.infoWrapper}>
        <div>
          {!isEmpty(item) && (
            <>
              <Collapse
                defaultActiveKey={["collapse-content"]}
                className={styles.swaggerCollapse}
                ghost
                expandIconPosition="end"
                items={[
                  {
                    key: "collapse-content",
                    label: (
                      <Flex gap={8} justifyContent="flex-start">
                        <TitleIcon />
                        <Text.NormalLarge>Basics</Text.NormalLarge>
                      </Flex>
                    ),
                    children: (
                      <div>
                        <table className={styles.tableSwagger} role="none">
                          <tr>
                            <th>
                              <Text.LightMedium color="#00000073">
                                Method
                              </Text.LightMedium>
                            </th>
                            <th>
                              <Text.LightMedium color="#00000073">
                                Path
                              </Text.LightMedium>
                            </th>
                          </tr>
                          <tr>
                            <td>
                              <RequestMethod method={item?.description} />
                            </td>
                            <td>
                              <td>{item?.title}</td>
                            </td>
                          </tr>
                        </table>
                        <table className={styles.tableSwagger}>
                          <tr>
                            <th>
                              <Text.LightMedium color="#00000073">
                                Description
                              </Text.LightMedium>
                            </th>
                          </tr>
                          <tr>
                            <td>{item?.info?.description}</td>
                          </tr>
                        </table>
                      </div>
                    ),
                  },
                ]}
              />
              {!isEmpty(item?.info?.parameters) && (
                <Collapse
                  style={{ marginTop: 12 }}
                  defaultActiveKey={["collapse-params-content"]}
                  className={styles.swaggerCollapse}
                  ghost
                  expandIconPosition="end"
                  items={[
                    {
                      key: "collapse-params-content",
                      label: (
                        <Flex gap={8} justifyContent="flex-start">
                          <TitleIcon />
                          <Text.NormalLarge>Parameters</Text.NormalLarge>
                        </Flex>
                      ),
                      children: (
                        <Table
                          style={{ marginTop: 12 }}
                          rowKey={(item: any) => item.name}
                          dataSource={item?.info?.parameters}
                          columns={columns}
                          pagination={false}
                        />
                      ),
                    },
                  ]}
                />
              )}
              {!isEmpty(item?.info?.requestBody) && (
                <Collapse
                  style={{ marginTop: 12 }}
                  defaultActiveKey={["collapse-body-content"]}
                  className={styles.swaggerCollapse}
                  ghost
                  expandIconPosition="end"
                  items={[
                    {
                      key: "collapse-body-content",
                      label: (
                        <Flex gap={8} justifyContent="flex-start">
                          <TitleIcon />
                          <Text.NormalLarge>Request</Text.NormalLarge>
                        </Flex>
                      ),
                      children: (
                        <RequestBody
                          showTitle={false}
                          item={item?.info?.requestBody}
                          schemas={schemas}
                        />
                      ),
                    },
                  ]}
                />
              )}

              <Collapse
                style={{ marginTop: 12 }}
                defaultActiveKey={["collapse-response-content"]}
                className={styles.swaggerCollapse}
                ghost
                expandIconPosition="end"
                items={[
                  {
                    key: "collapse-response-content",
                    label: (
                      <Flex gap={8} justifyContent="flex-start">
                        <TitleIcon />
                        <Text.NormalLarge>Response</Text.NormalLarge>
                      </Flex>
                    ),
                    children: (
                      <div style={{ marginTop: 12 }}>
                        <Response
                          item={item?.info?.responses}
                          schemas={schemas}
                        />
                      </div>
                    ),
                  },
                ]}
              />
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default SwaggerInfo;
