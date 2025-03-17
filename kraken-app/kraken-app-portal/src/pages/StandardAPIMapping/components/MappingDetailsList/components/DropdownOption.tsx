import MappingMatrix from "@/components/MappingMatrix";
import RequestMethod from "@/components/Method";
import TrimmedPath from "@/components/TrimmedPath";
import { IMapperDetails } from "@/utils/types/env.type";
import { InfoCircleOutlined } from "@ant-design/icons";
import { Flex, Badge, Tooltip } from "antd";

type AdditionalProps = {
  size: number;
  value: string;
};

type DropdownOptionProps = IMapperDetails & AdditionalProps;

const DropdownOption = (optionProps: DropdownOptionProps) => {
  const getPath = (path: string, size: number) => {
    const sizeString = size > 1 ? ` (${size})` : "";
    return path + sizeString;
  };

  return (
    <Flex align="center">
      <RequestMethod method={optionProps.method} />
      <TrimmedPath
        style={{ width: "650px" }}
        path={getPath(optionProps.path, optionProps.size)}
      />
      <Flex justify="space-between">
        <Flex>
          <MappingMatrix
            extraKey={optionProps.path}
            mappingMatrix={optionProps.mappingMatrix}
          />
        </Flex>
        <Flex justify="flex-end" wrap="wrap" align="center">
          {optionProps.mappingStatus === "incomplete" && (
            <Tooltip title="Incomplete mapping">
              <Badge status="warning" />
            </Tooltip>
          )}
          {!optionProps.requiredMapping && (
            <Tooltip title="No mapping required">
              <InfoCircleOutlined style={{ color: "#00000073" }} />
            </Tooltip>
          )}
        </Flex>
      </Flex>
    </Flex>
  );
};

export default DropdownOption;
