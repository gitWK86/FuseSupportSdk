package cn.houlang.support.encryption.aes;

/**
 * @author #Suyghur.
 * Created on 2020/7/29
 */
public class AesCommon {

    protected static final String AES_CBC_PKCS5PADDING = "AES/CBC/PKCS5Padding";

    public static final int AES_IV_LENGTH = 16;

    /**
     * reverse aesKey's first 16 bytes to generate IV, add number 1 if not enough 16 bytes
     *
     * @param aesKey
     * @return
     */
    public static byte[] generateIV(byte[] aesKey) {
        byte[] ivBytes = new byte[AES_IV_LENGTH];

        int j = 0;
        int i = aesKey.length - 1;
        while (i >= 0 && j < AES_IV_LENGTH) {
            ivBytes[j] = aesKey[i];
            i--;
            j++;
        }
        while (j < AES_IV_LENGTH) {
            ivBytes[j] = 1;
            j++;
        }
        return ivBytes;
    }
}
