import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { Button, Flex } from "antd";
import RequestItem from "../RequestItem";

const RequestMapping = () => {
  const { requestMapping, setRequestMapping } = useNewApiMappingStore();
  const handleAdd = () => {
    setRequestMapping([
      ...requestMapping,
      {
        title: "Title of Property Mapping",
        description: "description",
        customizedField: true,
      },
    ]);
  };
  return (
    <Flex vertical gap={26}>
      {requestMapping.map((it, index: number) => (
        <RequestItem item={it} index={index} />
      ))}
      <Button
        type="primary"
        style={{ width: "fit-content" }}
        onClick={handleAdd}
      >
        Add mapping property
      </Button>
    </Flex>
  );
};

export default RequestMapping;
