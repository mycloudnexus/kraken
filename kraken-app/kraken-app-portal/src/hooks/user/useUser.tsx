import { IUser } from "@/utils/types/user.type";
import { useGetCurrentUser, useGetUserList } from ".";
import { isEmpty } from "lodash";

export const useUser = () => {
  const { data: dataUser, isLoading } = useGetUserList(
    {
      size: 200,
      page: 0,
      // filterInternalUser: true
    },
    { staleTime: 999999 }
  );
  const { data: currentUser } = useGetCurrentUser();
  const runUser = () => {
    return true;
  };
  const findUserName = (id: string) => {
    if (!isEmpty(dataUser?.data)) {
      const user = dataUser?.data?.find((item: IUser) => item.id === id);
      return user?.name;
    }
    return "";
  };

  const findUserIdByEmail = (email: string) => {
    if (!isEmpty(dataUser?.data)) {
      const user = dataUser?.data?.find((item: IUser) => item.email === email);
      return user?.id;
    }
    return "";
  };
  return {
    runUser,
    isLoading,
    findUserName,
    findUserIdByEmail,
    dataUser: dataUser?.data || [],
    currentUser,
  };
};
