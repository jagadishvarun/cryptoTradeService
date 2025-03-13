package com.crypto.trade.aes;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.AttributeConverter;

import org.springframework.context.annotation.Configuration;
import org.springframework.util.SerializationUtils;

import lombok.SneakyThrows;

@Configuration
public class EncryptionService implements AttributeConverter<Object, String> {

	private String encryptionKey = "DiamWalletKey@41";
	private static final String ENCRYPTION_CYPHER = "AES";

	private Key key;
	private Cipher cipher;

	private Key getKey() {
		if (key == null)
			key = new SecretKeySpec(encryptionKey.getBytes(), ENCRYPTION_CYPHER);
		return key;
	}

	private Cipher getCipher() throws GeneralSecurityException {
		if (cipher == null)
			cipher = Cipher.getInstance(ENCRYPTION_CYPHER);
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
		initCipher(Cipher.DECRYPT_MODE);
		byte[] bytes = getCipher().doFinal(Base64.getDecoder().decode(dbData));
		return SerializationUtils.deserialize(bytes);
	}

}
