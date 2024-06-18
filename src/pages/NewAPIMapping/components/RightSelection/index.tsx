import SelectAPI from "../SelectAPI";

type Props = {
  onSelectAPI?: (value: any) => void;
};

const RightSelection = ({ onSelectAPI }: Props) => {
  return (
    <div>
      <SelectAPI onSelect={onSelectAPI} />
    </div>
  );
};

export default RightSelection;
