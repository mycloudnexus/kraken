import { Tag } from "antd";

type Props = {
  role: string;
};

export enum ERole {
  ADMIN = "ADMIN",
  USER = "USER",
}

const Role = ({ role }: Props) => {
  switch (role) {
    case ERole.ADMIN:
      return <Tag color="red">Admin</Tag>;
    case ERole.USER:
      return <Tag color="blue">User</Tag>;
    default:
      return <Tag>{role}</Tag>;
  }
};

export default Role;
