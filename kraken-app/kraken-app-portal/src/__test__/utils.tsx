/* eslint-disable react-refresh/only-export-components */
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { render as jestRender } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";

export * from '@testing-library/react'

export function render(component: React.ReactNode) {
  return jestRender(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        {component}
      </BrowserRouter>
    </QueryClientProvider>
  )
}
