import { FormInstance } from "antd";

export const form: FormInstance<any> = {
  getFieldValue: vi.fn().mockReturnValue(null),
  setFieldValue: vi.fn(),
  scrollToField: vi.fn(),
  getFieldInstance: vi.fn(),
  getFieldsValue: vi.fn() as any,
  getFieldError: vi.fn(),
  getFieldsError: vi.fn(),
  getFieldWarning: vi.fn(),
  isFieldsTouched: vi.fn() as any,
  isFieldTouched: vi.fn(),
  isFieldValidating: vi.fn(),
  isFieldsValidating: vi.fn(),
  resetFields: vi.fn(),
  setFields: vi.fn(),
  setFieldsValue: vi.fn(),
  validateFields: vi.fn() as any,
  submit: vi.fn(),
};
