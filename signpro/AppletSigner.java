/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package signpro;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import org.poreid.config.POReIDConfig;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.security.CrlClient;
import com.itextpdf.text.pdf.security.CrlClientOnline;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.OcspClient;
import com.itextpdf.text.pdf.security.OcspClientBouncyCastle;
import com.itextpdf.text.pdf.security.PrivateKeySignature;
import com.itextpdf.text.pdf.security.ProviderDigest;
import com.itextpdf.text.pdf.security.TSAClient;
import com.itextpdf.text.pdf.security.TSAClientBouncyCastle;
import com.mongodb.BasicDBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import nosqlconnection.db.MyConnection;
import org.apache.commons.io.IOUtils;
import org.poreid.config.POReIDConfig;
import org.poreid.crypto.POReIDProvider;
import org.poreid.json.JSONObject;
import signpro.aux.Utils;
import signpro.threads.ThreadStartCCVars;

/**
 *
 * @author prego
 */
public class AppletSigner extends javax.swing.JApplet {

    
    private CarregarDocumentos cd;
    private List <File> lstFilesToSign;
    private int[] idsels;
    private String user;
    private byte[] rub;
    
    private boolean hasRub;
    
    
    /**
     * Initializes the applet AppletSigner
     */
    @Override
    public void init() {
        
        try {
        
            this.hasRub = false;
            lstFilesToSign = new ArrayList <> ();
            this.user = getParameter("user");
            
            /* Set the Nimbus look and feel */
            //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
            /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
            * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
            */
            try {
                for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (ClassNotFoundException ex) {
                java.util.logging.Logger.getLogger(AppletSigner.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                java.util.logging.Logger.getLogger(AppletSigner.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                java.util.logging.Logger.getLogger(AppletSigner.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (javax.swing.UnsupportedLookAndFeelException ex) {
                java.util.logging.Logger.getLogger(AppletSigner.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
            //</editor-fold>
            
            setSize(700, 500);
            
            Security.addProvider(new POReIDProvider());
            
            /* Create and display the applet */
            try {
                java.awt.EventQueue.invokeAndWait(new Runnable() {
                    public void run() {
                        initComponents();
                    }
                });
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.toString());
            }
            
            MyConnection conn = new MyConnection("dbAMEC", "collRubricas");
            conn.setDB("dbAMEC");
            GridFS myGridFS = new GridFS(conn.getDB(),"collRubricas");
            
            //verificar se tem imagem p/ rubrica
            GridFSDBFile fdb = myGridFS.findOne(new BasicDBObject("metadata.user", user));
            
            if (fdb != null) {
                
                InputStream is = fdb.getInputStream();
                rub = IOUtils.toByteArray(is);

                Image image = Toolkit.getDefaultToolkit().createImage(rub);
                ImageIcon ii = new ImageIcon(image);
                Image image2 = ii.getImage();

                Image img2 = Utils.getScaledImage(image2, 200, 75);

                ImageIcon ii2 = new ImageIcon(img2);

                jLabel2.setIcon(ii2);
                jcb1.setSelected(true);

                this.hasRub = true;

            }
            
            //KeyStore ks = KeyStore.getInstance(POReIDConfig.POREID);
            //ks.load(null);
            //ThreadStartCCVars ThreadSCCV = new ThreadStartCCVars(this, ks);
            //ThreadSCCV.start();
            
            
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.toString());
        }// catch (KeyStoreException ex) {
        //    Logger.getLogger(AppletSigner.class.getName()).log(Level.SEVERE, null, ex);
        //} catch (NoSuchAlgorithmException ex) {
        //    Logger.getLogger(AppletSigner.class.getName()).log(Level.SEVERE, null, ex);
        //} catch (CertificateException ex) {
        //    Logger.getLogger(AppletSigner.class.getName()).log(Level.SEVERE, null, ex);
        //}
        
        //</editor-fold>
        
    }

    /**
     * This method is called from within the init() method to initialize the
     * form. WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        btCancelar = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jcb1 = new javax.swing.JCheckBox();
        jcb2 = new javax.swing.JCheckBox();
        jButton2 = new javax.swing.JButton();
        btAssinar = new javax.swing.JButton();
        btAssinarSelects = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(773, 542));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("SignSCPro 2.1");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 20, -1, -1));

        jButton1.setText("Escolher documentos");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(573, 14, 191, -1));
        getContentPane().add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 49, 752, 10));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nome", "Tipo", "Ação"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);
        if (jTable1.getColumnModel().getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(0).setPreferredWidth(300);
            jTable1.getColumnModel().getColumn(1).setPreferredWidth(150);
            jTable1.getColumnModel().getColumn(2).setPreferredWidth(80);
        }

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 65, 752, 201));

        btCancelar.setText("Cancelar");
        btCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCancelarActionPerformed(evt);
            }
        });
        getContentPane().add(btCancelar, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 490, 752, 50));

        jButton3.setText("Carregar documentos no processo");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 410, 752, 70));

        jLabel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Rubrica"));
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 272, 246, 133));

        jcb1.setText("Rubrica visível");
        jcb1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcb1ActionPerformed(evt);
            }
        });
        getContentPane().add(jcb1, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 278, -1, -1));

        jcb2.setText("Rubricar todas as folhas");
        jcb2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcb2ActionPerformed(evt);
            }
        });
        getContentPane().add(jcb2, new org.netbeans.lib.awtextra.AbsoluteConstraints(396, 278, -1, -1));

        jButton2.setText("Carregar rubrica");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(597, 278, 167, -1));

        btAssinar.setText("Assinar primeiro / único");
        btAssinar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAssinarActionPerformed(evt);
            }
        });
        getContentPane().add(btAssinar, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 313, 321, 80));

        btAssinarSelects.setText("Assinar selecionados");
        btAssinarSelects.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAssinarSelectsActionPerformed(evt);
            }
        });
        getContentPane().add(btAssinarSelects, new org.netbeans.lib.awtextra.AbsoluteConstraints(597, 313, 167, 80));
    }// </editor-fold>//GEN-END:initComponents


    
    public void hideMe(){
        this.setVisible(false);
    }
    
    
    public void showMe(){
        this.setVisible(true);
        this.repaint();
    }
    
    
    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        cd = new CarregarDocumentos(jTable1);
        cd.setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void btAssinarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAssinarActionPerformed
        
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        if (cd!=null && cd.getSelectedFiles() != null && cd.getSelectedFiles().length>0) {

            InputStream is = null;
            try {
                //selecionar docs para assinar
                File[] ftos = this.cd.getSelectedFiles();
                //List <File> lstFiles = new ArrayList <> (Arrays.asList(ftos));
                File f = ftos[0];
                is = new FileInputStream(f);
                byte[] b = IOUtils.toByteArray(is);
                System.out.println(b.length);

                byte[] bs = null;
                if (jcb1.isSelected() && this.hasRub) {
                    bs = this.signPdfWithRubrica(b, f.getName());
                } else {
                    bs = this.signPdf(b, f.getName());
                }

                //FileOutputStream fos = new FileOutputStream("/home/prego/test_sign.pdf");
                //fos.write(bs);

                JOptionPane.showMessageDialog(this, "Documento assinado com sucesso!");

            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(this, ex.toString());
                this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex.toString());
                this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            } catch (DocumentException | GeneralSecurityException ex) {
                JOptionPane.showMessageDialog(this, ex.toString());
                this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            } finally {
                try {
                    is.close();
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, ex.toString());
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }

        }else{
            
            JOptionPane.showMessageDialog(this, "Primeiro tem de escolher / selecionar um documento!");
            
        }
        
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        
    }//GEN-LAST:event_btAssinarActionPerformed

    private void btCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCancelarActionPerformed
        this.destroy();
        this.stop();
        this.setVisible(false);
    }//GEN-LAST:event_btCancelarActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        
        idsels = jTable1.getSelectedRows();
        
    }//GEN-LAST:event_jTable1MouseClicked

    private void btAssinarSelectsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAssinarSelectsActionPerformed
        InputStream is = null;
        
        if(lstFilesToSign.size()>0){
        
        try {
            //selecionar docs para assinar
            File[] ftos = this.cd.getSelectedFiles();
            List <File> lstFiles = new ArrayList <> (Arrays.asList(ftos));
            this.lstFilesToSign = new ArrayList <> ();
            
            //popular com os selecionados
            for(int i : idsels){
                lstFilesToSign.add(this.cd.getSelectedFiles()[i]);
            }
            
            //assinar apenas os selecionados
            for(File f : lstFilesToSign){
                is = new FileInputStream(f);
                byte[] b = IOUtils.toByteArray(is);
                System.out.println(b.length);
            
                if (jcb1.isSelected() && this.hasRub) {
                    this.signPdfWithRubricaFinal(b, f.getName());
                }else{
                    this.signPdf(b, f.getName());
                }

                //System.out.println(bs.length);
                //FileOutputStream fos = new FileOutputStream("/home/prego/test_sign.pdf");
                //fos.write(bs);
                
            }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AppletSigner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AppletSigner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DocumentException | GeneralSecurityException ex) {
            Logger.getLogger(AppletSigner.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                Logger.getLogger(AppletSigner.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        }else{
            JOptionPane.showMessageDialog(this, "Primeiro tem de selecionar os documentos que pretende assinar.", "Sistema:", JOptionPane.INFORMATION_MESSAGE);
        }
        
    }//GEN-LAST:event_btAssinarSelectsActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        CarregarRubrica cr = new CarregarRubrica(jLabel2, user, this);
        cr.setVisible(true);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jcb1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcb1ActionPerformed
        if(!jcb1.isSelected() && jcb2.isSelected()){
            jcb2.setSelected(false);
        }
    }//GEN-LAST:event_jcb1ActionPerformed

    private void jcb2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcb2ActionPerformed
        if(!jcb1.isSelected() && jcb2.isSelected()){
            jcb1.setSelected(true);
        }
    }//GEN-LAST:event_jcb2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton3ActionPerformed


    
    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btAssinar;
    private javax.swing.JButton btAssinarSelects;
    private javax.swing.JButton btCancelar;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable jTable1;
    private javax.swing.JCheckBox jcb1;
    private javax.swing.JCheckBox jcb2;
    // End of variables declaration//GEN-END:variables

    
    
    
    
    public byte[] signPdf(byte[] src, String fileName) throws IOException, DocumentException, GeneralSecurityException {
    
        KeyStore ks = KeyStore.getInstance(POReIDConfig.POREID);
        ks.load(null);
        PrivateKey pk = (PrivateKey) ks.getKey(POReIDConfig.ASSINATURA, null);
        Certificate[] chain = ks.getCertificateChain(POReIDConfig.ASSINATURA);

        // reader and stamper
        PdfReader reader = new PdfReader(src);
        File f = new File(fileName);
        OutputStream os = new FileOutputStream(f);
        PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');

        // appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setReason("qualquer motivo");
        appearance.setLocation("qualquer localização");
        appearance.setVisibleSignature(new Rectangle(72, 732, 144, 780), 1, "primeira assinatura");

        // digital signature
        ExternalSignature es = new PrivateKeySignature(pk, "SHA-256", POReIDConfig.POREID);
        ExternalDigest digest = new ProviderDigest(null); // find provider
        MakeSignature.signDetached(appearance, digest, es, chain, null, null, null, 0, CryptoStandard.CMS);
        
        //ByteArrayOutputStream bos = (ByteArrayOutputStream) os;
        InputStream is = new FileInputStream(f);
        byte[] ba = IOUtils.toByteArray(is);
        
        return ba;
    
    }
    
    

    
    
    public byte[] signPdfWithRubrica(byte[] src, String fileName) throws IOException, DocumentException, GeneralSecurityException {
    
        if (src != null) {

            KeyStore ks = KeyStore.getInstance(POReIDConfig.POREID);
            ks.load(null);
            PrivateKey pk = (PrivateKey) ks.getKey(POReIDConfig.ASSINATURA, null);
            Certificate[] chain = ks.getCertificateChain(POReIDConfig.ASSINATURA);

            // reader and stamper
            PdfReader reader = new PdfReader(src); //<--

            int assNumber = 0;
            if(reader.getAcroFields().getSignatureNames()!=null){
                assNumber = reader.getAcroFields().getSignatureNames().size();
            }
            
            File f = new File(fileName);
            OutputStream os = new FileOutputStream(f);
            PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0'); //<----

            int totPages = reader.getNumberOfPages();
            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            Rectangle rp = reader.getPageSize(1);
            
            if (jcb2.isSelected()) {

                for (int i = 1; i <= totPages; i++) {

                    PdfContentByte cb = stamper.getOverContent(i);

                    Rectangle rect2 = new Rectangle(rp.getLeft() + 50+(assNumber*130)+20, rp.getBottom() + 88, rp.getLeft() + 180+(assNumber*130)+20, rp.getBottom() + 118);
                    //rect2.setBorder(Rectangle.BOX);
                    //rect2.setBorderWidth(1);

                    ColumnText ct = new ColumnText(cb);
                    ct.setSimpleColumn(rect2);
                    Chunk t = new Chunk("(Doc. assinado digitalmente)", new Font(Font.FontFamily.HELVETICA, 9.0f, Font.NORMAL, BaseColor.BLACK));
                    ct.addElement(new Paragraph(t));
                    ct.go();
                    cb.rectangle(rect2);
                    cb.addImage(com.itextpdf.text.Image.getInstance(rub), 130, 0, 0, 50, rp.getLeft() + 35+(assNumber*130)+20, rp.getBottom() + 111);

                }

            } else {
                
                PdfContentByte cb = stamper.getOverContent(totPages);
                Rectangle rect = new Rectangle(rp.getLeft() + 40+(assNumber*130)+20, rp.getBottom() + 100, rp.getLeft() + 170+(assNumber*130)+20, rp.getBottom() + 150);
                ColumnText ct = new ColumnText(cb);
                Rectangle rect2 = new Rectangle(rp.getLeft() + 50+(assNumber*130)+20, rp.getBottom() + 88, rp.getLeft() + 180+(assNumber*130)+20, rp.getBottom() + 118);
                ct.setSimpleColumn(rect2);
                Chunk t = new Chunk("(Assinado digitalmente)", new Font(Font.FontFamily.HELVETICA, 9.0f, Font.NORMAL, BaseColor.BLACK));
                ct.addElement(new Paragraph(t));
                ct.go(); //<--
                cb.rectangle(rect2);
                appearance.setVisibleSignature(rect, totPages, "mysignature"+assNumber);
            }
            
            appearance.setSignatureGraphic(com.itextpdf.text.Image.getInstance(rub));
            appearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC);
            
       
            // digital signature
            // timestamp
            TSAClient tsc = new TSAClientBouncyCastle("http://ts.cartaodecidadao.pt/tsa/server", "", "");

            // OCSP
            OcspClient ocsp = new OcspClientBouncyCastle();

            // long term validation (LTV)
            List<CrlClient> crlList = new ArrayList<>();
            crlList.add(new CrlClientOnline(chain));

            // digital signature
            //ExternalSignature es = new PrivateKeySignature(pk, "SHA-256", POReIDConfig.POREID);
            //ExternalDigest digest = new ProviderDigest(null);
            //MakeSignature.signDetached(appearance, digest, es, (java.security.cert.Certificate[]) chain, crlList, ocsp, tsc, 0, CryptoStandard.CMS);

            ExternalSignature es = new PrivateKeySignature(pk, "SHA-256", POReIDConfig.POREID);
            ExternalDigest digest = new ProviderDigest(null); // find provider
            MakeSignature.signDetached(appearance, digest, es, chain, null, null, null, 0, CryptoStandard.CMS);
            
            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            
            //ByteArrayOutputStream bos = (ByteArrayOutputStream) os;
            InputStream is = new FileInputStream(f);
            byte[] ba = IOUtils.toByteArray(is);

            return ba;

        }else{
            JOptionPane.showMessageDialog(this, "Primeiro tem de selecionar um documento!");
        }
        
        return null;
    
    }
    
    
    
    
    public byte[] signPdfWithRubricaFinal(byte[] src, String fileName) throws IOException, DocumentException, GeneralSecurityException {
    
        if (src != null) {

            KeyStore ks = KeyStore.getInstance(POReIDConfig.POREID);
            ks.load(null);
            PrivateKey pk = (PrivateKey) ks.getKey(POReIDConfig.ASSINATURA, null);
            Certificate[] chain = ks.getCertificateChain(POReIDConfig.ASSINATURA);

            // reader and stamper
            PdfReader reader = new PdfReader(src);

            int assNumber = 0;
            if(reader.getAcroFields().getSignatureNames()!=null){
                assNumber = reader.getAcroFields().getSignatureNames().size();
            }
            
            File f = new File(fileName);
            OutputStream os = new FileOutputStream(f);
            PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');

            int totPages = reader.getNumberOfPages();
            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            Rectangle rp = reader.getPageSize(1);
            
            
            if (jcb2.isSelected()) {

                for (int i = 1; i <= totPages; i++) {

                    PdfContentByte cb = stamper.getOverContent(i);

                    Rectangle rect2 = new Rectangle(rp.getLeft() + 50+(assNumber*130)+20, rp.getBottom() + 88, rp.getLeft() + 180+(assNumber*130)+20, rp.getBottom() + 118);
                    //rect2.setBorder(Rectangle.BOX);
                    //rect2.setBorderWidth(1);

                    ColumnText ct = new ColumnText(cb);
                    ct.setSimpleColumn(rect2);
                    Chunk t = new Chunk("(Assinado digitalmente)", new Font(Font.FontFamily.HELVETICA, 9.0f, Font.NORMAL, BaseColor.BLACK));
                    ct.addElement(new Paragraph(t));
                    ct.go();
                    cb.rectangle(rect2);
                    cb.addImage(com.itextpdf.text.Image.getInstance(rub), 130, 0, 0, 50, rp.getLeft() + 35+(assNumber*130)+20, rp.getBottom() + 111);

                }

            } else {
                
                PdfContentByte cb = stamper.getOverContent(totPages);
                Rectangle rect = new Rectangle(rp.getLeft() + 40+(assNumber*130)+20, rp.getBottom() + 100, rp.getLeft() + 170+(assNumber*130)+20, rp.getBottom() + 150);
                ColumnText ct = new ColumnText(cb);
                Rectangle rect2 = new Rectangle(rp.getLeft() + 50+(assNumber*130)+20, rp.getBottom() + 88, rp.getLeft() + 180+(assNumber*130)+20, rp.getBottom() + 118);
                ct.setSimpleColumn(rect2);
                Chunk t = new Chunk("(Assinado digitalmente)", new Font(Font.FontFamily.HELVETICA, 9.0f, Font.NORMAL, BaseColor.BLACK));
                ct.addElement(new Paragraph(t));
                ct.go();
                cb.rectangle(rect2);
                appearance.setVisibleSignature(rect, totPages, "mysignature"+assNumber);
            }
            
            appearance.setSignatureGraphic(com.itextpdf.text.Image.getInstance(rub));
            appearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC);
            

            
            // digital signature
            // timestamp
            TSAClient tsc = new TSAClientBouncyCastle("http://ts.cartaodecidadao.pt/tsa/server", "", "");

            // OCSP
            OcspClient ocsp = new OcspClientBouncyCastle();

            // long term validation (LTV)
            List<CrlClient> crlList = new ArrayList<>();
            crlList.add(new CrlClientOnline(chain));

            // digital signature
            ExternalSignature es = new PrivateKeySignature(pk, "SHA-256", POReIDConfig.POREID);
            ExternalDigest digest = new ProviderDigest(null);
            MakeSignature.signDetached(appearance, digest, es, (java.security.cert.Certificate[]) chain, crlList, ocsp, tsc, 0, CryptoStandard.CMS);

            //ByteArrayOutputStream bos = (ByteArrayOutputStream) os;
            InputStream is = new FileInputStream(f);
            byte[] ba = IOUtils.toByteArray(is);

            return ba;

        }else{
            JOptionPane.showMessageDialog(this, "Primeiro tem de selecionar um documento!");
        }
        
        return null;
    
    }
    
    
    
    
    
  
    
   

    /**
     * @return the cd
     */
    public CarregarDocumentos getCd() {
        return cd;
    }

    /**
     * @param cd the cd to set
     */
    public void setCd(CarregarDocumentos cd) {
        this.cd = cd;
    }

    /**
     * @return the idsels
     */
    public int[] getIdsels() {
        return idsels;
    }

    /**
     * @param idsels the idsels to set
     */
    public void setIdsels(int[] idsels) {
        this.idsels = idsels;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the rub
     */
    public byte[] getRub() {
        return rub;
    }

    /**
     * @param rub the rub to set
     */
    public void setRub(byte[] rub) {
        this.rub = rub;
    }

    /**
     * @return the hasRub
     */
    public boolean isHasRub() {
        return hasRub;
    }

    /**
     * @param hasRub the hasRub to set
     */
    public void setHasRub(boolean hasRub) {
        this.hasRub = hasRub;
    }

    

    
}
