import { CheckCircleFilled, CloseCircleFilled } from "@ant-design/icons";
import { Spin } from "antd";

type Props = { status: string };

const StatusIcon = ({ status = "" }: Props) => {
  switch (status) {
    case "SUCCESS":
      return <CheckCircleFilled style={{ color: "#389E0D" }} />;
    case "IN_PROCESS":
      return <Spin size="small" />;
    case "FAILED":
      return <CloseCircleFilled style={{ color: "#CF1322" }} />;
    default:
      return <></>;
  }
};

export default StatusIcon;
