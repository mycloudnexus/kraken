import Text from "@/components/Text";
import {
  useDeployProduct,
  useGetProductComponentVersions,
  useGetProductEnvs,
} from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { Checkbox, Collapse, Modal, Radio, notification } from "antd";
import { flatten, omit } from "lodash";
import { useCallback, useMemo, useState } from "react";
import styles from "./index.module.scss";

interface Props {
  open: boolean;
  setOpen: (value: boolean) => void;
}
const ModalNewDeployment = ({ open, setOpen }: Readonly<Props>) => {
  const { currentProduct } = useAppStore();
  const { data: envs, isLoading: loadingEnvs } = useGetProductEnvs(
    currentProduct,
    open
  );
  const { data: componentVersions, isLoading: loadingComponentVersions } =
    useGetProductComponentVersions(currentProduct, open);
  const { mutateAsync: deployProduct, isPending: deploying } =
    useDeployProduct();
  const [selectEnv, setSelectEnv] = useState<string | undefined>();
  const [selectVersion, setSelectVersion] = useState<Record<string, string[]>>(
    {}
  );
  const handleCancel = () => {
    setOpen(false);
  };
  const envOptions = useMemo(() => {
    if (!loadingEnvs) {
      return envs?.data?.map((env) => ({
        label: env.name,
        value: env.id,
      }));
    }
    return [];
  }, [envs, loadingEnvs]);
  const handleCheckboxOnChange = useCallback(
    (key: string) => (checkedValues: string[]) =>
      setSelectVersion((sv) => {
        if (checkedValues.length) {
          return {
            ...sv,
            [key]: checkedValues,
          };
        }
        return omit(sv, key);
      }),
    []
  );
  const componentVersionCollapseItems = useMemo(() => {
    if (!loadingComponentVersions) {
      return componentVersions?.map((product) => ({
        key: product.key,
        label: <Text.NormalSmall>{product.name}</Text.NormalSmall>,
        children: (
          <Checkbox.Group
            options={product.componentVersions.map(({ id, version }) => ({
              label: version,
              value: id,
              disabled:
                selectVersion?.[product.key]?.length > 0 &&
                selectVersion?.[product.key]?.includes(id) === false,
            }))}
            className={styles.verticalRadio}
            onChange={handleCheckboxOnChange(product.key)}
          />
        ),
      }));
    }
    return [];
  }, [
    componentVersions,
    loadingComponentVersions,
    selectVersion,
    handleCheckboxOnChange,
  ]);
  const handleOk = async () => {
    const allSelectedVersions = flatten(Object.values(selectVersion));
    const selectedComponentVersions = flatten(
      componentVersions?.map((cv) => cv.componentVersions)
    )
      .filter((v) => allSelectedVersions.includes(v.id))
      .map(({ id, name, version, key }) => ({ id, name, version, key }));
    try {
      await deployProduct({
        product: currentProduct,
        envId: selectEnv,
        data: selectedComponentVersions,
      } as any);
      notification.success({ message: "Deploy product successfully!" });
      setOpen(false);
    } catch {
      notification.error({ message: "Error when deploy product!" });
    }
  };

  return (
    <Modal
      title="New deployment"
      open={open}
      onOk={handleOk}
      onCancel={handleCancel}
      okText="Deploy"
      okButtonProps={{
        loading: deploying,
      }}
    >
      <Text.BoldLarge className={styles.title}>Environment</Text.BoldLarge>
      <Radio.Group
        options={envOptions}
        style={{ paddingLeft: 8, marginBottom: 12 }}
        onChange={({ target: { value } }) => setSelectEnv(value)}
        className={styles.verticalRadio}
      />
      <Text.BoldLarge className={styles.title}>
        Select API components to deploy
      </Text.BoldLarge>
      <Collapse
        ghost
        items={componentVersionCollapseItems}
        className={styles.collapse}
      />
    </Modal>
  );
};

export default ModalNewDeployment;
