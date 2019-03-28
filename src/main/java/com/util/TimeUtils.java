package com.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * @author Chris Yu
 */
public class TimeUtils {

	public static String[] MONTH_NAMES = {"Jan", "Feb", "Mar", "Apr", "May",
			"Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

	public static Date stringToDate(String s) {
		long l = stringToTimeMillis(s);
		return new Date(l);

	}

	public static Date stringToDate2(String date) {
		SimpleDateFormat[] possibleFormats = new SimpleDateFormat[]{
				new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
				new SimpleDateFormat("yyyy-MM-dd"),
				new SimpleDateFormat("yyyy-MM-dd HH:mm"),
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"),
                new SimpleDateFormat(("yyyyMMddHHmmss"))};

		date = StringUtils.nullToBlank(date).replace('/', '-');
		Date retVal = null;
		for (SimpleDateFormat f : possibleFormats) {
			// f.setLenient(false);
			try {
				retVal = f.parse(date);
			} catch (ParseException e) {
			}
		}
		return retVal;
	}

	public static Date stringToDate2(String date, String format) {
		Date retVal = null;
		try {
			SimpleDateFormat f = new SimpleDateFormat(format);
			// f.setLenient(false);
			retVal = f.parse(date);
		} catch (ParseException e) {
		}
		return retVal;
	}

	public static Date parseDate(String dateStr, String format)
			throws ParseException {
		DateFormat df = new SimpleDateFormat(format, Locale.ENGLISH);
		return df.parse(dateStr);
	}

	public static String addMonth(String ori, int monthes) {
		Date oridate = stringToDate(ori);
		Calendar c = Calendar.getInstance();
		c.setTime(oridate);
		c.add(Calendar.MONTH, monthes);
		String pattern = "yyyy-MM-dd";
		DateFormat dateFormat = new SimpleDateFormat(pattern);
		return dateFormat.format(c.getTime());
	}

	/**
	 * convert string formated time to long millis time,such as YYYYMMDDhhmmss
	 * 0000-00-00 00:00:00.000, 0000-00-00 00:00:00, 0000-00-00
	 *
	 * @param s java.lang.String
	 * @return long
	 */
	public static long stringToTimeMillis(String s) {
		if (s == null || s.length() == 0)
			return 0;
		s = s.replace("/", "-").trim();
		int year = 0;
		int month = 0;
		int day = 0;
		int hour = 0;
		int minute = 0;
		int second = 0;
		int mills = 0;
		Calendar cal = Calendar.getInstance();
		long l = 0;

		String dayPart = "";
		String timePart = "";
		if (s.indexOf(" ") > 0) {
			dayPart = s.substring(0, s.indexOf(" "));
			timePart = s.substring(s.indexOf(" ")).trim();
		} else if (s.indexOf("-") > 0) {
			dayPart = s;
		} else if (s.indexOf(":") > 0) {
			timePart = s;
		} else {
			return 0;
		}

		String[] dayParts = dayPart.split("-");
		if (dayParts.length != 3) {
			return 0;
		}
		year = Integer.parseInt(dayParts[0]);
		month = Integer.parseInt(dayParts[1]);
		day = Integer.parseInt(dayParts[2]);

		timePart = StringUtils.replace(timePart, ".", ":");
		timePart = StringUtils.replace(timePart, ",", ":");
		String[] timeParts = timePart.split(":");
		if (timeParts.length == 3) {
			hour = Integer.parseInt(timeParts[0]);
			minute = Integer.parseInt(timeParts[1]);
			second = Integer.parseInt(timeParts[2]);
		} else if (timeParts.length == 4) {
			hour = Integer.parseInt(timeParts[0]);
			minute = Integer.parseInt(timeParts[1]);
			second = Integer.parseInt(timeParts[2]);
			mills = Integer.parseInt(timeParts[3]);
		} else if (timeParts.length == 2) {
			hour = Integer.parseInt(timeParts[0]);
			minute = Integer.parseInt(timeParts[1]);
		}

		cal.set(year, month - 1, day, hour, minute, second);
		cal.set(Calendar.MILLISECOND, mills);
		l = cal.getTime().getTime();

		return l;
	}

	public static long calCourseTime(String s) {
		if (s == null)
			return 0;
		s = s.trim();
		if (s.lastIndexOf(":") != -1) {
			StringTokenizer st = new StringTokenizer(s, ":");
			if (st.countTokens() >= 3) {
				int h = NumUtils.parseInt(st.nextToken());
				int m = NumUtils.parseInt(st.nextToken());
				String second = st.nextToken();
				float se = 0;
				if (second.indexOf(".") != -1)
					se = NumUtils.parseFloat(second);
				else
					se = NumUtils.parseInt(second);
				return (long) ((((h * 60 + m) * 60) + se) * 1000);
			}
		}
		return 0;
	}

	/**
	 * @param date int
	 * @return java.lang.String
	 */
	public static String addDate(int addDate, long date) {

		long dates = 0;
		long dayminis = 1000 * 60 * 60 * 24;
		dates = dayminis * addDate;

		return dateToString(dates + date);
	}

	public static String todayString() {
		return addDate(0, System.currentTimeMillis());
	}

	public static String yeaterdayString() {
		return addDate(-1, System.currentTimeMillis());
	}

	/**
	 * convet long millis to data yyyy-MM-dd
	 *
	 * @param date java.lang.Long
	 * @return java.lang.String
	 */
	public static String dateToString(long l) {
		Date date = new Date(l);
		return dateToString(date);
	}

	public static String dateToString(Date d) {
		return dateToString(d, "yyyy-MM-dd");
	}

	public static String datetimeToString(Date d) {
		return dateToString(d, "yyyy-MM-dd HH:mm");
	}

	public static String dateToStringFull(Date d) {
		return dateToString(d, "yyyy-MM-dd HH:mm:ss");
	}

	public static String dateToString(Date d, String format) {
		if (d == null || !StringUtils.isValid(format)) {
			return "";
		}
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(d);
	}

	public static String dateToTime(Date d) {
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
		return formatter.format(d);
	}

	/**
	 * convet long millis to time string yyyy-MM-dd HH:mm:ss
	 *
	 * @param l long
	 * @return java.lang.String
	 */
	public static String timeMillisToString(long l) {
		Date date = new Date(l);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(date);
	}

	public static String timeMillisToString(long l, String format) {
		Date date = new Date(l);
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(date);
	}

	/**
	 * convet long millis to time string in length HH:mm:ss
	 *
	 * @param l long
	 * @return java.lang.String
	 */
	public static String timeToString(long l) {
		String sign = "";
		if (l < 0) {
			sign = "-";
			l = -l;
		}
		l = Math.round(l / 1000.0);
		long h = l / 3600;
		l = l % 3600;
		long m = l / 60;
		l = l % 60;
		long s = l;
		String H = h + "";
		String M = m + "";
		String S = s + "";
		if (h < 10)
			H = "0" + H;
		if (m < 10)
			M = "0" + M;
		if (s < 10)
			S = '0' + S;
		return sign + H + ":" + M + ":" + S;
	}

	/**
	 * @param l , the minuts
	 * @return hhh:mm
	 */
	public static String minutesToString(long l) {
		String sign = "";
		if (l < 0) {
			sign = "-";
			l = -l;
		}
		long h = l / 60;
		long m = l % 60;
		String H = h + "";
		String M = m + "";
		if (h < 10)
			H = "0" + H;
		if (m < 10)
			M = "0" + M;
		return sign + H + ":" + M;
	}

	protected static Calendar todayzero() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.clear(Calendar.MINUTE);
		calendar.clear(Calendar.SECOND);
		return calendar;
	}

	public static Date today() {
		return todayzero().getTime();
	}

	public static int thisyear() {
		return todayzero().get(Calendar.YEAR);
	}
	public static int thismonth() {
		return todayzero().get(Calendar.MONDAY);
	}

	public static Date tomorrow() {
		Calendar calendar = todayzero();
		calendar.add(Calendar.DATE, 1);
		return calendar.getTime();
	}

	public static Date yesterday() {
		Calendar calendar = todayzero();
		calendar.add(Calendar.DATE, -1);
		return calendar.getTime();
	}

	public static Date firstDate() {
		return new Date(0);
	}

	public static String convertDigiToHalfHour(int digi) {
		String result = "";
		int hour = digi / 2;
		int min = 0;
		if (digi % 2 != 0)
			min = 30;
		if (hour < 10)
			result += "0";
		result += hour;
		result += ":";
		if (min < 10)
			result += "0";
		result += min;
		return result;
	}

	/**
	 * @param endDate
	 * @param begindate
	 * @return
	 */
	public static long days(Date endDate, Date begindate) {
		long gap = endDate.getTime() - begindate.getTime();
		long days = gap / (1000 * 60 * 60 * 24);
		return days + 1;
	}

	public static boolean isIn(Date begin, Date end) {
		String today = dateToString(System.currentTimeMillis());
		String b = dateToString(begin);
		String e = dateToString(end);
		return ((today.compareTo(b) >= 0) && (today.compareTo(e) <= 0));
	}

	public static int getMonth(int i) {
		int thismonth = Calendar.getInstance().get(Calendar.MONTH);
		int m = (thismonth + i) % 12 + 1;
		if (m <= 0)
			m += 12;
		return m;
	}

	public static boolean isInTimeRangle(String timeRange) {
		// 08:00-22:00
		if (!StringUtils.isValid(timeRange)) {
			return true;
		}
		try {
			Calendar cal = Calendar.getInstance();
			int beginHour = Integer.parseInt(timeRange.substring(0,
					timeRange.indexOf(":")));
			int beginMin = Integer.parseInt(timeRange.substring(
					timeRange.indexOf(":") + 1, timeRange.indexOf("-")));
			int endHour = Integer.parseInt(timeRange.substring(
					timeRange.indexOf("-") + 1, timeRange.lastIndexOf((":"))));
			int endMin = Integer.parseInt(timeRange.substring(timeRange
					.lastIndexOf(":") + 1));

			if (beginHour >= 0 && beginHour <= 24 && beginMin >= 0
					&& beginMin <= 59 && endHour >= 0 && endHour <= 24
					&& endMin >= 0 && endMin <= 59) {
				int currHour = cal.get(Calendar.HOUR_OF_DAY);
				int currMin = cal.get(Calendar.MINUTE);
				return (currHour * 60 + currMin) >= (beginHour * 60 + beginMin)
						&& (currHour * 60 + currMin) <= (endHour * 60 + endMin);
			}

		} catch (Exception e) {
			return false;
		}

		return false;
	}

	public static boolean isInDateTimeRange(String begin, String end) {

		long now = System.currentTimeMillis();

		if (begin.length() == "2000-01-01".length()) {
			begin += " 00:00:00";
		}
		if (end.length() == "2000-01-01".length()) {
			end += " 24:00:00";
		}

		boolean isBeginOk = StringUtils.isValid(begin)
				&& stringToTimeMillis(begin) <= now;
		boolean isEndOk = StringUtils.isValid(end)
				&& stringToTimeMillis(end) >= now;

		return isBeginOk && isEndOk;
	}

	/*
	 * 是否比开始时间早
	 */
	public static boolean isLessThan(String begin) {

		long now = System.currentTimeMillis();

		if (begin.length() == "2000-01-01".length()) {
			begin += " 00:00:00";
		}

		boolean isLessThan = StringUtils.isValid(begin)
				&& stringToTimeMillis(begin) > now;

		return isLessThan;
	}

	/*
	 * 是否比结束时间晚
	 */
	public static boolean isGreaterThan(String end) {

		long now = System.currentTimeMillis();

		if (end.length() == "2000-01-01".length()) {
			end += " 24:00:00";
		}

		boolean isGreaterThan = StringUtils.isValid(end)
				&& stringToTimeMillis(end) < now;

		return isGreaterThan;
	}

	/*
	 * retuen 2000-01 format of last month
	 */
	public static String lastMonthString() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DATE, 1);
		c.set(Calendar.MONTH, c.get(Calendar.MONTH) - 1);
		return dateToString(c.getTime(), "yyyy-MM");
	}

	/*
	 * retuen 2000-01 format of this month
	 */
	public static String thisMonthString() {
		Calendar c = Calendar.getInstance();
		return dateToString(c.getTime(), "yyyy-MM");
	}

	public static String toReadableDateTime(Date d) {
		if (TimeUtils.dateToString(d, "yyyy-MM-dd").equals(
				TimeUtils.dateToString(new Date(), "yyyy-MM-dd"))) {
			return dateToString(d, "HH:mm");
		}
		return dateToString(d, "yyyy-MM-dd HH:mm");
	}



	/**
	 * 三位月份名称转为月份数字，如Jan对应1
	 *
	 * @param shortMonthName
	 * @return
	 */
	public static int getMonthNumberByName(String shortMonthName) {
		for (int i = 0; i < MONTH_NAMES.length; i++) {
			if (shortMonthName.equalsIgnoreCase(MONTH_NAMES[i])) {
				return i + 1;
			}
		}
		return -1;
	}

	public static String normalizeApacheLogDateTime(String dateTime) {
		// SAMPLE 11/Jan/2013:00:00:00 +0800
		String time = dateTime.substring(0, dateTime.indexOf(" "));
		time = time.replace("/", " ");
		time = time.replace(":", " ");

		String[] tparts = time.split(" ");

		int monthNumber = TimeUtils.getMonthNumberByName(tparts[1]);
		String m = monthNumber < 10 ? "0" + monthNumber : "" + monthNumber;

		String normalTime = tparts[2] + "-" + m + "-" + tparts[0] + " "
				+ tparts[3] + ":" + tparts[4] + ":" + tparts[5];

		return normalTime;
	}

	/**
	 * 转换为年月日，并增加8个时区
	 *
	 * @param time
	 * @return
	 */
	public static String convertWebExTime(String time) {
		// 03/06/2013 06:46:48
		//
		time = time.replace("/", ":");
		time = time.replace(" ", ":");
		String[] parts = time.split(":");

		if (parts.length == 6) {
			String s = parts[2] + "-" + parts[0] + "-" + parts[1] + " "
					+ parts[3] + ":" + parts[4] + ":" + parts[5];
			Date d = stringToDate(s);
			long l = d.getTime() + 8 * 60 * 60 * 1000;
			return timeMillisToString(l);
		}
		return null;
	}


	public static Date addDate(Date d, long day) throws Exception {
		long time = d.getTime();
		day = day * 24 * 60 * 60 * 1000;
		time += day;
		return new Date(time);
	}

    public static String correctTimeFormat(String dateStr) {
        String result = dateStr;
        if (StringUtils.isBlank(result)) {
            return "";
        }
        if (result.length() == 15) {
            StringBuilder sb = new StringBuilder(result);
            char c = result.charAt(10);
            System.out.println(c);
            if (Character.isSpace(c)) {
                sb.insert(11, "0");
            }
            sb.append(":00");
        } else if (result.length() == 16) {
            result = result + ":00";
        }
        return result;
    }


}