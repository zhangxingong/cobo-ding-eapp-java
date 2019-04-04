/*
 * Created on 2007-1-12
 * Copyright (c) 2002-2007 Cobo Education & Training Co., Ltd
 * $Header$
 */
package com.util;

import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.SecureRandom;

/**
 *
 * @version $Revision$
 * @author Chris
 *
 */
public class CryptoUtils {

	private String _keyStr;

	private final static String TEMP_ENC_KEY = "44273821";

	public CryptoUtils(String keyStr) {
		this._keyStr = keyStr;
	}

	public static CryptoUtils getInstance(String keyStr) {
		return new CryptoUtils(keyStr);
	}

	public static byte[] encryptData(String encryptdata, String keyStr)
			throws Exception {
		SecureRandom sr = new SecureRandom();
		byte[] rawKeyData = keyStr.getBytes();

		DESKeySpec dks = new DESKeySpec(rawKeyData);

		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey key = keyFactory.generateSecret(dks);
		Cipher cipher = Cipher.getInstance("DES");
		cipher.init(Cipher.ENCRYPT_MODE, key, sr);
		byte[] data = encryptdata.getBytes();
		byte[] encryptedData = cipher.doFinal(data);

		return encryptedData;
	}

	public byte[] encryptData(String encryptdata) throws Exception {
		return CryptoUtils.encryptData(encryptdata, _keyStr);
	}

	public static String encryptBase64(String encryptdata, String keyStr)
			throws Exception {
		return Base64Utils.encode(CryptoUtils.encryptData(encryptdata, keyStr));
	}

	public String encryptBase64(String encryptdata) throws Exception {
		return CryptoUtils.encryptBase64(encryptdata, _keyStr);
	}

	public static String encryptBase64UrlSafe(String encryptdata, String keyStr)
			throws Exception {
		return Base64Utils.encodeUrlSafe(CryptoUtils.encryptData(encryptdata,
				keyStr));
	}

	public String encryptBase64UrlSafe(String encryptdata) throws Exception {
		return CryptoUtils.encryptBase64(encryptdata, _keyStr);
	}

	public static String decryptData(byte[] decryptdata, String keyStr)
			throws Exception {

		byte[] rawKeyData = keyStr.getBytes();
		DESKeySpec dks = new DESKeySpec(rawKeyData);

		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey key = keyFactory.generateSecret(dks);

		Cipher cipher = Cipher.getInstance("DES");
		cipher.init(Cipher.DECRYPT_MODE, key);

		return new String(cipher.doFinal(decryptdata));
	}

	public String decryptData(byte[] decryptdata) throws Exception {
		return CryptoUtils.decryptData(decryptdata, _keyStr);
	}

	public static String decryptBase64(String base64Str, String keyStr)
			throws Exception {
		return CryptoUtils.decryptData(Base64Utils.decode(base64Str), keyStr);
	}

	public String decryptBase64(String base64Str) throws Exception {
		return CryptoUtils.decryptData(Base64Utils.decode(base64Str), _keyStr);
	}

	public static String decryptBase64UrlSafe(String base64Str, String keyStr)
			throws Exception {
		return CryptoUtils.decryptData(Base64Utils.decodeUrlSafe(base64Str),
				keyStr);
	}

	public String decryptBase64UrlSafe(String base64Str) throws Exception {
		return CryptoUtils.decryptData(Base64Utils.decodeUrlSafe(base64Str),
				_keyStr);
	}

	public static String tempEncrypt(String oriStr) {
		try {
			return encryptBase64(oriStr, TEMP_ENC_KEY);
		} catch (Exception e) {
		}
		return "";
	}

	public static String tempDecrypt(String base64Str) {
		try {
			return decryptBase64(base64Str, TEMP_ENC_KEY);
		} catch (Exception e) {
		}
		return "";
	}

	public static String tempEncryptUrlSafe(String oriStr) {
		try {
			return encryptBase64UrlSafe(oriStr, TEMP_ENC_KEY);
		} catch (Exception e) {
		}
		return "";
	}

	public static String tempDecryptUrlSafe(String base64Str) {
		try {
			return decryptBase64UrlSafe(base64Str, TEMP_ENC_KEY);
		} catch (Exception e) {
		}
		return "";
	}

	public static String md5sum(String data) {
		return DigestUtils.md5Hex(data);
	}

	public static String sha1sum(String data) {
		return DigestUtils.shaHex(data);
	}

	public static String sha256sum(String data) {
		return DigestUtils.sha256Hex(data);
	}
}
