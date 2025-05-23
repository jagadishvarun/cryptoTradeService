package com.crypto.trade.aes;

import java.nio.charset.StandardCharsets;
import java.security.DigestException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.AttributeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;


import lombok.SneakyThrows;

@Service
public class PayloadEncryptService implements AttributeConverter<Object, String> {

	private static final Logger log = LoggerFactory.getLogger(PayloadEncryptService.class);

	private static final String ENCRYPTIONKEY = "PayIndivalKey022";
	private static final String ENCRYPTIONCIPHER = "AES";
	private Key key;
	private Cipher cipher;

	private Key getKey() {
		if (key == null)
			key = new SecretKeySpec(ENCRYPTIONKEY.getBytes(), ENCRYPTIONCIPHER);
		return key;
	}

	private Cipher getCipher() throws GeneralSecurityException {
		if (cipher == null)
			cipher = Cipher.getInstance(ENCRYPTIONCIPHER);
		return cipher;
	}

	private void initCipher(int encryptMode) throws GeneralSecurityException {
		getCipher().init(encryptMode, getKey());
	}

	@SneakyThrows
	@Override
	public String convertToDatabaseColumn(Object attribute) {
		if (attribute == null)
			return null;
		initCipher(Cipher.ENCRYPT_MODE);
		byte[] bytes = SerializationUtils.serialize(attribute);
		return Base64.getEncoder().encodeToString(getCipher().doFinal(bytes));
	}

	@SneakyThrows
	@Override
	public Object convertToEntityAttribute(String dbData) {
		if (dbData == null)
			return null;
		byte[] cipherData = Base64.getDecoder().decode(dbData);
		byte[] saltData = Arrays.copyOfRange(cipherData, 8, 16);

		MessageDigest md5 = MessageDigest.getInstance("MD5");
		final byte[][] keyAndIV = generateKeyAndIv(32, 16, 1, saltData, ENCRYPTIONKEY.getBytes(StandardCharsets.UTF_8),
				md5);
		SecretKeySpec keySpec = new SecretKeySpec(keyAndIV[0], "AES");
		IvParameterSpec iv = new IvParameterSpec(keyAndIV[1]);

		byte[] encrypted = Arrays.copyOfRange(cipherData, 16, cipherData.length);
		Cipher aesCBC = Cipher.getInstance("AES/CBC/PKCS5Padding");
		aesCBC.init(Cipher.DECRYPT_MODE, keySpec, iv);
		byte[] decryptedData = aesCBC.doFinal(encrypted);
		String decryptedText = new String(decryptedData, StandardCharsets.UTF_8);

		log.info("decryptedText {} ", decryptedText);
		return decryptedText;
	}

	/**
	 * Generates a key and an initialization vector (IV) with the given salt and
	 * password.
	 * <p>
	 * This method is equivalent to OpenSSL's EVP_BytesToKey function (see
	 * https://github.com/openssl/openssl/blob/master/crypto/evp/evp_key.c). By
	 * default, OpenSSL uses a single iteration, MD5 as the algorithm and UTF-8
	 * encoded password data.
	 * </p>
	 * 
	 * @param keyLength  the length of the generated key (in bytes)
	 * @param ivLength   the length of the generated IV (in bytes)
	 * @param iterations the number of digestion rounds
	 * @param salt       the salt data (8 bytes of data or <code>null</code>)
	 * @param password   the password data (optional)
	 * @param md         the message digest algorithm to use
	 * @return an two-element array with the generated key and IV
	 */
	public static byte[][] generateKeyAndIv(int keyLength, int ivLength, int iterations, byte[] salt, byte[] password,
			MessageDigest md) {

		int digestLength = md.getDigestLength();
		int requiredLength = (keyLength + ivLength + digestLength - 1) / digestLength * digestLength;
		byte[] generatedData = new byte[requiredLength];
		int generatedLength = 0;

		try {
			md.reset();

			// Repeat process until sufficient data has been generated
			while (generatedLength < keyLength + ivLength) {

				// Digest data (last digest if available, password data, salt if available)
				if (generatedLength > 0)
					md.update(generatedData, generatedLength - digestLength, digestLength);
				md.update(password);
				if (salt != null)
					md.update(salt, 0, 8);
				md.digest(generatedData, generatedLength, digestLength);

				// additional rounds
				for (int i = 1; i < iterations; i++) {
					md.update(generatedData, generatedLength, digestLength);
					md.digest(generatedData, generatedLength, digestLength);
				}

				generatedLength += digestLength;
			}

			// Copy key and IV into separate byte arrays
			byte[][] result = new byte[2][];
			result[0] = Arrays.copyOfRange(generatedData, 0, keyLength);
			if (ivLength > 0)
				result[1] = Arrays.copyOfRange(generatedData, keyLength, keyLength + ivLength);

			return result;

		} catch (DigestException e) {
			return new byte[0][];

		} finally {
			// Clean out temporary data
			Arrays.fill(generatedData, (byte) 0);
		}

	}

	public String getDecryptValue(Map<String, Object> req) {
		String encryReq = req.get("encryptedRequestBody").toString();
		log.info("encrypted payload: {}, ", encryReq);
		return convertToEntityAttribute(encryReq).toString();
	}
}
