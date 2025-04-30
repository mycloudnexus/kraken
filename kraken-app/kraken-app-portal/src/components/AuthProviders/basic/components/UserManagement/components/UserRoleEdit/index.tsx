import Role, { ERole } from "@/components/Role";
import { IUser } from "@/utils/types/user.type";
import { Select, notification } from "antd";
import clsx from "clsx";
import { useBoolean } from "usehooks-ts";
import styles from "./index.module.scss";
import { useEditUser } from "@/hooks/user";
import { get } from "lodash";
import { renderRole } from "../UserModal";
type Props = {
  user: IUser;
  isAdmin?: boolean;
};

export const roleOptions = [
  { label: "Admin", value: ERole.ADMIN },
  { label: "User", value: ERole.USER },
];

const UserRoleEdit = ({ user, isAdmin }: Props) => {
  const {
    value: isEdit,
    setTrue: enableEdit,
    setFalse: disableEdit,
  } = useBoolean(false);
  const { mutateAsync: runEdit } = useEditUser();
  const handleChange = async (value: string) => {
    try {
      const res = await runEdit({
        ...user,
        role: value,
      } as any);
      notification.success({
        message: get(res, "message", "Success!"),
      });
      disableEdit();
    } catch (error) {
      notification.error({
        message: get(error, "reason", "Error. Please try again"),
      });
    }
  };

  if (!isAdmin) {
    return <Role role={user.role} />;
  }
  if (isAdmin && !isEdit) {
    return (
      <div role="none" onClick={enableEdit}>
        <Role role={user.role} />
      </div>
    );
  }
  return (
    <Select
      className={clsx(["role-edit", styles.role])}
      value={user.role}
      onChange={disableEdit}
      options={roleOptions}
      onSelect={handleChange}
      labelRender={renderRole}
    />
  );
};

export default UserRoleEdit;
