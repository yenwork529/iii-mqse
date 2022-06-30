import React from "react";
import { Button } from "@material-ui/core";
import { useDispatch, useSelector } from "react-redux";
import { updateLatestTime } from "./reducer";
import { postAlert } from "./service";

const sendAlert = (dispatch) => {
  dispatch(updateLatestTime());
  postAlert();
};

export default function Alert() {
  const latestTime = useSelector((state) => state.alert.latestTime);
  const dispatch = useDispatch();

  return (
    <div>
      <h3>上次發送告警時間: {latestTime}</h3>
      <Button
        id="send-alert"
        variant="contained"
        onClick={() => sendAlert(dispatch)}
      >
        發送告警
      </Button>
    </div>
  );
}
