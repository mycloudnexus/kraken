import { EnumRightType } from "@/utils/types/common.type";
import RightAddSellerProp from "../RightAddSellerProp";
import RightAddSonataProp from "../RightAddSonataProp";
import SelectAPI from "../SelectAPI";
import SelectResponseProperty from "../SelectResponseProperty";
import SonataResponseMapping from "../SonataResponseMapping";

type RightSideProps = {
  rightSide: number;
  jsonSpec: any;
  method: string;
  handleSelectSonataProp: (value: any) => void;
  handleSelectSellerProp: (value: any) => void;
  isRequiredMapping: boolean;
};

export function RightSide({
  rightSide,
  jsonSpec,
  method,
  handleSelectSonataProp,
  handleSelectSellerProp,
  isRequiredMapping,
}: RightSideProps) {
  if (!isRequiredMapping) {
    return <SelectAPI isRequiredMapping={isRequiredMapping} />;
  }
  switch (rightSide) {
    case EnumRightType.AddSonataProp:
      return (
        <RightAddSonataProp
          spec={jsonSpec}
          method={method}
          onSelect={handleSelectSonataProp}
        />
      );
    case EnumRightType.SelectSellerAPI:
      return <SelectAPI />;

    case EnumRightType.AddSellerProp:
      return <RightAddSellerProp onSelect={handleSelectSellerProp} />;
    case EnumRightType.AddSellerResponse:
      return <SelectResponseProperty />;
    case EnumRightType.SonataResponse:
      return <SonataResponseMapping spec={jsonSpec} method={method} />;
    default:
      return <></>;
  }
}
