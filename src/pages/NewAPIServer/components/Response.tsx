import Flex from "@/components/Flex";
import { Button } from "antd";
import { isEmpty } from "lodash";
import { useEffect, useMemo, useState } from "react";

type Props = {
  item: Record<string, any>;
};

const Response = ({ item }: Props) => {
  const [selectedResponse, setSelectedResponse] = useState<string>("");
  const responseKeys = useMemo(() => Object.keys(item || {}), [item]);
  useEffect(() => {
    if (isEmpty(responseKeys)) {
      return;
    }
    setSelectedResponse(responseKeys[0]);
  }, [responseKeys]);

  return (
    <div>
      <Flex justifyContent="flex-start" flexWrap="wrap">
        {responseKeys?.map((rKey: string) => (
          <Button
            key={rKey}
            style={
              selectedResponse === rKey
                ? {
                    borderColor: "#1677ff",
                    color: "#1677ff",
                  }
                : {}
            }
            onClick={() => setSelectedResponse(rKey)}
          >
            {rKey}
          </Button>
        ))}
      </Flex>
    </div>
  );
};

export default Response;
