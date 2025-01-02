import BreadCrumb from "@/components/Breadcrumb";
import DeleteApiButton from "@/components/DeleteApiButton";
import { PageLayout } from "@/components/Layout";
import {
  useCreateNewComponent,
  useDeleteApiServer,
  useEditComponent,
  useGetComponentDetailV2,
  useGetProductEnvs,
} from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { COMPONENT_KIND_API_TARGET_SPEC } from "@/utils/constants/product";
import { transformApiData } from "@/utils/helpers/swagger";
import { Form, Spin, notification } from "antd";
import jsYaml from "js-yaml";
import { get, isEmpty, set } from "lodash";
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import AddEnv from "./components/AddEnv";
import BtnStep from "./components/BtnStep";
import SelectAPIServer from "./components/SelectAPIServer";
import UploadYaml from "./components/UploadYaml";
import styles from "./index.module.scss";
import renderRequiredMark from '@/components/RequiredFormMark';
import { decodeFileContent } from "@/utils/helpers/base64";

const NewAPIServer = () => {
  const { componentId } = useParams();
  const { currentProduct: id } = useAppStore();
  const [openMappingDrawer, setOpenMappingDrawer] = useState(false);
  const { mutateAsync: deleteApiServer, isPending: isDeletePending } =
    useDeleteApiServer();
  const { data: componentDetail, isLoading } = useGetComponentDetailV2(
    id,
    (componentId ?? "").replace(".api.", ".api-spec.")
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
      const yamlContent = jsYaml.load(decodeFileContent(newSwaggerData));
      const result = transformApiData(get(yamlContent, "paths", {}));

      const data = {
        ...(isEmpty(componentDetail?.id) ? {} : componentDetail),
        description: values.description,
        kind: COMPONENT_KIND_API_TARGET_SPEC,
        metadata: {
          name: values.name,
          version: 1,
          key: get(
            componentDetail,
            "metadata.key",
            `mef.sonata.api-target-spec.${values.name
              ?.replace(" ", "")
              ?.substring(0, 3)
              .toLowerCase()}${new Date().getTime()}`
          ),
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
        set(
          data,
          "metadata.version",
          get(componentDetail, "metadata.version", 1) + 1
        );
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
        message: get(
          error,
          "reason",
          get(error, "message", "Error. Please contact administration")
        ),
      });
    }
  };

  useEffect(() => {
    if (!isEmpty(componentDetail) && !isEmpty(componentId)) {
      const base64data = get(componentDetail, "facets.baseSpec.content");
      let swaggerData;
      let fileDecode = "";
      if (base64data) {
        fileDecode = decodeFileContent(get(componentDetail, "facets.baseSpec.content"));
        swaggerData = jsYaml.load(fileDecode);
      }
      const environments = get(componentDetail, "facets.environments");
      const newEnv = {};
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
                `${get(swaggerData, "info.title", "file")}.yaml`,
                {
                  type: "application/x-yaml",
                }
              ),
        },
        environments,
        ...newEnv,
      });
    }
  }, [componentDetail, componentId]);

  return (
    <PageLayout
      title={
        <BreadCrumb
          lastItem={
            isEmpty(componentId) ? "Create API server" : "Edit API server"
          }
          mainUrl={`/component/${id}/list`}
        />
      }
    >
      <Spin spinning={isLoading || isDeletePending}>
        <Form
          className={styles.container}
          form={form}
          onFinish={onFinish}
          requiredMark={renderRequiredMark}
        >
          <main id="12" className={styles.paper} style={{ flex: 1 }}>
            <div id="12" style={{ maxWidth: "60%", minWidth: 600 }}>
              <SelectAPIServer name={get(componentDetail, "metadata.name")}/>
              <AddEnv form={form} env={env} />
              <UploadYaml form={form} />
            </div>
          </main>

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
                >
                  {!isEmpty(componentId) && (
                    <DeleteApiButton
                      openMappingDrawer={openMappingDrawer}
                      deleteCallback={deleteApiServer}
                      setOpenMappingDrawer={setOpenMappingDrawer}
                      item={componentDetail}
                      isInEditMode={true}
                    />
                  )}
                </BtnStep>
              );
            }}
          </Form.Item>
        </Form>
      </Spin>
    </PageLayout>
  );
};

export default NewAPIServer;
