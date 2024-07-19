import { Flex, Select } from 'antd';
import styles from './index.module.scss';
import { useMemo } from 'react';
import ContactIcon from "@/assets/standardAPIMapping/contact.svg";
import { useNavigate } from 'react-router-dom';

type ComponentSelectProps = {
  componentList: any;
  componentName: string
}

const Label = ({ value }: { value: string }) => (
  <Flex style={{ textOverflow: "ellipsis", overflow: 'hidden', whiteSpace: 'nowrap' }} align='center' gap={10}><div className={styles.componentIconWrapper}><ContactIcon /></div>{value}</Flex>
)

const ComponentSelect = ({ componentList, componentName }: ComponentSelectProps) => {
  const navigate = useNavigate();

  const parsedOptions = useMemo(() => {
    if (!componentList?.data?.length) return []
    return componentList.data.map((el: any) => ({
      value: el.metadata.key,
      label: <Label value={el.metadata.name} />
    }));
  }, [componentList]);

  const value = {
    value: componentName,
    label: <Label value={componentName} />
  }

  const handleSelect = (e: { value: string}) => {
    navigate(`/api-mapping/${e.value}`)
  }
  return <Select
    className={styles.selectStyle}
    onSelect={handleSelect}
    size="large"
    value={value}
    labelInValue={true}
    options={parsedOptions} />
}

export default ComponentSelect;