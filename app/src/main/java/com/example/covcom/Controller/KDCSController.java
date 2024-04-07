package com.example.covcom.Controller;

import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
public class KDCSController {
    private static String algorithm = "AES";
    private static HashMap<String, Key> keystore = null;

    public KDCSController() {

    }

    static{
        try
        {
            keystore = new HashMap<>();
            Key ka = generateKey();
            Key kb = generateKey();

            keystore.put("Alice", ka);
            keystore.put("bob", kb);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Key generateKey() throws Exception{

        SecretKey k =  KeyGenerator.getInstance(algorithm).generateKey();
        return new SecretKeySpec(k.getEncoded(), 0, 16, algorithm);
    }

    public static Key lookup(String alias)
    {
        return (keystore!=null) ? keystore.get(alias) : null;
    }
}
