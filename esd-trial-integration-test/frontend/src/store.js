import {configureStore} from '@reduxjs/toolkit'

import mainReducer from './main/reducer';
import alertReducer from './alert/reducer';
import noticeReducer from './notice/reducer';

export default configureStore({
    reducer: {
        main: mainReducer,
        alert: alertReducer,
        notice: noticeReducer,
    },
});