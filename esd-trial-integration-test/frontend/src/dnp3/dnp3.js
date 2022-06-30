import {
  getEsdServerUrl,
  getTgId,
  getQseId,
  getServiceType,
} from "../utils/viewHelper";

const buildBaseRequest = () => ({
  serverUrl: getEsdServerUrl(),
  tgId: getTgId(),
  qseId: getQseId(),
  serviceType: getServiceType(),
});

export const buildAlertRequest = () => {
  const baseRequest = buildBaseRequest();
  return {
    ...baseRequest,
    alertTime: new Date(),
  };
};

export const buildBeginRequest = (startTime, stopTime, capacity) => {
  const baseRequest = buildBaseRequest();
  return {
    ...baseRequest,
    startTime,
    stopTime,
    capacity,
  };
};

export const buildEndRequest = (stopTime) => {
  const baseRequest = buildBaseRequest();
  return {
    ...baseRequest,
    stopTime,
  };
};
