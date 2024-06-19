import { Tag } from "antd";
import { capitalize } from "lodash";

interface Props {
  type?: string;
}
const TypeTag = ({ type }: Readonly<Props>) => {
  if (["number", "string", "boolean"].includes(type?.toLowerCase() ?? "")) {
    return (
      <Tag bordered={false} color="success" style={{ marginRight: 0 }}>
        {capitalize(type)}
      </Tag>
    );
  }
  if (type?.toLowerCase() === "object") {
    return (
      <Tag bordered={false} color="success" style={{ marginRight: 0 }}>
        JSON
      </Tag>
    );
  }
  return null;
};

export default TypeTag;
