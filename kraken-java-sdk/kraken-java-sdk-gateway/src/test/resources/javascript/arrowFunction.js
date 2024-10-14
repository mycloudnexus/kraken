(jsonStr) => {
  console.log("input: " + jsonStr);
  let input = JSON.parse(jsonStr);
  let ret = {
    echo: input,
  };
  return JSON.stringify(ret);
};
