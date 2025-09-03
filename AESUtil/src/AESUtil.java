import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.util.Base64;
import java.util.Scanner;
public class AESUtil {
    private static final String ALGO = "AES";
    public static SecretKey generateKey() throws Exception{
        KeyGenerator KeyGen = KeyGenerator.getInstance(ALGO);
        KeyGen.init(128);
        return KeyGen.generateKey();
    }
    public static String encrypt(String data, SecretKey key) throws Exception{
        Cipher cipher = Cipher.getInstance(ALGO);
        cipher.init(Cipher.ENCRYPT_MODE,key);
        byte[] encrypted = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }
    public static String decrypt(String encryptedData, SecretKey key) throws Exception{
        Cipher cipher = Cipher.getInstance(ALGO);
        cipher.init(Cipher.DECRYPT_MODE,key);
        byte[] decoded = Base64.getDecoder().decode(encryptedData);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted);
    }
    public static void main(String[] args) throws Exception{
        SecretKey key = generateKey();
        Scanner in = new Scanner(System.in);
        System.out.print("Original Data : ");
        String OriginalData = in.next();
        String encrypted = encrypt(OriginalData,key);
        String decrypted = decrypt(encrypted,key);
        System.out.println("Original : "+OriginalData);
        System.out.println("Encrypted : "+encrypted);
        System.out.println("Decrypted : "+decrypted);
    }
}
