import { PageLayout } from "@/components/Layout";
import { lazy, useRef } from "react";
import styles from "./index.module.scss";

const ReleaseHistory = lazy(() => import("./components/ReleaseHistory"));

const MappingTemplateV2 = () => {
  const ref = useRef<any>();

  return (
    <PageLayout title="Mapping template release & Upgrade v2">
      <div ref={ref} className={styles.container}>
        <ReleaseHistory />
      </div>
    </PageLayout>
  );
};

export default MappingTemplateV2;
