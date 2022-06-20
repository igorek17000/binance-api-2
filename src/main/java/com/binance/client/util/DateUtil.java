package com.binance.client.util;

import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Slf4j
public class DateUtil {

    private DateUtil() {
    }

    public static final String DATE_FORMAT_01 = "yyyy-MM-dd HH:mm:ss";

    public static final String DATE_FORMAT_02 = "yyyy-MM-dd";

    public static final String DATE_FORMAT_03 = "yyyy-MM";

    public static final String DATE_FORMAT_04 = "yyyyMMdd";

    public static final String DATE_FORMAT_05 = "MM月dd日 HH:mm:ss";

    public static final String DATE_FORMAT_06 = "MM月dd日";

    public static final String DATE_FORMAT_07 = "yyyy年MM月dd日";

    public static final String DATE_FORMAT_08 = "yyyy/MM/dd";

    public static final String DATE_FORMAT_09 = "yyyy/MM";

    public static final String DATE_FORMAT_10 = "yyyy年MM月";

    public static final String DAY_BEGIN = " 00:00:00";

    public static final String DAY_OVER = " 23:59:59";

    public static final String MONTH_FIRST_DAY = "-01";

    public static Date getNow() {
        return new Date();
    }

    /**
     * String 时间按照指定格式转 Date 类型
     */
    public static Date formatString(String time) {
        DateTime dateTime = DateTime.parse(time);
        return dateTime.toDate();
    }

    public static Date strToDate(String dateTimeStr) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT_01);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr);
        return dateTime.toDate();
    }

    public static Date strToDate(String dateTimeStr, String format) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(format);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr);
        return dateTime.toDate();
    }

    /**
     * 获取前一个月
     */
    public static String getBeforeMonth(String date) {
        DateTime dateTime = new DateTime(date);
        DateTime beforeMonth = dateTime.minusMonths(1);
        return beforeMonth.toString(DATE_FORMAT_02);
    }

    /**
     * 指定格式获取时间
     */
    public static String formatDate(Date date, String dateFormat) {
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(dateFormat);
    }

    /**
     * 获取前一天
     */
    public static String getBeforeDay(String date) {
        DateTime dateTime = new DateTime(date);
        DateTime beforeDay = dateTime.minusDays(1);
        return beforeDay.toString(DATE_FORMAT_02);
    }

    /**
     * 时间往前推几分钟
     */
    public static Date getBeforeMinute(Date date, int minute) {
        DateTime dateTime = new DateTime(date);
        DateTime beforeDay = dateTime.minusMinutes(minute);
        return beforeDay.toDate();
    }

    /**
     * 获取月份的最后一天
     */
    public static String getMonthLastDay(String date) {
        return getBeforeDay(getAfterMonth(date + MONTH_FIRST_DAY));
    }

    /**
     * 获取后一个月
     */
    public static String getAfterMonth(String date) {
        DateTime dateTime = new DateTime(date);
        DateTime beforeMonth = dateTime.plusMonths(1);
        return beforeMonth.toString(DATE_FORMAT_03);
    }

    /**
     * 获取后n个月
     */
    public static Date getAfterMonth(String date, Integer n) {
        DateTime dateTime = new DateTime(date);
        DateTime beforeMonth = dateTime.plusMonths(n);
        return DateUtil.formatString(beforeMonth.toString(DATE_FORMAT_02));
    }

    /**
     * 获取指定的前几天
     */
    public static String getBeforeDays(String date, int days) {
        DateTime dateTime = new DateTime(date);
        DateTime beforeDay = dateTime.minusDays(days);
        return beforeDay.toString(DATE_FORMAT_02);
    }

    public static Date getBeforeDays(Date date, int days) {
        DateTime dateTime = new DateTime(date);
        return dateTime.minusDays(days).toDate();
    }

    /**
     * 获取指定日期的后几天
     */
    public static Date getAfterDate(Date date, int days) {
        DateTime dateTime = new DateTime(date);
        DateTime afterDate = dateTime.plusDays(days);
        return afterDate.toDate();
    }

    /**
     * 获取hou一天
     */
    public static String getAfterDay(String date) {
        DateTime dateTime = new DateTime(date);
        DateTime beforeDay = dateTime.plusDays(1);
        return beforeDay.toString(DATE_FORMAT_02);
    }

    /**
     * 获取指定天的开始时间
     */
    public static String getDayBegin(String day) {
        return day + DAY_BEGIN;
    }

    /**
     * 获取指定天的结束时间
     */
    public static String getDayOver(String day) {
        return day + DAY_OVER;
    }

    /**
     * 获取指定日期的上一个月
     */
    public static String getPreMonth(String date) {
        DateTime dateTime = new DateTime(date);
        DateTime preMonth = dateTime.minusMonths(1);
        return preMonth.toString(DATE_FORMAT_03);
    }

    public static String formatStrDate(String date, String dateFormat) {
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(dateFormat);
    }

    public static int getMonthEndDay(String date) {
        DateTime dateTime = new DateTime(date);
        return dateTime.dayOfMonth().getMaximumValue();
    }

    public static List<String> getDayList(String startTime, String endTime, String format) {

        // 返回的日期集合
        List<String> days = new ArrayList<>();

        DateFormat dateFormat = new SimpleDateFormat(format);
        try {
            Date start = dateFormat.parse(startTime);
            Date end = dateFormat.parse(endTime);

            Calendar tempStart = Calendar.getInstance();
            tempStart.setTime(start);

            Calendar tempEnd = Calendar.getInstance();
            tempEnd.setTime(end);
            tempEnd.add(Calendar.DATE, +1);
            while (tempStart.before(tempEnd)) {
                days.add(dateFormat.format(tempStart.getTime()));
                tempStart.add(Calendar.DAY_OF_YEAR, 1);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return days;
    }
}
