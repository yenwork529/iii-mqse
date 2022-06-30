import { createSlice } from "@reduxjs/toolkit";

export const Slice = createSlice({
  name: "main",
  initialState: {
    config: {
      esdServerUrl: "",
      tgId: "",
      qseId: "",
      serviceType: 4,
    },
  },
  reducers: {
    updateEsdServerUrl: (state, action) => {
      const { esdServerUrl } = action.payload;
      state.config.esdServerUrl = esdServerUrl;
    },

    updateTgId: (state, action) => {
      const { tgId } = action.payload;
      state.config.tgId = tgId;
    },

    updateQseId: (state, action) => {
      const { qseId } = action.payload;
      state.config.qseId = qseId;
    },

    updateServiceType: (state, action) => {
      const { serviceType } = action.payload;
      state.config.serviceType = serviceType;
    },
  },
});

export const {
  updateEsdServerUrl,
  updateTgId,
  updateQseId,
  updateServiceType,
} = Slice.actions;

export default Slice.reducer;
