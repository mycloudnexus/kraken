import { IUser } from "@/utils/types/user.type";
import { Avatar, AvatarProps } from "antd";

export function UserAvatar({ user, ...props }: Readonly<AvatarProps & { user?: IUser }>) {
  return (
    <Avatar data-testid="username" size={64} {...props}>
      {user?.name?.[0]}
    </Avatar>
  )
}
