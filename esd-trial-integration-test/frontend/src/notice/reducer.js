import { createSlice } from "@reduxjs/toolkit";
import DateFnsUtils from "@date-io/date-fns";

const dateFns = new DateFnsUtils();

export const Slice = createSlice({
  name: "alert",
  initialState: {
    begin: {
      startTime: new Date(),
      stopTime: new Date(),
      capacity: 0,
    },
    end: {
      stopTime: new Date(),
    },
  },
  reducers: {
    updateBeginStartTime: (state, action) => {
      const { startTime } = action.payload;
      const begin = state.begin;
      const newStartTime = dateFns.setSeconds(startTime, 0);

      state.begin = {
        ...begin,
        startTime: newStartTime,
      };
    },
    updateBeginStopTime: (state, action) => {
      const { stopTime } = action.payload;
      const begin = state.begin;
      const newStopTime = dateFns.setSeconds(stopTime, 0);

      state.begin = {
        ...begin,
        stopTime: newStopTime,
      };
    },
    updateBeginCapacity: (state, action) => {
      const { capacity } = action.payload;
      const begin = state.begin;

      state.begin = {
        ...begin,
        capacity,
      };
    },
    updateEndStopTime: (state, action) => {
      const { stopTime } = action.payload;
      const end = state.end;
      const newStopTime = dateFns.setSeconds(stopTime, 0);

      state.end = {
        ...end,
        stopTime: newStopTime,
      };
    },
  },
});

export const {
  updateBeginStartTime,
  updateBeginCapacity,
  updateBeginStopTime,
  updateEndStopTime,
} = Slice.actions;

export default Slice.reducer;
