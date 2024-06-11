export interface IPagingData<T> {
  data: T[];
  total: number;
  page: number;
  size: number;
}
