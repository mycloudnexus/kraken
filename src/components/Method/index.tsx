import { Tag } from "antd";
import { useMemo } from "react";

const RequestMethod = ({ method = "" }) => {
  const methodColor = useMemo(() => {
    switch (method.toLowerCase()) {
      case "get":
        return "green";
      case "post":
        return "blue";
      case "put":
        return "orange";
      case "delete":
        return "red";
      default:
        return "gray";
    }
  }, [method]);

  return <Tag color={methodColor}>{method.toUpperCase()}</Tag>;
};

export default RequestMethod;
