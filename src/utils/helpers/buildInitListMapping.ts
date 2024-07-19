import { IMapping } from '@/pages/NewAPIMapping/components/ResponseMapping';
import { isEmpty, every, chain, keys, pickBy } from 'lodash';

const buildInitListMapping = (responseMapping: any[]) => {
  let k = 0;
  let list: IMapping[] = [];
  for (const item of responseMapping) {
    if (
      !isEmpty(item.valueMapping) &&
      every(item.valueMapping, (v) => !isEmpty(v))
    ) {
      const res = chain(item.valueMapping)
        .groupBy(value => value)
        .map((_, from) => {
          k += 1;
          return {
            from,
            to: keys(pickBy(item.valueMapping, v => v === from)),
            key: k,
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