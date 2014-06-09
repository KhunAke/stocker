package com.javath.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;

public class AES extends Instance {
	
	private final static byte[] byteEmpty = new byte[] {};
	
	private final Cipher cipher_encrypt;
	private final Cipher cipher_decrypt;
	
	public AES(String key) {
		this(Base64.decodeBase64(key));
	}
	public AES(byte[] key) {
		try {
			SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
			cipher_encrypt = Cipher.getInstance("AES");
			cipher_encrypt.init(Cipher.ENCRYPT_MODE, skeySpec);
			cipher_decrypt = Cipher.getInstance("AES");
			cipher_decrypt.init(Cipher.DECRYPT_MODE, skeySpec);
		} catch (NoSuchAlgorithmException e) {
			throw new ObjectException(e);
		} catch (NoSuchPaddingException e) {
			throw new ObjectException(e);
		} catch (InvalidKeyException e) {
			throw new ObjectException(e);
		}
	}
	
	public String encrypt(String message) {
		byte[] encrypted = byteEmpty;
		try {
			encrypted = cipher_encrypt
			        .doFinal(message.getBytes("UTF-8"));
		} catch (IllegalBlockSizeException e) {
			WARNING(e);
		} catch (BadPaddingException e) {
			WARNING(e);
		} catch (UnsupportedEncodingException e) {
			WARNING(e);
		}
		return Base64.encodeBase64String(encrypted);
	}
	public String decrypt(String encrypted) {
		byte[] decrypted = byteEmpty;
		try {
			decrypted = cipher_decrypt
			        .doFinal(Base64.decodeBase64(encrypted));
		} catch (IllegalBlockSizeException e) {
			WARNING(e);
		} catch (BadPaddingException e) {
			WARNING(e);
		}
		return StringUtils.newStringUtf8(decrypted);
	}
	
}
