import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { IRequestMapping } from "@/utils/types/component.type";
import { Button, Flex } from "antd";
import { nanoid } from "nanoid";
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
        id: nanoid(),
      } as IRequestMapping,
    ]);
  };
  return (
    <Flex vertical gap={26}>
      {requestMapping.map((it: IRequestMapping, index: number) => (
        <RequestItem key={it.id} item={it} index={index} />
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
