import { Tag } from "antd";

interface Props {
  status: string | number;
}

export const DeploymentStatus = ({ status }: Readonly<Props>) => {
  switch (status) {
    case "SUCCESS":
      return <Tag data-testid="deploymentStatus" color="success">Success</Tag>;

    case "IN_PROCESS":
      return <Tag data-testid="deploymentStatus" color="purple">In process</Tag>;

    case "FAILED":
      return <Tag data-testid="deploymentStatus" color="error">Failed</Tag>;

    default:
      return <Tag data-testid="deploymentStatus">{status}</Tag>
  }
};

export default DeploymentStatus;
