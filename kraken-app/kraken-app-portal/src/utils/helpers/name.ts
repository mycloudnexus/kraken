export const getProductName = (product: string) => {
  switch (product) {
    case "ACCESS_E_LINE":
      return "Access Eline";
    case "UNI":
      return "UNI";
    case "INTERNET_ACCESS":
      return "Internet Access";
    case "SHARE":
      return "Share";
    default:
      return "-";
  }
};

export const parseProductName = (product: string) => {
  switch (product) {
    case "ACCESS_E_LINE":
      return "Access Eline";
    case "UNI":
      return "UNI";
    case "INTERNET_ACCESS":
      return "Internet Access";
    case "SHARE":
      return "Share";
    default:
      return (product as string).split(':').pop();
  }
};