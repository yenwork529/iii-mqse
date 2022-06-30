import { buildAlertRequest } from "../dnp3/dnp3";
import { post } from "../utils/viewHelper";

export const postAlert = () => {
  const request = buildAlertRequest();
  post("alert", request);
};
