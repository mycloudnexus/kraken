import { Tag, Flex, Typography } from 'antd';
import { toUpper } from 'lodash';
import styles from './index.module.scss';

const MappingMatrix = ({ extraKey = '', mappingMatrix, isItemActive = false }: { extraKey?: string, mappingMatrix: Record<string, string | boolean>, isItemActive?: boolean }) => {
  if (!mappingMatrix) return null
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

  return (<Flex wrap style={{ marginLeft: '20px', maxWidth: "calc(100% - 35px)" }} gap={3}>
    {tagLabels.map(({ label, value }, index) => (
      <Tag key={`${extraKey}-${label}-${value}-${index}`} style={{ maxWidth: "100%", marginRight: "3px" }}>
        <Flex vertical className={styles.tagContainer}>
          <Typography.Text
            className={styles.tagLabel}
            ellipsis={{ tooltip: true }}
          >
            {label}
          </Typography.Text>
          <Typography.Text
            className={`${styles.tagBadge}  ${isItemActive && styles.tagActive}`}
            ellipsis={{ tooltip: true }}
          >
            {toUpper(String(value))}
          </Typography.Text>
        </Flex>
      </Tag>
    ))}
  </Flex>
  )
};

export default MappingMatrix;