import { Tag } from "antd";

type Props = { status: string };

const BuyerStatus = ({ status }: Props) => {
  switch (status) {
    case "activated":
      return (
        <Tag
          color="#F6FFED"
          style={{ color: "#52C41A", borderColor: "#B7EB8F" }}
        >
          Active
        </Tag>
      );
    case "deactivated":
      return <Tag style={{ color: "#00000073" }}>Inactive</Tag>;
    default:
      return <Tag style={{ color: "#00000073" }}>{status}</Tag>;
  }
};

export default BuyerStatus;
