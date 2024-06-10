import { isEmpty } from "lodash";
import { useMemo } from "react";

type Props = {
  item: any;
};

const ExpandRow = ({ item }: Props) => {
  const data = useMemo(() => {
    if (!item.facets?.selectedAPIs) {
      return null;
    }
    return item.facets?.selectedAPIs?.map((key: string) => {
      const dataKey = key.split(" ");
      return {
        name: dataKey[0],
        method: dataKey[1],
      };
    });
  }, [item]);

  if (isEmpty(data)) {
    return <td>No data</td>;
  }
  return data?.map((dataItem: any) => (
    <tr key={dataItem.name}>
      <td>{dataItem?.name}</td>
      <td>{dataItem?.method}</td>
    </tr>
  ));
};

export default ExpandRow;
