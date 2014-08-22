package at.archistar.crypto.mac;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.params.KeyParameter;

/**
 * 
 * @author andy
 */
public class BCMacHelper implements MacHelper {
    
    private final Mac mac;
    
    /**
     * Constructor
     * 
     * @param algorithm the MAC algorithm to use (for example <i>SHA-256</i>)
     * @throws NoSuchAlgorithmException thrown if the given algorithm is not supported
     */
    public BCMacHelper(Mac mac) throws NoSuchAlgorithmException {
        this.mac = mac;
    }
    
    /**
     * Computes the MAC of the specified length for the given share with the given key.
     * 
     * @param share the share to create the MAC for
     * @param key the key to use for computing the MAC
     * @return the message authentication code (tag or MAC) for this share
     * @throws InvalidKeyException thrown if an InvalidKeyException occurred
     */
    @Override
    public byte[] computeMAC(byte[] data, byte[] key) throws InvalidKeyException {
        
        byte[] result = new byte[mac.getMacSize()];
        
        mac.init(new KeyParameter(key));
        mac.update(data, 0, data.length);
        mac.doFinal(result, 0);
        return result;
    }
    
    /**
     * Verifies the given MAC.<br>
     * (recomputes the tag from share and key and compares it with the given tag)
     * 
     * @param share the share to verify the MAC for
     * @param tag the tag to verify
     * @param key the key to use for verification
     * @return true if verification was successful (the tags matched); false otherwise
     */
    @Override
    public boolean verifyMAC(byte[] data, byte[] tag, byte[] key) {
        boolean valid = false;
        
        try {
            byte[] newTag = computeMAC(data, key); // compute tag for the given parameters
            valid = Arrays.equals(tag, newTag); // compare with original tag
        } catch (InvalidKeyException e) {}
        
        return valid;
    }
}