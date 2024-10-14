(jsonStr) => {
    console.log("input: " + jsonStr);
    let input = JSON.parse(jsonStr);
    let ret = {
      echo_request: input,
    };
    return JSON.stringify(ret);
  };
