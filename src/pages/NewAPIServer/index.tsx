import {
  useCreateNewComponent,
  useEditComponent,
  useGetComponentDetail,
  useGetProductEnvs,
} from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { COMPONENT_KIND_API_TARGET_SPEC } from "@/utils/constants/product";
import { Form, Spin, notification } from "antd";
import { get, isEmpty, set } from "lodash";
import { useNavigate, useParams } from "react-router-dom";
import AddEnv from "./components/AddEnv";
import BtnStep from "./components/BtnStep";
import SelectAPIServer from "./components/SelectAPIServer";
import styles from "./index.module.scss";
import Text from "@/components/Text";
import UploadYaml from "./components/UploadYaml";
import { useEffect } from "react";
import { decode } from "js-base64";
import jsYaml from "js-yaml";
import { transformApiData } from "@/utils/helpers/swagger";

const NewAPIServer = () => {
  const { componentId } = useParams();
  const { currentProduct: id } = useAppStore();
  const { data: componentDetail, isLoading } = useGetComponentDetail(
    id,
    componentId ?? ""
  );
  const { data: dataEnv } = useGetProductEnvs(id);
  const env = get(dataEnv, "data", []);
  const [form] = Form.useForm();
  const { mutateAsync: runCreate, isPending: loadingCreate } =
    useCreateNewComponent();
  const navigate = useNavigate();
  const { mutateAsync: runUpdate, isPending } = useEditComponent();

  const handleSave = async () => {
    try {
      form.submit();
      return;
    } catch (error) {
      return;
    }
  };

  const onFinish = async (values: any) => {
    try {
      const swaggerData = await new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(reader.result);
        reader.onerror = reject;
        reader.readAsDataURL(values.file.file);
      });
      const newSwaggerData = (swaggerData as string).replace(
        "data:application/octet-stream;base64",
        "data:application/x-yaml;base64"
      );
      const yamlContent = jsYaml.load(decode(newSwaggerData));
      const result = transformApiData(get(yamlContent, "paths", {}));

      const data = {
        ...(isEmpty(componentDetail?.id) ? {} : componentDetail),
        description: values.description,
        kind: COMPONENT_KIND_API_TARGET_SPEC,
        metadata: {
          name: values.name,
          version: 1,
          key: `mef.sonata.api-target-spec.${values.name
            ?.replace(" ", "")
            ?.substring(0, 3)
            .toLowerCase()}${new Date().getTime()}`,
          description: values.description,
        },
        spec: {
          baseSpec: {
            path: values.link,
            content: newSwaggerData,
          },
          selectedAPIs: result?.map((r) => r.api),
          environments: values.environments,
        },
      };
      if (!isEmpty(componentDetail?.id)) {
        set(data, "metadata.version", get(data, "metadata.version", 1) + 1);
      }

      const res = isEmpty(componentDetail?.id)
        ? await runCreate({
            productId: id,
            data,
          } as any)
        : await runUpdate({
            productId: id,
            componentId,
            data,
          } as any);
      notification.success({ message: res.message });
      navigate(`/component/${id}/list`);
    } catch (error) {
      notification.error({
        message: get(error, "message", "Error. Please contact administration"),
      });
    }
  };

  useEffect(() => {
    if (!isEmpty(componentDetail) && !isEmpty(componentId)) {
      const base64data = get(componentDetail, "facets.baseSpec.content");
      let swaggerData;
      let fileDecode = "";
      if (base64data) {
        fileDecode = decode(get(componentDetail, "facets.baseSpec.content"));
        swaggerData = jsYaml.load(fileDecode);
      }
      const environments = get(componentDetail, "facets.environments");
      let newEnv = {};
      const keys = Object.keys(environments);

      for (const key of keys) {
        if (!isEmpty(environments[key])) {
          set(newEnv, `is${key}`, true);
        }
      }

      form.setFieldsValue({
        name: get(componentDetail, "metadata.name"),
        description: get(componentDetail, "metadata.description"),
        link: get(componentDetail, "facets.baseSpec.path"),
        file: {
          file: isEmpty(base64data)
            ? undefined
            : new File(
                [fileDecode],
                `${get(swaggerData, "info.title", "file")}.yaml`
              ),
        },
        environments,
        ...newEnv,
      });
    }
  }, [componentDetail, componentId]);

  return (
    <Spin spinning={isLoading}>
      <Form form={form} onFinish={onFinish}>
        <div className={styles.root}>
          <div className={styles.container}>
            <div
              style={{
                display: "flex",
                flex: 1,
                flexDirection: "column",
              }}
            >
              <Text.BoldLarge>
                {isEmpty(componentId)
                  ? "Create New API server"
                  : "Edit API server"}
              </Text.BoldLarge>
              <div className={styles.paper} style={{ flex: 1 }}>
                <SelectAPIServer />
                <AddEnv form={form} env={env} />
                <UploadYaml form={form} />
              </div>
              <Form.Item noStyle shouldUpdate>
                {({ getFieldValue }) => {
                  const checkConditionEnv = () => {
                    return env.reduce((prev, curr) => {
                      return (
                        prev ||
                        (!!getFieldValue(`is${curr.name}`) &&
                          !isEmpty(getFieldValue(["environments", curr.name])))
                      );
                    }, false);
                  };
                  const disabledEnv = !checkConditionEnv();
                  return (
                    <BtnStep
                      disabled={disabledEnv}
                      loading={loadingCreate || isPending}
                      onNext={handleSave}
                    />
                  );
                }}
              </Form.Item>
            </div>
          </div>
        </div>
      </Form>
    </Spin>
  );
};

export default NewAPIServer;
