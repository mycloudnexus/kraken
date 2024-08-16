import { Tag } from "antd";

interface Props {
  status: string;
}

const DeploymentStatus = ({ status }: Readonly<Props>) => {
  if (status === "SUCCESS") {
    return <Tag color="success">Success</Tag>;
  }
  if (status === "IN_PROCESS") {
    return <Tag color="purple">In process</Tag>;
  }
  if (status === "FAILED") {
    return <Tag color="error">Failed</Tag>;
  }
  return null;
};

export default DeploymentStatus;
