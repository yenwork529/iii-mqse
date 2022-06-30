import React from "react";
import { Button, TextField } from "@material-ui/core";
import DateFnsUtils from "@date-io/date-fns";
import {
  KeyboardDateTimePicker,
  MuiPickersUtilsProvider,
} from "@material-ui/pickers";
import { useDispatch, useSelector } from "react-redux";
import moment from "moment";

import { getValueFromEvent } from "../utils/viewHelper";
import {
  updateBeginCapacity,
  updateBeginStartTime,
  updateBeginStopTime,
  updateEndStopTime,
} from "./reducer";
import { postBegin, postEnd } from "./service";

export default function Notice() {
  const now = moment();
  const begin = useSelector((state) => state.notice.begin);
  const end = useSelector((state) => state.notice.end);
  const dispatch = useDispatch();

  return (
    <div>
      <h3>發送調度通知</h3>

      <h4>指令發送時間: {now.format("yyyy-MM-dd HH:mm:ss")}</h4>
      <h4>啟動</h4>
      <MuiPickersUtilsProvider utils={DateFnsUtils}>
        <KeyboardDateTimePicker
          variant="inline"
          ampm={false}
          label="啟動時間"
          value={begin.startTime}
          onChange={(date) =>
            dispatch(
              updateBeginStartTime({
                startTime: date,
              })
            )
          }
          format="yyyy/MM/dd HH:mm"
          margin="normal"
          id="begin-start-picker"
        />
        <br />

        <KeyboardDateTimePicker
          variant="inline"
          ampm={false}
          label="停止時間"
          value={begin.stopTime}
          onChange={(date) =>
            dispatch(
              updateBeginStopTime({
                stopTime: date,
              })
            )
          }
          format="yyyy/MM/dd HH:mm"
          margin="normal"
          id="begin-stop-picker"
        />
        <br />
      </MuiPickersUtilsProvider>

      <TextField
        id="capacity"
        label="調度容量"
        value={begin.capacity}
        onChange={(event) =>
          dispatch(
            updateBeginCapacity({
              capacity: getValueFromEvent(event),
            })
          )
        }
      />
      <br />

      <Button
        id="send-begin"
        variant="contained"
        onClick={() => postBegin(begin)}
      >
        發送啟動指令
      </Button>

      <hr />

      <h4>結束</h4>
      <MuiPickersUtilsProvider utils={DateFnsUtils}>
        <KeyboardDateTimePicker
          ampm={false}
          format="yyyy/MM/dd HH:mm"
          variant="inline"
          margin="normal"
          id="end-stop-picker"
          label="停止時間"
          value={end.stopTime}
          onChange={(date) =>
            dispatch(
              updateEndStopTime({
                stopTime: date,
              })
            )
          }
        />
        <br />
      </MuiPickersUtilsProvider>

      <Button id="send-end" variant="contained" onClick={() => postEnd(end)}>
        發送結束指令
      </Button>
    </div>
  );
}
