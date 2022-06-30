import { buildBeginRequest, buildEndRequest } from "../dnp3/dnp3";
import { post } from "../utils/viewHelper";

export const postBegin = ({ startTime, stopTime, capacity }) => {
  const request = buildBeginRequest(startTime, stopTime, capacity);
  post("begin", request);
};

export const postEnd = ({ stopTime }) => {
  const request = buildEndRequest(stopTime);
  post("end", request);
};
