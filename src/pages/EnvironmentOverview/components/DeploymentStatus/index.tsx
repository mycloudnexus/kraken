import { Tag } from "antd";

interface Props {
  status: string;
}

const DeploymentStatus = ({ status }: Readonly<Props>) => {
  if (status === "SUCCESS") {
    return (
      <Tag bordered={false} color="success">
        Success
      </Tag>
    );
  }
  if (status === "IN_PROCESS") {
    return (
      <Tag bordered={false} color="blue">
        In process
      </Tag>
    );
  }
  if (status === "FAILED") {
    return (
      <Tag bordered={false} color="error">
        Failed
      </Tag>
    );
  }
  return null;
};

export default DeploymentStatus;
