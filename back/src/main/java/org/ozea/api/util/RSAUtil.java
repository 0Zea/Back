package org.ozea.api.util;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
public class RSAUtil {
    private static String ENCRYPT_TYPE_RSA = "RSA";

    public static String encryptRSA(String plainText, String base64PublicKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] bytePublicKey = Base64.getDecoder().decode(base64PublicKey);
        KeyFactory keyFactory = KeyFactory.getInstance(ENCRYPT_TYPE_RSA);
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(bytePublicKey));
        Cipher cipher = Cipher.getInstance(ENCRYPT_TYPE_RSA);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] bytePlain = cipher.doFinal(plainText.getBytes());
        String encrypted = Base64.getEncoder().encodeToString(bytePlain);
        return encrypted;
    }
}
