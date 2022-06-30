package org.iii.esd.control;

import java.util.Date;

public class TimeSlot {
    public final static int TIMESLOT_LEN_15MIN = 96;
    public final static int TIMESLOT_LEN_3MIN = 480;
    public final static int INDEX_MIN = 0;  //陣列最小值索引
    public final static int INDEX_MAX = 1;  //陣列最大值索引

    public final static int PERIOD_UNKNOWN = 0;  //未知時段
    public final static int PERIOD_PEAK_TIME = 1;  //尖峰/半尖峰時段
    public final static int PERIOD_DR_TIME = 2;  //DR時段
    public final static int PERIOD_LOAD_SHIFT_TIME = 3;  //墊基時段
    //      public const int PERIOD_HALF_PEAK_TIME = 4;  //半尖峰時段
    public final static int PERIOD_OFF_PEAK_TIME = 5;  //離峰時段

    public int[] Of025H = new int[TIMESLOT_LEN_15MIN];

    public TimeSlot() {
        for (int i = 0; i < TIMESLOT_LEN_15MIN; i++) {
            if (i <= 29) { Of025H[i] = PERIOD_OFF_PEAK_TIME; } else if (i > 29 && i <= 51) { Of025H[i] = PERIOD_PEAK_TIME; }
            //                else if (i > 51 && i <= 67)  // 13:00-17:00
            else if (i > 51 && i <= 59)  // 13:00-15:00
            { Of025H[i] = PERIOD_DR_TIME; } else { Of025H[i] = PERIOD_OFF_PEAK_TIME; }
        }
    }

    @SuppressWarnings("deprecation")
    public static int GetOf25HIndex(Date dateTime) {
        return (dateTime.getHours() * 4 + dateTime.getMinutes() / 15 + 95) % 96;
    }

    //所屬時段索引值
    @SuppressWarnings("deprecation")
    public static int GetIndex(Date dateTime) {
        int minute = dateTime.getMinutes();
        if (minute >= 0 && minute < 15) { return 0; } else if (minute >= 15 && minute < 30) { return 1; } else if (minute >= 30 &&
                minute < 45) { return 2; } else if (minute <= 59) {
            return 3;
        } else { return -1; }
    }

    public static int GetHourlyAddedTime(int idx) {
        int value = 0;
        switch (idx) {
            case 0:
                value = 60;
                break;
            case 1:
                value = 45;
                break;
            case 2:
                value = 30;
                break;
            case 3:
                value = 15;
                break;
            default:
                value = 0;
                break;
        }
        return value;
    }

    public int GetPeriodOf025HIndex(int timeIndex) {
        return Of025H[timeIndex];
    }

    public int GetWhole3Min(int minute) {
        return minute % 15 / 3 + 1;

    }

    public void SetRaiseIndexOf025H(int[] raiseIdxOf025H) {
		/*
					if (raiseIdxOf025H[0] >=0 && raiseIdxOf025H[1] >=0)
					{
						Of025H[raiseIdxOf025H[INDEX_MIN]] = PERIOD_LOAD_SHIFT_TIME;
						Of025H[raiseIdxOf025H[INDEX_MAX]] = PERIOD_LOAD_SHIFT_TIME;
					}
		*/
        for (int i = 0; i < raiseIdxOf025H.length; i++) {
            if (raiseIdxOf025H[i] >= 0) { Of025H[raiseIdxOf025H[i]] = PERIOD_LOAD_SHIFT_TIME; }
        }
    }

    public boolean IsLast3Min(int minute) {
        boolean flag = false;
        if (GetWhole3Min(minute) == 5) { flag = true; }
        return flag;
    }
}
