package kr.co.bocare.sms;

import java.util.Calendar;
import java.util.Date;

public class Util {
    private Util() {
    }

    public static Date addOneDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1);
        return calendar.getTime();
    }
}
