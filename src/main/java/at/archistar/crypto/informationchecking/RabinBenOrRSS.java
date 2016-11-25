package at.archistar.crypto.informationchecking;

import at.archistar.crypto.data.Share;
import at.archistar.crypto.mac.MacHelper;
import at.archistar.crypto.random.RandomSource;

import java.security.InvalidKeyException;
import java.util.Arrays;

/**
 * <p>This class implements the <i>Rabin-Ben-Or Robust Secret-Sharing </i> scheme.</p>
 *
 * <p>For a detailed description of the scheme,
 * see: <a href="http://www.cse.huji.ac.il/course/2003/ns/Papers/RB89.pdf">http://www.cse.huji.ac.il/course/2003/ns/Papers/RB89.pdf</a></p>
 */
public class RabinBenOrRSS implements InformationChecking {

    private final MacHelper mac;

    private final RandomSource rng;

    /** minimum amount of shares needed for reconstructing the secret */
    protected final int k;

    /**
     * Constructor
     *
     * @param mac the mac that will be used
     * @param rng the mac will need a random number source
     */
    public RabinBenOrRSS(int k, MacHelper mac, RandomSource rng) {
        this.mac = mac;
        this.rng = rng;
        this.k = k;
    }

    @Override
    public Share[] createTags(Share[] rboshares) {
        /* compute and add the corresponding tags */
        for (Share share1 : rboshares) {
            share1.setInformationChecking(Share.ICType.RABIN_BEN_OR);

            for (Share share2 : rboshares) {
                try {
                    byte[] key = new byte[this.mac.keySize()];
                    this.rng.fillBytes(key);
                    byte[] tag = this.mac.computeMAC(share1.getYValues(), key);

                    share1.getMacs().put((byte) share2.getId(), tag);
                    share2.getMacKeys().put((byte) share1.getId(), key);
                } catch (InvalidKeyException e) {
                    throw new RuntimeException("this cannot happen");
                }
            }
        }
        return rboshares;
    }

    @Override
    public Share[] checkShares(Share[] rboshares) {
        Share[] valid = new Share[rboshares.length];
        int counter = 0;

        for (Share rboshare1 : rboshares) { // go through all shares
            int accepts = 0; // number of participants accepting i
            for (Share rboshare : rboshares) {
                byte[] data = rboshare1.getYValues();
                byte[] macCmp = rboshare1.getMacs().get((byte) rboshare.getId());
                byte[] macKey = rboshare.getMacKeys().get((byte) rboshare1.getId());

                if (mac.verifyMAC(data, macCmp, macKey)) {
                    accepts++;
                }
            }

            if (accepts >= k) { // if there are at least k accepts, this share is counted as valid
                valid[counter++] = rboshare1;
            }
        }
        return Arrays.copyOfRange(valid, 0, counter);
    }

    @Override
    public String toString() {
        return "RabinBenOr(k=" + k + ", " + mac + ")";
    }
}
