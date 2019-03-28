/*
 * Created on 2005-2-16
 * Copyright (c) 2002-2005 Cobo Education & Training Co., Ltd
 * $Header: /var/cvsroot/repository/HDMS3/src/com/cobocn/hdms/utils/NumUtils.java,v 1.7.2.1 2005-09-28 08:22:43 qgyang Exp $
 */
package com.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;


/**
 * @author qinguo
 *
 */
public class NumUtils {

	public static int parseInt(String s) {
		int i = 0;
		if (s == null || s.length() == 0)
			return 0;
		else
			try {
				i = Integer.parseInt(s);
			} catch (Exception e) {
			}
		return i;
	}

	public static long parseLong(String s) {
		long l = 0;
		if (s == null || s.length() == 0)
			return 0;
		else
			try {
				l = Long.parseLong(s);
			} catch (Exception e) {
			}
		return l;
	}

	public static float parseFloat(String s) {
		float f = 0;
		if (s == null || s.length() == 0)
			return 0;
		else
			try {
				f = Float.parseFloat(s);
			} catch (Exception e) {
			}
		return f;
	}

	public static double parseDouble(String s) {
		double f = 0;
		if (s == null || s.length() == 0)
			return 0;
		else
			try {
				f = Double.parseDouble(s);
			} catch (Exception e) {
			}
		return f;
	}

	public static String formatNumber(double number, int fraction) {
		NumberFormat nm = NumberFormat.getInstance();
		nm.setMaximumFractionDigits(fraction);
		nm.setMinimumFractionDigits(fraction);
		return nm.format(number);
	}

	public static double formatDouble(double number, int fraction) {
		BigDecimal bg = new BigDecimal(number);
		return bg.setScale(fraction, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public static String formatNumberToInt(double number) {
		return formatNumber(number, 0);
	}

	public static String formatNumber(double number) {
		NumberFormat nm = NumberFormat.getInstance();
		return nm.format(number);
	}

	public static String toString(int number, int digits) {
		boolean isNegtive = false;
		if (number < 0) {
			number = Math.abs(number);
			isNegtive = true;
		}
		if (digits <= 0)
			return number + "";
		int temp = 10;
		for (int i = 1; i < digits; i++)
			temp *= 10;
		String s = (number + temp) + "";
		s = s.substring(1);
		if (isNegtive) {
			s = "-" + s;
		}
		return s;
	}

	public static boolean isInteger(String value) {
		try {
			Integer.parseInt(value);
			return true;
	   }
		catch (NumberFormatException e) {
		   return false;
		}
    }

	public static boolean isDouble(String value) {
		try {
			Double.parseDouble(value);
			if (value.contains("."))
				return true;
			    return false;
			}
		catch (NumberFormatException e){
			    return false;
		   }
	}

	public static boolean isNumber(String value) {
		return isInteger(value) || isDouble(value);
	}

	public static String generateOrder4(int i) {
	   return ((i + 10000) + "").substring(1);
	}

    public static boolean isBigDecimalIsInteger(BigDecimal bigDecimal) {
        return (new BigDecimal(bigDecimal.intValue()).compareTo(bigDecimal)==0);
    }

    public static String convertCurrencyStr(Integer price, String currencyUnit) {
	    if (StringUtils.isBlank(currencyUnit))
	        currencyUnit = "";
        return String.format("%s%d", currencyUnit, price);
    }

    public static String convertCurrencyStr(BigDecimal price, String currencyUnit) {
        if (price == null)
            return "";
	    if (StringUtils.isBlank(currencyUnit))
	        currencyUnit = "";
        int intValue = price.intValue();
        price.setScale(2, BigDecimal.ROUND_DOWN);
        if (isBigDecimalIsInteger(price)) {
            return convertCurrencyStr(intValue, currencyUnit);
        }

        //保留两位小数
        return (new DecimalFormat(currencyUnit + "#.##").format(price));
    }
}