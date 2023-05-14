package org.diplom.client.crypto;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class GOST28147_89 {
    private byte[][] key;
    private int[][] table;
    private byte[] syncMessage;
    public static long GAMMA_C = 0x1010104;
    public static int FOUR_BIT_MASK = 0b1111;
    public static int SEVEN_BIT_MASK = 0b1111111;
    public static int EIGHT_BIT_MASK = 0b11111111;

    public GOST28147_89(byte[] rawKey, byte[][] rawTable, byte[] syncMessage) throws Exception {
        key = transformKey(rawKey);
        table = transformTable(rawTable);
        this.syncMessage = syncMessage;
    }

    public ArrayList<byte[]> cipher(ArrayList<byte[]> information, GOST28147_89_Mode mode) {
        switch (mode) {
            case GAMMA:
                return gammaCrypt(information, key, syncMessage);
            default:
                return information;
        }
    }

    public byte[] cipher(byte[] rawInformation, GOST28147_89_Mode mode) {
        ArrayList<byte[]> information = transformInformation(rawInformation);
        switch (mode) {
            case GAMMA:
                return backToBytes(gammaCrypt(information, key, syncMessage));
            default:
                return rawInformation;
        }
    }

    private byte[] cryptoStep(byte[] block, byte[] key) {
        long N1 = bytesToLong(new byte[]{block[0], block[1], block[2], block[3]});
        long N2 = bytesToLong(new byte[]{block[4], block[5], block[6], block[7]});

        long CM1 = (N1 + bytesToLong(key)) % (1L << 32);

        long R1 = 0;
        for (int i = 0; i < 8; i++) {
            int s = (int) ((CM1 >>> (i * 4)) & FOUR_BIT_MASK);
            R1 = R1 | ((long) table[i][s] << (4 * i));
        }

        long R2 = ((R1 << 11) | (R1 >>> 21)) % (1L << 32);

        long CM2 = R2 ^ N2;

        byte[] CM2Bytes = longToBytes(CM2);

        byte[] cryptoBlock = new byte[8];

        cryptoBlock[0] = CM2Bytes[0];
        cryptoBlock[1] = CM2Bytes[1];
        cryptoBlock[2] = CM2Bytes[2];
        cryptoBlock[3] = CM2Bytes[3];
        cryptoBlock[4] = block[0];
        cryptoBlock[5] = block[1];
        cryptoBlock[6] = block[2];
        cryptoBlock[7] = block[3];

        return cryptoBlock;
    }

    private byte[] encrypt32(byte[] informationBlock, byte[][] key) {
        for (int i = 0; i < 32; i++) {
            int keyIndex = i < 24 ? (i % 8) : (7 - i % 8);
            informationBlock = cryptoStep(informationBlock, key[keyIndex]);
        }
        informationBlock = halfRevers(informationBlock);
        return informationBlock;
    }

    private byte[] decipher32(byte[] informationBlock, byte[][] key) {
        for (int i = 0; i < 32; i++) {
            int keyIndex = i < 8 ? (i % 8) : (7 - i % 8);
            informationBlock = cryptoStep(informationBlock, key[keyIndex]);
        }
        informationBlock = halfRevers(informationBlock);
        return informationBlock;
    }

    private byte[] encrypt16(byte[] informationBlock, byte[][] key) {
        for (int i = 0; i < 16; i++) {
            int keyIndex = i % 8;
            informationBlock = cryptoStep(informationBlock, key[keyIndex]);
        }
        informationBlock = halfRevers(informationBlock);
        return informationBlock;
    }

    private ArrayList<byte[]> encryptChange(ArrayList<byte[]> data, byte[][] key) throws Exception {
        if (sizeOfInformation(data) % 8 != 0) {
            throw new Exception("Не целое число блоков по 64 бита");
        }
        ArrayList<byte[]> cryptoInformation = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {
            cryptoInformation.add(encrypt32(data.get(i), key));
        }
        return cryptoInformation;
    }

    private ArrayList<byte[]> decipherChange(ArrayList<byte[]> data, byte[][] key) throws Exception {
        if (sizeOfInformation(data) % 8 != 0) {
            throw new Exception("Не целое число блоков по 64 бита");
        }
        ArrayList<byte[]> cryptoInformation = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {
            cryptoInformation.add(decipher32(data.get(i), key));
        }
        return cryptoInformation;
    }

    private ArrayList<byte[]> gammaCrypt(ArrayList<byte[]> data, byte[][] key, byte[] syncMessage) {
        ArrayList<byte[]> gamma = buildGamma(key, syncMessage, data.size());
        ArrayList<byte[]> cryptoInformation = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {
            byte[] dataBlock = data.get(i);
            byte[] gammaBlock = gamma.get(i);
            byte[] cryptoBlock = new byte[dataBlock.length];
            for (int j = 0; j < dataBlock.length; j++) {
                cryptoBlock[j] = (byte) ((byte) dataBlock[j] ^ gammaBlock[j]);
            }
            cryptoInformation.add(cryptoBlock);
        }
        return cryptoInformation;
    }

    private ArrayList<byte[]> buildGamma(byte[][] key, byte[] syncMessage, int size) {
        ArrayList<byte[]> gamma = new ArrayList<>();

        byte[] S = encrypt32(syncMessage, key);

        for (int i = 0; i < size; i++) {
            long S0 = bytesToLong(new byte[]{S[0], S[1], S[2], S[3]});
            long S1 = bytesToLong(new byte[]{S[4], S[5], S[6], S[7]});

            S0 = (S0 + GAMMA_C) % (1L << 32);
            S1 = (S1 + GAMMA_C - 1) % ((1L << 32) - 1) + 1;

            byte[] S0b = longToBytes(S0);
            byte[] S1b = longToBytes(S1);

            S = encrypt32(new byte[]{S0b[0], S0b[1], S0b[2], S0b[3], S1b[0], S1b[1], S1b[2], S1b[3]}, key);
            gamma.add(S);
        }

        return gamma;
    }

    private ArrayList<byte[]> gammaChainCrypt(ArrayList<byte[]> data, byte[][] key, byte[] syncMessage) {
        ArrayList<byte[]> cryptoInformation = new ArrayList<>();

        byte[] S = syncMessage;

        for (int i = 0; i < data.size(); i++) {
            S = encrypt32(S, key);
            byte[] dataBlock = data.get(i);
            byte[] cryptoBlock = new byte[dataBlock.length];
            for (int j = 0; j < dataBlock.length; j++) {
                cryptoBlock[j] = (byte) ((byte) dataBlock[j] ^ S[j]);
            }
            cryptoInformation.add(cryptoBlock);
            S = cryptoBlock;
        }
        return cryptoInformation;
    }

    private ArrayList<byte[]> gammaChainDecipher(ArrayList<byte[]> data, byte[][] key, byte[] syncMessage) {
        ArrayList<byte[]> cryptoInformation = new ArrayList<>();

        byte[] S;
        for (int i = data.size() - 1; i >= 0; i--) {
            byte[] dataBlock = data.get(i);
            byte[] cryptoBlock = new byte[dataBlock.length];
            if (i != 0) {
                S = encrypt32(data.get(i - 1), key);
            } else {
                S = encrypt32(syncMessage, key);
            }
            for (int j = 0; j < dataBlock.length; j++) {
                cryptoBlock[j] = (byte) ((byte) dataBlock[j] ^ S[j]);
            }
            cryptoInformation.add(0, cryptoBlock);
        }

        return cryptoInformation;
    }

    private byte[] makeImittoInsert(ArrayList<byte[]> data, byte[][] key) {
        byte[] S = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
        for (int i = 0; i < data.size(); i++) {
            byte[] dataBlock = data.get(i);
            for (int j = 0; j < 8; j++) {
                if (j < dataBlock.length)
                    S[j] = (byte) (S[j] ^ dataBlock[j]);
            }
            S = encrypt16(S, key);
        }
        return new byte[]{S[0], S[1], S[2], S[3]};
    }

    private byte[] halfRevers(byte[] bytes) {
        byte[] halfRevers = new byte[8];
        halfRevers[0] = bytes[4];
        halfRevers[1] = bytes[5];
        halfRevers[2] = bytes[6];
        halfRevers[3] = bytes[7];
        halfRevers[4] = bytes[0];
        halfRevers[5] = bytes[1];
        halfRevers[6] = bytes[2];
        halfRevers[7] = bytes[3];
        return halfRevers;
    }

    private int bytesToInt(byte[] bytes) {
        int result = 0;
        for (int i = 0; i < 4; i++) {
            result |= bytes[i] & SEVEN_BIT_MASK;
            if (bytes[i] < 0)
                result = result | 0b10000000;
            if (i < 3)
                result = result << 8;
        }
        return result;
    }

    private long bytesToLong(byte[] bytes) {
        long result = 0L;

        for (int i = 0; i < 4; i++) {
            result |= bytes[i] & SEVEN_BIT_MASK;
            if (bytes[i] < 0)
                result = result | 0b10000000;
            if (i < 3)
                result = result << 8;
        }

        return result;
    }

    private byte[] intToBytes(int integer) {
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            bytes[3 - i] = (byte) ((integer >>> (i * 8)) & EIGHT_BIT_MASK);
        }
        return bytes;
    }

    private byte[] longToBytes(long l) {
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            bytes[3 - i] = (byte) ((l >>> (i * 8)) & EIGHT_BIT_MASK);
        }
        return bytes;
    }

    public static ArrayList<byte[]> transformInformation(byte[] rawInformation) {
        int sizeOfInformation = rawInformation.length;
        ArrayList<byte[]> information = new ArrayList<>();

        int arrListCounter = 0;
        while (true) {
            int size;
            if (sizeOfInformation - arrListCounter * 8 < 8) {
                size = sizeOfInformation - arrListCounter * 8;
            } else {
                size = 8;
            }

            byte[] bits = new byte[size];
            for (int i = 0; i < size; i++) {
                bits[i] = rawInformation[arrListCounter * 8 + i];
            }

            information.add(bits);
            arrListCounter++;

            if (arrListCounter * 8 >= sizeOfInformation)
                break;
        }

        return information;
    }

    public static byte[] backToBytes(ArrayList<byte[]> information) {
        byte[] informationInByte = new byte[sizeOfInformation(information)];
        for (int i = 0; i < information.size(); i++) {
            byte[] block = information.get(i);
            for (int j = 0; j < block.length; j++) {
                informationInByte[i * 8 + j] = block[j];
            }
        }
        return informationInByte;
    }

    public static byte[][] transformKey(byte[] rawKey) throws Exception {
        byte[][] key = new byte[8][4];

        if (rawKey.length != 32)
            throw new Exception("Wrong size of key!");

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 4; j++) {
                key[i][j] = rawKey[4 * i + j];
            }
        }

        return key;
    }

    public static int[][] transformTable(byte[][] rawTable) {
        int[][] table = new int[8][16];

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 16; j++) {
                table[i][j] = (int) rawTable[i][j];
            }
        }

        return table;
    }

    public void writeInformation(ArrayList<byte[]> information, String fileName) throws Exception {
        BufferedOutputStream bufferedInputStream = new BufferedOutputStream(new FileOutputStream(fileName));

        for (int i = 0; i < information.size(); i++) {
            bufferedInputStream.write(information.get(i));
        }
        bufferedInputStream.close();
    }

    public static int sizeOfInformation(ArrayList<byte[]> information) {
        int size;
        if (information.get(information.size() - 1).length == 8) {
            size = information.size() * 8;
        } else {
            size = (information.size() - 1) * 8 + information.get(information.size() - 1).length;
        }
        return size;
    }
}
