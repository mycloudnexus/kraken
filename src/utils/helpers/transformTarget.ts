const transformTarget = (from: string, fromSrc: string) => {
  switch (fromSrc) {
    case "PATH":
      return from.replace("@{{", "@{{path.");
    case "QUERY":
      return from.replace("@{{", "@{{query.");
    case "HYBRID":
      return "hybrid." + from;
    default:
      return from;
  }
};

export default transformTarget;
