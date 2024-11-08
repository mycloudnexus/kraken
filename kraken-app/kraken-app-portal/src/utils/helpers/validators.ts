import { UseMutateAsyncFunction } from '@tanstack/react-query';
import { isEmpty } from 'lodash';
import { IProductIdAndNameParams } from '../types/product.type';
import { isURL } from './url';

export const validateServerName = async (validateName: UseMutateAsyncFunction<any, Error, IProductIdAndNameParams, unknown>, currentProduct: string, name: string) => {
  const { data: isValid } = await validateName({ productId: currentProduct, name });
  if (isValid) {
    return Promise.resolve();
  } else {
    return Promise.reject(new Error(`The name ${name} is already taken`));
  }
};

export const validateURL = (_: unknown, value: string) => {
  if (isURL(value) || isEmpty(value)) {
    return Promise.resolve();
  }
  return Promise.reject(new Error("Please enter a valid URL"));
};