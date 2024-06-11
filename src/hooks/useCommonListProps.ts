import { useCallback, useState } from 'react'

export const useCommonListProps = (
  initQueryParams: Record<string, any>,
  initPagination: Record<string, any>,
  // if the pagination is separated from other parameters, set this to true
  separatePaginationFromQueryParams = false
) => {
  const [queryParams, setQueryParams] =
    useState<Record<string, any>>(initQueryParams)
  const [pagination, setPagination] =
    useState<Record<string, any>>(initPagination)
  const handlePaginationChange = useCallback(
    (current: number, pageSize: number) => {
      if (separatePaginationFromQueryParams) {
        setPagination((p) => ({
          ...p,
          current: current - 1,
          pageSize,
        }))
      } else {
        setQueryParams((q) => ({
          ...q,
          page: current - 1,
          size: pageSize,
        }))
      }
    },
    [separatePaginationFromQueryParams]
  )
  const handlePaginationShowSizeChange = useCallback(
    (current: number, pageSize: number) => {
      if (separatePaginationFromQueryParams) {
        setPagination((p) => ({
          ...p,
          current: current - 1,
          pageSize,
        }))
      } else {
        setQueryParams((q) => ({
          ...q,
          page: current - 1,
          size: pageSize,
        }))
      }
    },
    [separatePaginationFromQueryParams]
  )
  const [tableData, setTableData] = useState<any[]>([])
  return {
    queryParams,
    pagination,
    tableData,
    setQueryParams,
    setPagination,
    setTableData,
    handlePaginationChange,
    handlePaginationShowSizeChange,
  }
}
