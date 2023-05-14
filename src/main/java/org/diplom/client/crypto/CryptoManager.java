package org.diplom.client.crypto;

import org.diplom.client.connection.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class CryptoManager {
    @Autowired
    private SessionManager sessionManager;
    private Cipher sessionCipherEncrypt = Cipher.getInstance(ASYMETRIC_ALGORITHM);
    private Cipher sessionCipherDecrypt = Cipher.getInstance(ASYMETRIC_ALGORITHM);
    private KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ASYMETRIC_ALGORITHM);
    private KeyFactory keyFactory = KeyFactory.getInstance(ASYMETRIC_ALGORITHM);

    private static String ASYMETRIC_ALGORITHM = "RSA";

    public CryptoManager() throws NoSuchPaddingException, NoSuchAlgorithmException {

    }

    public void initSessionCiphers() {
        try {
            sessionCipherDecrypt.init(Cipher.DECRYPT_MODE,
                    createPrivateKeyFromBytes(sessionManager.getClientPrivateKey()));
            sessionCipherEncrypt.init(Cipher.ENCRYPT_MODE,
                    createPublicKeyFromBytes(sessionManager.getServerPublicKey()));
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public String sessionEncryption(byte[] info) {
        try {
            byte[] encryptedInfo = sessionCipherEncrypt.doFinal(info);
            return Base64.getEncoder().encodeToString(encryptedInfo);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] sessionDecryption(String info) {
        byte[] infoByte = Base64.getDecoder().decode(info);
        try {
            return sessionCipherDecrypt.doFinal(infoByte);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public KeyPair generateSessionKeys() {
        return keyPairGenerator.generateKeyPair();
    }

    public Key createPublicKeyFromBytes(byte[] key) {
        try {
            return keyFactory.generatePublic(new X509EncodedKeySpec(key));
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public Key createPrivateKeyFromBytes(byte[] key) {
        try {
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(key));
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
}
