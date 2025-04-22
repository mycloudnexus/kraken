export const getProductName = (productTypes: string[], product: string) => {
  const prod = productTypes.find(prod => prod.split(':').shift() === product);
  return parseProductName(prod);
};

export const parseProductName = (product?: string) => {
  switch (product) {
    case "ACCESS_E_LINE":
      return "Access Eline";
    case "UNI":
      return "UNI";
    case "SHARE":
      return "Shared";
    default:
      return product?.split(':')?.pop();
  }
};

export const parseProductValue = (product: string) => {
  return product.split(':').shift();
};