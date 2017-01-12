/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package signpro.threads;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.poreid.config.POReIDConfig;
import signpro.AppletSigner;

/**
 *
 * @author prego
 */
public class ThreadStartCCVars extends Thread {

    private KeyStore ks;
    private AppletSigner as;
    
    public ThreadStartCCVars(AppletSigner as, KeyStore ks){    
        this.ks = ks;
        this.as = as;
    }
    
    
    @Override
    public void run(){
    
        try {
            
            PrivateKey pk = (PrivateKey) ks.getKey(POReIDConfig.ASSINATURA, null);
            as.setPk(pk);
            Certificate[] chain = ks.getCertificateChain(POReIDConfig.ASSINATURA);
            as.setChain(chain);
            
        } catch (KeyStoreException ex) {
            JOptionPane.showMessageDialog(as, ex.toString());
        } catch (NoSuchAlgorithmException ex) {
            JOptionPane.showMessageDialog(as, ex.toString());
        } catch (UnrecoverableKeyException ex) {
            JOptionPane.showMessageDialog(as, ex.toString());
        }
    
    }

    
}
