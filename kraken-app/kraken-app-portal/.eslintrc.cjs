/* eslint-disable @typescript-eslint/no-var-requires */
const path = require("path");

module.exports = {
  root: true,
  env: { browser: true, es2020: true },
  extends: [
    "eslint:recommended",
    "plugin:@typescript-eslint/recommended",
    "plugin:react-hooks/recommended",
    "eslint-config-prettier",
    "prettier",
  ],
  settings: {
    react: {
      version: "detect",
    },
    "import/resolver": {
      node: {
        paths: [path.resolve(__dirname, "")],
        extensions: [".js", ".jsx", ".ts", ".tsx"],
      },
      typescript: {
        project: path.resolve(__dirname, "./tsconfig.json"),
      },
    },
  },
  ignorePatterns: ["dist", ".eslintrc.cjs"],
  parser: "@typescript-eslint/parser",
  plugins: ["prettier", "react-refresh"],
  rules: {
    "@typescript-eslint/no-unused-vars": ["warn", { ignoreRestSiblings: true }],
    "react-refresh/only-export-components": "off",
    "@typescript-eslint/no-explicit-any": "off",
    "react-hooks/exhaustive-deps": "off",
  },
};
