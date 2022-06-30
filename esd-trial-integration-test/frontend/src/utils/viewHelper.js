import axios from "axios";

export const getValueFromEvent = (event) => event.target.value;

export const getEsdServerUrl = () => sessionStorage.getItem("esdServerUrl");

export const getTgId = () => sessionStorage.getItem("tgId");

export const getQseId = () => sessionStorage.getItem("qseId");

export const getServiceType = () => parseInt(sessionStorage.getItem("serviceType"));

export const post = (command, request) => {
  const url = `/api/command/${command}`;

  axios
    .post(url, request)
    .then(function (response) {
      console.log("response: ", response);
    })
    .catch(function (error) {
      console.log("error: ", error);
    });
};
