import path from "path";
import svgr from "vite-plugin-svgr";
import { defineConfig } from "vitest/config";

export default defineConfig({
  plugins: [
    svgr({
      include: "**/*.svg",
      exclude: "**/*.svg?url",
      svgrOptions: {
        exportType: "default",
      },
    }),
  ],
  test: {
    globals: true,
    environment: "jsdom",
    setupFiles: ["src/tests/setup.tsx"],
    coverage: {
      provider: "v8",
      include: ["src/pages/**/*", "src/components/**/*"],
      exclude: ["src/tests/*", "html_template/*.html", "src/services/*", "src/hooks/*"],
      extension: [".ts", ".tsx"],
      all: true,
      reporter: ["text", "html", "clover", "json", "lcov"],
      skipFull: true,
    },
  },
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
});
