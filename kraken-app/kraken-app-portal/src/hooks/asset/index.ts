import { getAsset } from "@/services/asset";
import { useQuery } from "@tanstack/react-query";

export const ASSET_CACHE_KEYS = {
  get_asset: "get_asset",
};
export const useGetAsset = (id: string) => {
  return useQuery<any, Error>({
    queryKey: [ASSET_CACHE_KEYS.get_asset, id],
    queryFn: () => getAsset(id),
    enabled: Boolean(id),
    select: (data) => data?.data,
  });
};
