import RequestMethod from '@/components/Method';
import { Flex } from 'antd';
import styles from '../index.module.scss';
import Dot from './Dot';

const CollapseLabel = ({ size, isActive, labelProps }: {
  size: number;
  isActive: boolean;
  labelProps: { method: string; path: string };
}) => (
  <Flex className={styles.labelWrapper}>
    {isActive && <Dot />}
    <RequestMethod method={labelProps.method} />
    .../{labelProps.path.split('/').slice(-3).join('/')} {`(${size})`}
  </Flex>
);
export default CollapseLabel;