import RequestMethod from '@/components/Method';
import { Flex } from 'antd';
import styles from '../index.module.scss';
import Dot from './Dot';
import { IMapperDetails } from '@/utils/types/env.type';



const CollapseLabel = ({ handleSelection, size, isActive, labelProps, isOneChild, highlighted }: {
  handleSelection: (mapItem: IMapperDetails) => void;
  size: number;
  isActive: boolean;
  labelProps: IMapperDetails;
  isOneChild: boolean;
  highlighted: boolean;
}) => {

  return <Flex className={`${styles.labelWrapper} ${highlighted && styles.highlighted}`} onClick={() => handleSelection(labelProps)}>
    {isActive && !isOneChild && <Dot />}
    <RequestMethod method={labelProps.method} />
    .../{labelProps.path.split('/').slice(-2).join('/')} {size > 1 && `(${size})`}
  </Flex>
}
export default CollapseLabel;