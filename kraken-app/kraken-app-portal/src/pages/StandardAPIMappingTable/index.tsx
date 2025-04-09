import BreadCrumb from "@/components/Breadcrumb";
import { PageLayout } from "@/components/Layout";
import MappingMatrix from "@/components/MappingMatrix";
import RequestMethod from "@/components/Method";
import TrimmedPath from "@/components/TrimmedPath";
import {
  useGetComponentListAPI,
  useGetComponentDetail,
  useGetComponentDetailMapping,
} from "@/hooks/product";
import {
  useGetProductTypeList,
} from "@/hooks/homepage";
import { useAppStore } from "@/stores/app.store";
import { IMapperDetails } from "@/utils/types/env.type";
import { Badge, Button, Flex, Spin, Table, TableColumnsType } from "antd";
import { get } from "lodash";
import { useMemo } from "react";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import ComponentSelect from "../StandardAPIMapping/components/ComponentSelect";
import styles from "./index.module.scss";

interface RowSpanDetails {
  [path: string]: {
    count: number;
    firstIndex?: number;
  };
}

// Define this type above your component
type ProductTypeOption = {
  key: string;
  label: string;
};

type ComponentItem = {
  kind: string;
  apiVersion: string;
  metadata: {
    id: string;
    name: string;
    key: string;
  };
  facets?: {
    supportedProductTypesAndActions?: {
      path: string;
      method: string;
      productTypes: string[];
    }[];
  };
};

const StandardAPIMappingTable = () => {
  const navigate = useNavigate();
  const { componentId } = useParams();
  const { currentProduct } = useAppStore();
  const { state } = useLocation();
  const { productType } = state;
  const { data: componentList } = useGetComponentListAPI(currentProduct);
  const { data: componentDetail, isLoading } = useGetComponentDetail(
    currentProduct,
    componentId ?? ""
  );
  const { data: detailDataMapping, isLoading: isDetailMappingLoading } =
    useGetComponentDetailMapping(
      currentProduct,
      componentId ?? "",
      productType
    );
  
  const { data: productTypeList } = useGetProductTypeList(currentProduct);

  const componentName = useMemo(
    () => get(componentDetail, "metadata.name", ""),
    [componentDetail]
  );

  const mergePath = (data: IMapperDetails[]) => {
    const result = [...data];
    const rowSpanDetails: RowSpanDetails = {};

    data.forEach((row, index) => {
      if (!rowSpanDetails[row.path]) {
        rowSpanDetails[row.path] = { count: 0, firstIndex: index };
      }
      rowSpanDetails[row.path].count += 1;
    });

    result.forEach((row, index) => {
      const { count, firstIndex } = rowSpanDetails[row.path];
      if (index === firstIndex) {
        row.rowSpan = count;
      } else {
        row.rowSpan = 0;
      }
    });

    return result;
  };

  const mappingDetailsData = useMemo(() => {
    if (!detailDataMapping?.details) return [];

    const filteredData = detailDataMapping.details.filter(
      (product) =>
        product.mappingMatrix?.productType === productType?.toLowerCase()
    );

    return mergePath(filteredData);
  }, [detailDataMapping, productType]);

  const productTypeOptions = useMemo(() => {
    if (!productTypeList) return [];
    // Safely assert the type to string[] just inside this block
    const list = productTypeList as string[];
    return list.reduce<ProductTypeOption[]>((acc, item) => {
      const [key, label] = item.split(":");
      if (key && label) {
        acc.push({ key, label });
      }
      return acc;
    }, []);
  }, [productTypeList]);

  const filteredComponentList = useMemo(() => {
    const components = (componentList?.data ?? []) as ComponentItem[];
    if (!components.length) return [];
    if (productType === "SHARE") {
      return components.filter((item) => {
        const supportedTypes = item.facets?.supportedProductTypesAndActions ?? [];
        return supportedTypes?.some((entry) =>
          entry.productTypes?.includes("SHARE")
        );
      });
    } else {
      return components.filter((item) => {
        const supportedTypes = item.facets?.supportedProductTypesAndActions ?? [];
        return supportedTypes?.every((entry) =>
          !entry.productTypes?.includes("SHARE")
        );
      });
    }
  }, [componentList, productType]);
  
  const columns: TableColumnsType = [
    {
      title: "Endpoints",
      dataIndex: "path",
      render: (path, data) => (
        <Flex>
          <RequestMethod method={data.method} />
          <TrimmedPath path={path} style={{ width: "500px" }} />
        </Flex>
      ),
      onCell: (record) => {
        return {
          rowSpan: record.rowSpan,
        };
      },
    },
    {
      title: "Variable matrix",
      dataIndex: "mappingMatrix",
      render: (matrix) => <MappingMatrix mappingMatrix={matrix} />,
    },
    {
      title: "Mapping status",
      dataIndex: "mappingStatus",
      render: (status) => {
        const badgeProps = () => {
          switch (status) {
            case "complete":
              return {
                text: "Complete",
                color: "#389E0D",
              };
            case "incomplete":
              return {
                text: "Incomplete",
                color: "#FF9A2E",
              };
            default:
              return {
                text: "Error",
                color: "red",
              };
          }
        };
        return <Badge {...badgeProps()} />;
      },
    },
    {
      title: "Actions",
      dataIndex: "targetKey",
      render: (targetKey) => (
        <Button type="link" onClick={() => navigate(targetKey)}>
          Mapping
        </Button>
      ),
    },
  ];

  return (
    <PageLayout
      title={
        <Flex
          align="center"
          justify="space-between"
          style={{ padding: "5px 0" }}
        >
          <BreadCrumb
            mainTitle={
              productTypeOptions.find((opt) => opt.key === productType)?.label ??
              "Standard API mapping"
            }
            mainUrl="/components"
            lastItem={
              <ComponentSelect
                componentList={{data : filteredComponentList}}
                componentName={componentName}
                productType={productType}
              />
            }
          />
        </Flex>
      }
    >
      <Spin spinning={isLoading || isDetailMappingLoading}>
        <Flex className={styles.pageBody}>
          <Table
            style={{ width: "100%" }}
            columns={columns}
            dataSource={mappingDetailsData}
            pagination={false}
          ></Table>
        </Flex>
      </Spin>
    </PageLayout>
  );
};

export default StandardAPIMappingTable;
