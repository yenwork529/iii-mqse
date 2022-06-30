import {createSlice} from '@reduxjs/toolkit'
import moment from 'moment';

export const Slice = createSlice(
    {
        name: 'alert',
        initialState: {
            latestTime: "",
        },
        reducers: {
            updateLatestTime: (state) => {
                state.latestTime = moment().format("yyyy-MM-dd HH:mm:ss");
            },
        },
    })

export const {updateLatestTime} = Slice.actions

export default Slice.reducer