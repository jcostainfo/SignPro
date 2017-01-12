/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package signpro;

import java.io.IOException;

import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.ParseException;

/**
 *
 * @author prego
 */
public class SignPro {

    /**
     * @param args the command line arguments
     * @throws IOException 
     * @throws ParseException 
     */
    public static void main(String[] args) throws ParseException, IOException {
        AppletSigner ap = new AppletSigner();
        ap.init();
        ap.start();       
    }
    
}
