/*
 * Created on 2007-7-5
 * Copyright (c) 2002-2007 Cobo Education & Training Co., Ltd
 * $Header$
 */
package com.util;

import org.apache.commons.codec.binary.Base64;

/**
 *
 * @version $Revision$
 * @author Chris
 *
 */
public class Base64Utils {

	public final static String ENCONDING = "UTF-8";

	public static String encode(byte[] str) {
		return new Base64(0, "".getBytes(), false).encodeToString(str);
	}

	public static byte[] decode(String str) throws Exception {
		return new Base64(0, "".getBytes(), false).decode(str);
	}

	public static String encodeString(String s) throws Exception {
		return new Base64(0, "".getBytes(), false).encodeToString(s
				.getBytes(ENCONDING));
	}

	public static String decodeString(String str) throws Exception {
		return new String(new Base64(0, "".getBytes(), false).decode(str),
				ENCONDING);
	}

	public static String encodeUrlSafe(byte[] str) {
		return new Base64(0, "".getBytes(), true).encodeToString(str);
	}

	public static byte[] decodeUrlSafe(String str) throws Exception {
		return new Base64(0, "".getBytes(), true).decode(str);
	}

	public static String encodeStringUrlSafe(String s) throws Exception {
		return new Base64(0, "".getBytes(), true).encodeToString(s
				.getBytes(ENCONDING));
	}

	public static String decodeStringUrlSafe(String str) throws Exception {
		return new String(new Base64(0, "".getBytes(), true).decode(str),
				ENCONDING);
	}

}
