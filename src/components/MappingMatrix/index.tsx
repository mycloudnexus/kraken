import { Tag, Flex } from 'antd';
import { toUpper } from 'lodash';
import styles from './index.module.scss';

const MappingMatrix = ({ extraKey = '', mappingMatrix, isItemActive = false }: { extraKey?: string, mappingMatrix: Record<string, string | boolean>, isItemActive?: boolean }) => {
  if(!mappingMatrix) return null
  const renderTextType = (type: string | boolean) => {
    switch (type) {
      case "access_e_line":
        return "Access E-line";
      case "uni":
        return "UNI";
      default:
        return String(type);
    }
  };

  const tagLabels = Object.entries(mappingMatrix).map(([label, value]) => {
    return { label: label, value: renderTextType(value) };
  });

  return tagLabels.map(({ label, value }, index) => (
    <Tag key={`${extraKey}-${label}-${value}-${index}`}>
      <Flex vertical className={`${styles.tagBadge} ${isItemActive && styles.tagActive}`}>
        <span>{label}</span>
        {toUpper(String(value))}
      </Flex>
    </Tag>

  ));
};

export default MappingMatrix;