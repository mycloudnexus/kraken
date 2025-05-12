
import { ISystemInfo } from "@/utils/types/user.type";
import BasicHeader from "../../basic/components/header";

const Header = ({ info }: Readonly<{ info?: ISystemInfo }>) => {
  return (<BasicHeader info={ info } />);
}

export default Header;