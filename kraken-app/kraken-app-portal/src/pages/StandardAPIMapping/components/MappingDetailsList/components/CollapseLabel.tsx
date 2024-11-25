import RequestMethod from "@/components/Method";
import { Flex } from "antd";
import styles from "../index.module.scss";
import Dot from "./Dot";
import { IMapperDetails } from "@/utils/types/env.type";
import TrimmedPath from "@/components/TrimmedPath";

type CollapseLabelProps = {
  handleSelection: (mapItem: IMapperDetails) => void;
  size: number;
  isActive: boolean;
  labelProps: IMapperDetails;
  isOneChild: boolean;
  highlighted: boolean;
};

const CollapseLabel = ({
  handleSelection,
  size,
  isActive,
  labelProps,
  isOneChild,
  highlighted,
}: CollapseLabelProps) => {
  const handleClick = () => {
    handleSelection(labelProps);
  };

  return (
    <Flex
      className={`${styles.labelWrapper} ${highlighted ? styles.highlighted : ""
        } ${isOneChild && highlighted ? "hightlight-one" : ""}`}
      onClick={handleClick}
    >
      {isActive && !isOneChild && <Dot />}
      <RequestMethod method={labelProps.method} />

      <TrimmedPath style={{ width: 160 }} path={`${labelProps.path}${size > 1 ? ` (${size})` : ''}`} />
    </Flex>
  );
};

export default CollapseLabel;
