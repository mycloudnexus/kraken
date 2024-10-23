import { IMapping } from "@/pages/NewAPIMapping/components/ResponseMapping";
import { isEmpty, every, chain, keys, pickBy } from "lodash";
import { nanoid } from "nanoid";
import { IRequestMapping, IResponseMapping } from "../types/component.type";

const buildInitListMapping = (
  valuesMapping: (IRequestMapping | IResponseMapping)[],
  type: "request" | "response"
) => {
  if (type === "request") {
    return valuesMapping.flatMap(({ name, valueMapping }) =>
      Object.keys(valueMapping ?? {}).map((sonataProp) => ({
        name,
        key: nanoid(),
        from: sonataProp,
        to: [valueMapping?.[sonataProp]], // seller prop
      }))
    ) as IMapping[];
  }

  let list: IMapping[] = [];
  for (const item of valuesMapping) {
    if (
      !isEmpty(item.valueMapping) &&
      every(item.valueMapping, (v) => !isEmpty(v))
    ) {
      const res = chain(item.valueMapping)
        .groupBy((value) => value)
        .map((_, from) => {
          return {
            from,
            to: keys(pickBy(item.valueMapping, (v) => v === from)),
            key: nanoid(),
            name: item.name,
          };
        })
        .value();
      list = [...list, ...res];
    }
  }
  return list;
};

export default buildInitListMapping;
