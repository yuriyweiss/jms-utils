package yweiss.local.jms.utils.helper;

public class DecryptorStub {

    public static String decryptIfNeeded(String text) {
        if (text.startsWith("{DES}")) {
            return decryptText(text);
        } else {
            return text;
        }
    }

    private static String decryptText(String text) {
        return text;
    }
}
