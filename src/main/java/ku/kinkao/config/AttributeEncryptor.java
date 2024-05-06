package ku.kinkao.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import jakarta.persistence.AttributeConverter;
import java.security.InvalidKeyException;
import java.security.Key;
import java.util.Base64;

@Component
public class AttributeEncryptor implements AttributeConverter<String, String> {

    @Value("${db.secret}")
    private String SECRET;
    private static final String AES = "AES";

    private Key key;
    private Cipher cipher;

    @PostConstruct
    public void init() throws Exception {
        key = new SecretKeySpec(SECRET.getBytes(), AES);
        cipher = Cipher.getInstance(AES);
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);

            return Base64
                    .getEncoder()
                    .encodeToString(cipher.doFinal(attribute.getBytes()));

        } catch (IllegalBlockSizeException | BadPaddingException
                 | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);

            return new String(
                    cipher.doFinal(
                            Base64.getDecoder().decode(dbData)));

        } catch (InvalidKeyException | BadPaddingException
                 | IllegalBlockSizeException e) {
            throw new IllegalStateException(e);
        }
    }
}
