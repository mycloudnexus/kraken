import { Text } from "@/components/Text";
import {
  useDeployProduct,
  useGetProductComponentVersions,
} from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import type { IComponentVersion } from "@/utils/types/component.type";
import { Select, Modal, notification, Form, Tag, Spin } from "antd";
import { useCallback, useEffect } from "react";
import styles from "./index.module.scss";

interface Props {
  open: boolean;
  setOpen: (value: boolean) => void;
  runningComponent: any;
  currentEnvId: string;
}
const ModalNewDeployment = ({
  open,
  setOpen,
  runningComponent,
  currentEnvId = "",
}: Readonly<Props>) => {
  const { currentProduct } = useAppStore();

  const { data: componentVersions, isLoading: loadingComponentVersions } =
    useGetProductComponentVersions(currentProduct, open);
  const { mutateAsync: deployProduct, isPending: deploying } =
    useDeployProduct();

  const [form] = Form.useForm();
  const handleCancel = () => {
    setOpen(false);
  };

  useEffect(() => {
    if (!componentVersions?.length) return;
    const r = new Array(componentVersions.length).fill(undefined);

    form.setFieldsValue({
      versions: r,
    });
  }, [componentVersions, form]);

  const onFinish = useCallback(async () => {
    if (!componentVersions || !currentEnvId) return;
    const values = await form.validateFields();
    const t = values.versions
      ?.filter((v: any) => v)
      ?.map((i: string) => {
        const [index = 0, versionId] = i.split("@");
        const list =
          componentVersions[Number(index)].componentVersions ??
          ([] as IComponentVersion[]);

        const { id, name, version, key } =
          list?.find((f) => f.id === versionId) ?? {};
        return { id, name, version, key };
      });

    try {
      await deployProduct({
        productId: currentProduct,
        envId: currentEnvId,
        data: t,
      } as any);
      notification.success({ message: "Deploy product successfully!" });
      setOpen(false);
    } catch {
      notification.error({ message: "Error when deploy product!" });
    }
  }, [componentVersions, currentEnvId]);

  const getOptions = useCallback(
    (i: number) => {
      if (!componentVersions) return [];
      const t = componentVersions[i];

      const currentVersion =
        runningComponent?.data.find((f: any) => f.id === currentEnvId)
          ?.components ?? [];

      return t.componentVersions.map((c) => {
        const runningVersion = currentVersion
          .map((v: any) => v.id)
          ?.includes(c.id);
        return {
          label: (
            <div>
              Version {c.version}
              <span>
                {runningVersion ? (
                  <Tag
                    bordered={false}
                    color="success"
                    style={{ marginLeft: 4 }}
                  >
                    Running
                  </Tag>
                ) : (
                  ""
                )}
              </span>
            </div>
          ),
          value: `${i}@${c.id}`,
          disabled: runningVersion,
        };
      });
    },
    [componentVersions, runningComponent, currentEnvId]
  );
  return (
    <Modal
      title="New deployment"
      open={open}
      onOk={onFinish}
      onCancel={handleCancel}
      okButtonProps={{
        loading: deploying,
        disabled: deploying,
      }}
      cancelButtonProps={{ disabled: deploying }}
      maskClosable={false}
    >
      <Text.BoldLarge className={styles.title}>
        Select API components to deploy
      </Text.BoldLarge>
      <Spin spinning={loadingComponentVersions}>
        <Form
          name="componentVersion"
          onFinish={onFinish}
          style={{ maxWidth: 600 }}
          layout="vertical"
          form={form}
          className={styles.formContainer}
        >
          <Form.List
            name="versions"
            rules={[
              {
                validator: async () => {
                  const { versions } = form.getFieldsValue();
                  const haveVersion = versions.some((v: any) => v);
                  if (!haveVersion) {
                    return Promise.reject(
                      new Error(
                        "Please select version for at least one component"
                      )
                    );
                  }
                  return Promise.resolve();
                },
              },
            ]}
          >
            {(fields, _, { errors }) => {
              return (
                <>
                  {fields.map((field, index) => (
                    <Form.Item
                      label={componentVersions?.[index].name}
                      required={false}
                      key={field.key}
                    >
                      <Form.Item {...field} noStyle>
                        <Select
                          placeholder="No selection"
                          style={{ width: "60%" }}
                          options={getOptions(index)}
                        />
                      </Form.Item>
                    </Form.Item>
                  ))}
                  <Form.Item>
                    <Form.ErrorList errors={errors} />
                  </Form.Item>
                </>
              );
            }}
          </Form.List>
        </Form>
      </Spin>
    </Modal>
  );
};

export default ModalNewDeployment;
