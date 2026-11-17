package org.delcom.ifs25002springbootcashflow.domain.entity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

public class PasswordHasher {

    private static final String HASH_ALGORITHM = "SHA-256";

    public static String hash(String plainText) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] digestBytes = messageDigest.digest(plainText.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digestBytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Algoritma hash tidak didukung: " + HASH_ALGORITHM, ex);
        }
    }

    public static boolean verify(String plainText, String expectedHash) {
        return Objects.equals(hash(plainText), expectedHash);
    }
}