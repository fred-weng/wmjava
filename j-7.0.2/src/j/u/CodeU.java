//<editor-fold>
package j.u;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public abstract class CodeU {

    public static class Des {

        private static final String ALGORITHM = "TripleDES";
        private static final String CHARNAME = "UTF-8";

        private static byte[] encrypt(byte[] key, String source) throws Exception {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, ALGORITHM));
            return cipher.doFinal(source.getBytes());
        }

        private static byte[] decrypt(byte[] key, byte[] source) throws Exception {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, ALGORITHM));
            return cipher.doFinal(source);
        }

        public static String encode(String key, String source) throws IOException {
            try {
                return B64.encode(encrypt(padding(key).getBytes(CHARNAME), source));
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        public static String decode(String key, String cryptograph_base64) throws IOException {
            try {
                return new String(decrypt(padding(key).getBytes(CHARNAME), B64.decode(cryptograph_base64)));
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        private static final String P = StrU.makeSpace(24);

        private static String padding(String s) {
            if (s == null || s.equals(""))
                return P;
            return s.length() < 24 ? s.concat(P).substring(0, 24) : s.substring(0, 24);
        }
    }

    public static class B64 {

        public static String encode(byte[] d) {

            int n = d.length;
            char[] r = new char[((n + 2) / 3) * 4];

            for (int i = 0, j = 0; i < n; i += 3, j += 4) {
                boolean q = false, t = false;
                int v = (0xFF & (int) d[i]);
                v <<= 8;
                if ((i + 1) < n) {
                    v |= (0xFF & (int) d[i + 1]);
                    t = true;
                }
                v <<= 8;
                if ((i + 2) < n) {
                    v |= (0xFF & (int) d[i + 2]);
                    q = true;
                }
                r[j + 3] = CHARS[(q ? (v & 0x3F) : 64)];
                v >>= 6;
                r[j + 2] = CHARS[(t ? (v & 0x3F) : 64)];
                v >>= 6;
                r[j + 1] = CHARS[v & 0x3F];
                v >>= 6;
                r[j + 0] = CHARS[v & 0x3F];
            }
            return new String(r);
        }

        public static byte[] decode(String s) {
            char[] d = s.toCharArray();
            int n = d.length;
            int len = ((n + 3) / 4) * 3;
            if (n > 0 && d[n - 1] == '=')
                --len;
            if (n > 1 && d[n - 2] == '=')
                --len;

            byte[] r = new byte[len];
            int m = 0, f = 0, j = 0;
            for (int i = 0; i < n; i++) {
                int v = CODES[d[i] & 0xFF];
                if (v >= 0) {
                    m <<= 6;
                    f += 6;
                    m |= v;
                    if (f >= 8) {
                        f -= 8;
                        r[j++] = (byte) ((m >> f) & 0xff);
                    }
                }
            }
            if (j != len) {
                System.err.print("Base64 decode:miscalculated data length!");
                return null;
            }
            return r;
        }

        //<editor-fold defaultstate="collapsed"  desc="private data field">
        private static final char[] CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".toCharArray();
        private static final byte[] CODES = new byte[256];

        static {
            for (int i = 0; i < 256; i++)
                CODES[i] = -1;
            for (int i = 'A'; i <= 'Z'; i++)
                CODES[i] = (byte) (i - 'A');
            for (int i = 'a'; i <= 'z'; i++)
                CODES[i] = (byte) (26 + i - 'a');
            for (int i = '0'; i <= '9'; i++)
                CODES[i] = (byte) (52 + i - '0');
            CODES['+'] = 62;
            CODES['/'] = 63;
        }
        //</editor-fold>

    }

    public abstract String decode(String source);

    public static CodeU MD5 = new MD("MD5");
    public static CodeU MD2 = new MD("MD2");
    public static CodeU SHA_1 = new MD("SHA-1");
    public static CodeU SHA_256 = new MD("SHA-256");
    public static CodeU SHA_384 = new MD("SHA-384");
    public static CodeU SHA_512 = new MD("SHA-512");

    private static class MD extends CodeU {

        private MessageDigest md = null;

        private MD(String algorithm) {
            try {
                md = MessageDigest.getInstance(algorithm);
            } catch (NoSuchAlgorithmException e) {
                LogU.error(e.getMessage());
            }
        }

        @Override
        public String decode(String source) {
            return B64.encode(md.digest(source.getBytes()));
        }
    }

}
//</editor-fold>
