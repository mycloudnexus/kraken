import { IUser } from "@/utils/types/user.type";
import { useGetUserList } from ".";
import { isEmpty } from "lodash";

const useUser = () => {
  const { data: dataUser } = useGetUserList(
    { size: 200, page: 0 },
    { staleTime: 999999 }
  );
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
  return {
    runUser,
    findUserName,
    dataUser: dataUser?.data || [],
  };
};

export default useUser;
