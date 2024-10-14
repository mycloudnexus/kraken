import RequestMethod from '@/components/Method';
import { Flex } from 'antd';
import styles from '../index.module.scss';
import Dot from './Dot';
import { IMapperDetails } from '@/utils/types/env.type';

type CollapseLabelProps = {
  handleSelection: (mapItem: IMapperDetails) => void;
  size: number;
  isActive: boolean;
  labelProps: IMapperDetails;
  isOneChild: boolean;
  highlighted: boolean;
};

const CollapseLabel = ({
  handleSelection,
  size,
  isActive,
  labelProps,
  isOneChild,
  highlighted,
}: CollapseLabelProps) => {
  const handleClick = () => {
    handleSelection(labelProps);
  };

  const pathSnippet = labelProps.path.split('/').slice(-2).join('/');

  return (
    <Flex
      className={`${styles.labelWrapper} ${highlighted ? styles.highlighted : ''}`}
      onClick={handleClick}
    >
      {isActive && !isOneChild && <Dot />}
      <RequestMethod method={labelProps.method} />
      .../{pathSnippet} {size > 1 && `(${size})`}
    </Flex>
  );
};

export default CollapseLabel;
