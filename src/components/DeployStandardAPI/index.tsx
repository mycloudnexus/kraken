import { Button } from "antd";
import { useBoolean } from "usehooks-ts";
import DeployStandardAPIModal from "./DeployStandardAPIModal";

type Props = {};

const DeployStandardAPI = ({}: Props) => {
  const { value: isOpen, setTrue: open, setFalse: close } = useBoolean(false);
  return (
    <>
      {isOpen && <DeployStandardAPIModal open={isOpen} onClose={close} />}
      <Button type="default" onClick={open}>
        Deploy to stage
      </Button>
    </>
  );
};

export default DeployStandardAPI;
