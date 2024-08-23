import { Tag } from "antd";

interface Props {
  status: string | number;
}

const DeploymentStatus = ({ status }: Readonly<Props>) => {
  if (status === "SUCCESS" || status === 200) {
    return <Tag color="success">Success</Tag>;
  }
  if (status === "IN_PROCESS") {
    return <Tag color="purple">In process</Tag>;
  }
  if (status === "FAILED" || Number(status) >= 400) {
    return <Tag color="error">Failed</Tag>;
  }
  return null;
};

export default DeploymentStatus;
