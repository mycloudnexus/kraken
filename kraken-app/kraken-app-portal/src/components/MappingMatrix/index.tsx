import { Tag, Flex, Typography, FlexProps } from "antd";
import { toUpper } from "lodash";
import styles from "./index.module.scss";
import classNames from "classnames";

function beautifyCamelCase(text: string): string {
  return text.replace(/([a-z])(?=[A-Z])/g, "$1 ");
}

const MappingMatrix = ({
  extraKey = "",
  mappingMatrix,
  isItemActive = false,
  className,
  ...props
}: Readonly<{
  extraKey?: string;
  mappingMatrix: Record<string, string | boolean>;
  isItemActive?: boolean;
} & Omit<FlexProps, 'children'>>) => {
  if (!mappingMatrix) return null;
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

  return (
    <Flex className={classNames(className, styles.wrapper)} {...props}>
      {tagLabels.map(({ label, value }, index) => (
        <Tag
          key={`${extraKey}-${label}-${value}-${index}`}
          className={styles.tag}
        >
          <Flex vertical className={styles.tagContainer}>
            <Typography.Text
              data-testid="mappingType"
              className={styles.tagLabel}
              ellipsis={{ tooltip: true }}
            >
              {beautifyCamelCase(label)}
            </Typography.Text>
            <Typography.Text
              data-testid="mappingValue"
              className={`${styles.tagBadge}  ${isItemActive && styles.tagActive}`}
              ellipsis={{ tooltip: true }}
            >
              {toUpper(String(value))}
            </Typography.Text>
          </Flex>
        </Tag>
      ))}
    </Flex>
  );
};

export default MappingMatrix;
