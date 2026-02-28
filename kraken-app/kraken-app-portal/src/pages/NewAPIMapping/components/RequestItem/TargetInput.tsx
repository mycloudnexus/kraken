import { IRequestMapping } from "@/utils/types/component.type";
import { CommonInput } from "./CommonInput";

export function TargetInput({
  item,
  index,
}: Readonly<{ item: IRequestMapping; index: number }>) {
  return <CommonInput item={item} index={index} isSource={false} />;
}
