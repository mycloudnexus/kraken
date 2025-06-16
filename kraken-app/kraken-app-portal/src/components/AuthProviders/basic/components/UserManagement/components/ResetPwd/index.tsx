import { IUser } from "@/utils/types/user.type";
import { EllipsisOutlined } from "@ant-design/icons";
import { Dropdown } from "antd";
import { useBoolean } from "usehooks-ts";
import ResetPwdModal from "../ResetPwdModal";

type Props = {
  user: IUser;
};

const ResetPwd = ({ user }: Props) => {
  const { value: isOpen, setTrue: open, setFalse: close } = useBoolean(false);
  return (
    <>
      {isOpen && <ResetPwdModal open={isOpen} user={user} onClose={close} />}
      <Dropdown
        menu={{
          items: [{ key: "reset", label: "Reset Password", onClick: open }],
        }}
      >
        <EllipsisOutlined rotate={90} />
      </Dropdown>
    </>
  );
};

export default ResetPwd;
