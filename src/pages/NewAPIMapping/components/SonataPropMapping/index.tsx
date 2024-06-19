import MappingIcon from "@/assets/newAPIMapping/mapping-icon.svg";
import Text from "@/components/Text";
import TypeTag from "@/components/TypeTag";
import { EnumRightType } from "@/utils/types/common.type";
import { FolderFilled, RightOutlined } from "@ant-design/icons";
import { Button, Flex, Tree, TreeDataNode, Typography } from "antd";
import { useEffect, useMemo, useState } from "react";
import styles from "./index.module.scss";

const { DirectoryTree } = Tree;
const treeData: TreeDataNode[] = [
  {
    title: "provideAlternative",
    key: "0-0",
    children: [
      {
        title: "associatedGrographicAddress",
        key: "0-0-0",
        children: [
          {
            title: (
              <Flex align="center" gap={6} className={styles.sonataAPIPropItem}>
                <TypeTag type="string" />
                country
              </Flex>
            ),
            key: "0-0-0-0",
            isLeaf: true,
          },
          {
            title: (
              <Flex align="center" gap={6} className={styles.sonataAPIPropItem}>
                <TypeTag type="string" />
                city
              </Flex>
            ),
            key: "0-0-0-1",
            isLeaf: true,
          },
        ],
      },
    ],
  },
];

const buildArrowList = (expandedKeys: React.Key[]) => {
  const list: any[] = [];
  let countFolder = 0;
  const recursiveBuildArrowList = (
    expandedKeys: React.Key[],
    list: any[],
    node: TreeDataNode
  ) => {
    if (node?.isLeaf) {
      list.push({
        key: node.key,
        marginTop: countFolder * 36,
      });
      countFolder = 0;
      return;
    }
    countFolder += 1;
    if (node?.children && expandedKeys.includes(node.key)) {
      node.children.forEach((child) =>
        recursiveBuildArrowList(expandedKeys, list, child)
      );
    }
    return list;
  };
  treeData.forEach((node) => recursiveBuildArrowList(expandedKeys, list, node));
  return list;
};

const renderIcon = (props: any) =>
  props.isLeaf ? null : <FolderFilled style={{ color: "#bfbfbf" }} />;
const renderSwitcherIcon = () => <></>;

interface Props {
  openRight?: (value: EnumRightType) => void;
}
const SonataPropMapping = ({ openRight }: Readonly<Props>) => {
  const [showSelect, setShowSelect] = useState(false);
  const [expandedKeys, setExpandedKeys] = useState<React.Key[]>([]);
  const alignList = useMemo(() => buildArrowList(expandedKeys), [expandedKeys]);

  useEffect(() => {
    if (showSelect) {
      openRight?.(EnumRightType.AddSonataProp);
    }
  }, [showSelect, openRight]);
  return (
    <Flex gap={16}>
      <div className={styles.sonataPropMappingWrapper}>
        <DirectoryTree
          showLine
          defaultExpandAll
          selectable={false}
          treeData={treeData}
          icon={renderIcon}
          switcherIcon={renderSwitcherIcon}
          className={styles.tree}
          expandedKeys={expandedKeys}
          onExpand={(keys) => setExpandedKeys(keys)}
        />
        {showSelect && (
          <Flex
            align="center"
            justify="space-between"
            className={styles.addMappingDiv}
          >
            <Typography.Text style={{ color: "#86909c" }}>
              Select property
            </Typography.Text>
            <RightOutlined style={{ color: "#4E5969" }} />
          </Flex>
        )}
        <Button
          type="primary"
          onClick={() => setShowSelect((ss) => !ss)}
          style={{ alignSelf: "flex-start" }}
        >
          Add mapping property
        </Button>
      </div>
      <div className={styles.alignArrowList}>
        {alignList?.map((align) => (
          <div
            key={align.key}
            className={styles.alignArrowWrapper}
            style={{
              paddingTop: align.marginTop + 11.67,
            }}
          >
            <MappingIcon />
          </div>
        ))}
      </div>
      <div className={styles.sellerPropMappingWrapper}>
        {alignList?.map((align) => (
          <div
            key={align.key}
            className={styles.sellerPropItemWrapper}
            style={{
              marginTop: align.marginTop,
            }}
          >
            <Text.NormalMedium color="#86909C">
              Select property
            </Text.NormalMedium>
            <RightOutlined style={{ color: "#4E5969" }} />
          </div>
        ))}
      </div>
    </Flex>
  );
};

export default SonataPropMapping;
