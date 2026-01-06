import { render, screen } from "@testing-library/react";
import { Select } from "antd";
import { describe, it, expect } from "vitest";

describe("Global Setup Coverage (setup.tsx)", () => {
  it("executes the global window.getComputedStyle override", () => {
    const div = document.createElement("div");
    document.body.appendChild(div);
    const style = window.getComputedStyle(div);

    expect(style).toBeDefined();
    document.body.removeChild(div);
  });

  it("uses the mocked Antd Select component", () => {
    render(
      <Select className="test-class" data-custom-prop="true">
        <div data-testid="select-child">Option</div>
      </Select>
    );

    const mockElement = screen.getByTestId("mock-antd-select");

    expect(mockElement).toBeInTheDocument();
    expect(mockElement).toHaveClass("test-class");
    expect(screen.getByTestId("select-child")).toBeInTheDocument();
  });
});