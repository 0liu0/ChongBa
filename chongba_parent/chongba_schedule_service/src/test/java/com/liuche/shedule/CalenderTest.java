package com.liuche.shedule;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CalenderTest {
    public static void main(String[] args) {
//        Calendar cal = Calendar.getInstance();
//        int year = cal.get(Calendar.YEAR);
//        //比当前月份少1
//        int month = cal.get(Calendar.MONTH);
//        //date表示日期，day表示天数，所以date与day_of_month相同
//        int date = cal.get(Calendar.DATE);
//        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
//        //表示本周的第几天，从周日开始计算
//        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
//        int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
//        //12小时制
//        int hour = cal.get(Calendar.HOUR);
//        //24小时制
//        int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
//        int minute = cal.get(Calendar.MINUTE);
//        int second = cal.get(Calendar.SECOND);
//        int millisecond = cal.get(Calendar.MILLISECOND);
//        int maxDate = cal.getActualMaximum(Calendar.DATE);
//        System.out.println("现在的年份为:" + year);
//        System.out.println("现在的月份为:" + month);
//        System.out.println("现在的号为:" + date);
//        System.out.println("现在的号为:" + dayOfMonth);
//        System.out.println("现在是星期:" + dayOfWeek);
//        System.out.println("现在过了的天数为:" + dayOfYear);
//        System.out.println("现在几点:" + hour);
//        System.out.println("现在几点:" + hourOfDay);
//        System.out.println("现在几分:" + minute);
//        System.out.println("现在几秒:" + second);
//        System.out.println("现在几毫秒:" + millisecond);
//        System.out.println("本月最后一天是:" + maxDate);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE,1);
        System.out.printf("当前时间："+calendar.getTime());
        long timeInMillis = calendar.getTimeInMillis();
        System.out.println(String.valueOf(timeInMillis));
        System.out.println("你好");
    }
}
