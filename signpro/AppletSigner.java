/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package signpro;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.io.IOUtils;
import org.poreid.config.POReIDConfig;
import org.poreid.crypto.POReIDProvider;

import com.infosistema.iflow.service.WebClient;
import com.infosistema.iflow.service.WorkFile;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
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
import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.ParseException;

import nosqlconnection.db.MyConnection;
import signpro.auxiliar.Utils;

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
    private PrivateKey pk;
    private Certificate[] chain;
    private String documentServiceUrl;
	private String cookie;
	
	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	private boolean hasRub;
	private String rubricServiceURL;    
    
    /**
     * Initializes the applet AppletSigner
     */
    @Override
    public void init() {
        
        try {
        
            lstFilesToSign = new ArrayList <> ();
            this.user = getParameter("user");
            this.rubricServiceURL = getCodeBase().toString() + getParameter("rubricServiceURL");//"http://localhost:8080/iFlow/SignProRubric"; 
            this.cookie = getParameter("cookie");//"JSESSIONID=407BA1D105A32E93449D38AA8C1C1344";
            
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
            
//            MyConnection conn = new MyConnection("dbAMEC", "collRubricas");
//            conn.setDB("dbAMEC");
//            GridFS myGridFS = new GridFS(conn.getDB(),"collRubricas");
//            
//            //verificar se tem imagem p/ rubrica
//            GridFSDBFile fdb = myGridFS.findOne(new BasicDBObject("metadata.user", user));
            this.setHasRub(false);
            try{
//	            InputStream is = fdb.getInputStream();
//	            rub = IOUtils.toByteArray(is);
	            rub = WebClient.downloadRubric(this.rubricServiceURL, this.cookie);
            
	            if(rub!=null && rub.length>0){
		            Image image = Toolkit.getDefaultToolkit().createImage(rub);
		            ImageIcon ii = new ImageIcon(image);
		            Image image2 = ii.getImage();
		            
		            Image img2 = Utils.getScaledImage(image2, 200, 75);
		            
		            ImageIcon ii2 = new ImageIcon(img2);
		            
		            jLabel2.setIcon(ii2);
		            jcb1.setSelected(true);
		            this.setHasRub(true);
	            }
            } catch(Exception e){
            	//JOptionPane.showMessageDialog(this, e.toString());
            }
            
            //KeyStore ks = KeyStore.getInstance(POReIDConfig.POREID);
            //ks.load(null);
            //ThreadStartCCVars ThreadSCCV = new ThreadStartCCVars(this, ks);
            //ThreadSCCV.start();
            cd = new CarregarDocumentos(jTable1);
            cd.setSelectedFiles(new WorkFile[0]);
            popoutApplet(700,500);
//			downloadFile("http://localhost:8480/iFlow/DocumentService", "JSESSIONID=7841609BB888773BA39A2606D1DCFC3B", "138", "812", "1", "886", "documento");
//			downloadFile("http://localhost:8480/iFlow/DocumentService", "JSESSIONID=7841609BB888773BA39A2606D1DCFC3B", "138", "812", "1", "887", "documento");
//			downloadFile("http://localhost:8480/iFlow/DocumentService", "JSESSIONID=7841609BB888773BA39A2606D1DCFC3B", "138", "812", "1", "888", "documento");
//			uploadFile("http://localhost:8080/iFlow/DocumentService", "JSESSIONID=FB4068C71B1A5AE4924DE48E4E2E66D6","0", (WorkFile)cd.getSelectedFiles()[0]);
//          escolherDocumentos("http://localhost:8080/iFlow/DocumentService", "JSESSIONID=39E19DFF6652D28CF66B3DE0502B1106", "138", "810", "1", "documento");
            frameSetVisible(false);
        } catch (Exception ex) {
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

	private JFrame frame;
    private JPanel mainPanel;    
    
    private void popoutApplet(int largura, int altura){
        if (frame == null){
    //first time popped - create the frame
            frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            frame.setSize(largura, altura);
            //frame.setIconImage(ImageLoader.getLogoImage());
            //frame.addWindowListener(new FrameCloser());           
    //createPopMessage();
    //createPopMessage() just creates a new JPanel
    //that says "the applet is being displayed in a separate frame"
        }
        super.getContentPane().remove(mainPanel);
        //getContentPane().add("popMessage", BorderLayout.CENTER);
        frame.getContentPane().add(mainPanel);
        frame.pack();
        //this is the right way to do this,
        //frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        //here is a way to do it but it doesn't account
        //for the Windows toolbar
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        //frame.setSize(screenSize.width, screenSize.height);
        frame.setLocation(0,0);
        frame.setVisible(true);
        //frame.setAlwaysOnTop(true);
        //isPopped = true;
        invalidate();
        validate();
        repaint();
    }
	
    public void frameSetVisible(Boolean show){
    	frame.setVisible(show);
    	frame.toFront();
    	repaint();
    }    
    
    public void frameMaximize(){
    	frame.setState(Frame.NORMAL);
    	frame.setVisible(true);
    	repaint();
    } 
    
    protected void executeScript(String script) {	    
    	com.infosistema.iflow.service.JSObject.getWindow(this).eval(script);	    
    }
    
    /**
     * This method is called from within the init() method to initialize the
     * form. WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
    	//frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		mainPanel = new JPanel();
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
        mainPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("SignSCPro 2.1");
        mainPanel.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 20, -1, -1));

        jButton1.setText("Escolher documentos");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        //disable Escolher documentos dutton
        //mainPanel.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(573, 14, 191, -1));
        mainPanel.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 49, 752, 10));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nome", "Tipo", "Acção"
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

        mainPanel.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 65, 752, 201));

        btCancelar.setText("Cancelar");
        btCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCancelarActionPerformed(evt);
            }
        });
        mainPanel.add(btCancelar, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 490, 752, 50));

        jButton3.setText("Carregar documentos no processo");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        mainPanel.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 410, 752, 70));

        jLabel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Rubrica"));
        mainPanel.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 272, 246, 133));

        jcb1.setText("Rubrica visível");
        jcb1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcb1ActionPerformed(evt);
            }
        });
        mainPanel.add(jcb1, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 278, -1, -1));

        jcb2.setText("Rubricar todas as folhas");
        jcb2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcb2ActionPerformed(evt);
            }
        });
        mainPanel.add(jcb2, new org.netbeans.lib.awtextra.AbsoluteConstraints(396, 278, -1, -1));

        jButton2.setText("Carregar rubrica");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        mainPanel.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(597, 278, 167, -1));

        btAssinar.setText("Assinar primeiro / Único");
        btAssinar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAssinarActionPerformed(evt);
            }
        });
        mainPanel.add(btAssinar, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 313, 321, 80));

        btAssinarSelects.setText("Assinar selecionados");
        btAssinarSelects.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAssinarSelectsActionPerformed(evt);
            }
        });
        mainPanel.add(btAssinarSelects, new org.netbeans.lib.awtextra.AbsoluteConstraints(597, 313, 167, 80));
		super.getContentPane().add(mainPanel);
    }// </editor-fold>//GEN-END:initComponents

    public Container getContentPane(){
    	return this.frame.getContentPane();
    }
    
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
        
    	this.getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
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
                    bs = this.signPdfWithRubrica(b, f.getAbsolutePath());
                } else {
                    bs = this.signPdf(b, f.getAbsolutePath());
                }
                WorkFile wf = (WorkFile) WorkFile.createClientSideWorkFile(((WorkFile)f).getFid(), ((WorkFile)f).getPid(), ((WorkFile)f).getSubpid(), ((WorkFile)f).getDocid(), ((WorkFile)f).getVariable(), new ByteArrayInputStream(bs), ((WorkFile)f).getFilename());
                this.executeScript("changeFileState(" +wf.getDocid()+ ")");
                uploadFile(documentServiceUrl, cookie, "1", wf);

//                FileOutputStream fos = new FileOutputStream("/home/prego/test_sign.pdf");
//                fos.write(bs);

                JOptionPane.showMessageDialog(this, "Documento assinado com sucesso!");                 
				frameSetVisible(false);
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(this, ex.toString());
                this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex.toString());
                this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            } catch (DocumentException | GeneralSecurityException ex) {
                JOptionPane.showMessageDialog(this, ex.toString());
                this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            } catch (PrivilegedActionException e) {
            	JOptionPane.showMessageDialog(this, e.toString());
                this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			} catch (Exception e) {
            	JOptionPane.showMessageDialog(this, e.toString() + "Não foi possível assinar o documento");
                this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			} finally {
				//set files to show
                WorkFile[] updatedFileList = new WorkFile[cd.getSelectedFiles().length-1];
                for(int i=1; i<cd.getSelectedFiles().length; i++)
                	updatedFileList[i-1] = (WorkFile) cd.getSelectedFiles()[i];
                cd.initializeFileList(updatedFileList);
                try {
                    is.close();
                    this.getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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
//        this.destroy();
//        this.stop();
//        this.setVisible(false);     
    	cd.initializeFileList(new WorkFile[0]);
    	frameSetVisible(false);
    	
    }//GEN-LAST:event_btCancelarActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        
        idsels = jTable1.getSelectedRows();
        
    }//GEN-LAST:event_jTable1MouseClicked

    private void btAssinarSelectsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAssinarSelectsActionPerformed
        InputStream is = null;
        try {
        	//selecionar docs para assinar
            File[] ftos = this.cd.getSelectedFiles();
            List <File> lstFiles = new ArrayList <> (Arrays.asList(ftos));
            this.lstFilesToSign = new ArrayList <> ();
            
            //popular com os selecionados
            if(idsels==null){
            	JOptionPane.showMessageDialog(this, "Não foram selecionados documentos!");
            	return;
            }
            
            this.getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));            
            
            for(int i : idsels){
                lstFilesToSign.add(this.cd.getSelectedFiles()[i]);
            }
            
            if(lstFilesToSign.size()==0)
            	JOptionPane.showMessageDialog(this, "Não foram selecionados documentos!");
            
            //assinar apenas os selecionados
            for(File f : lstFilesToSign){
                is = new FileInputStream(f);
                byte[] b = IOUtils.toByteArray(is);
                System.out.println(b.length);
                byte[] bs;
                if (jcb1.isSelected() && this.hasRub)
                	bs = this.signPdfWithRubrica(b, f.getAbsolutePath());
                else 
                	bs = this.signPdf(b, f.getAbsolutePath());
                
                System.out.println(bs.length);
                WorkFile wf = (WorkFile) WorkFile.createClientSideWorkFile(((WorkFile)f).getFid(), ((WorkFile)f).getPid(), ((WorkFile)f).getSubpid(), ((WorkFile)f).getDocid(), ((WorkFile)f).getVariable(), new ByteArrayInputStream(bs), ((WorkFile)f).getFilename());
                this.executeScript("changeFileState(" +wf.getDocid()+ ")");
                uploadFile(documentServiceUrl, cookie, "1", wf);
            }
            
            //FileOutputStream fos = new FileOutputStream("/home/prego/test_sign.pdf");
            //fos.write(bs);
            JOptionPane.showMessageDialog(this, "Documentos assinados com sucesso!");                        
            frameSetVisible(false);
        } catch (FileNotFoundException ex) {
        	JOptionPane.showMessageDialog(this, ex.toString());
            Logger.getLogger(AppletSigner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
        	JOptionPane.showMessageDialog(this, ex.toString() );
            Logger.getLogger(AppletSigner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DocumentException | GeneralSecurityException ex) {
        	JOptionPane.showMessageDialog(this, ex.toString());
            Logger.getLogger(AppletSigner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (PrivilegedActionException e) {
        	JOptionPane.showMessageDialog(this, e.toString());
        	Logger.getLogger(AppletSigner.class.getName()).log(Level.SEVERE, null, e);
		} catch(Exception e){   
			JOptionPane.showMessageDialog(this, e.toString() + "Não foi possível assinar o documento");
		} finally {
			//update list to show
            ArrayList<WorkFile> updatedFileList = new ArrayList<WorkFile>();
            for(int i=0; i<cd.getSelectedFiles().length; i++){
            	boolean keep=true;
            	for(int j=0; j<idsels.length; j++)
            		if(i==idsels[j])
            			keep=false;
            	if(keep)
            		updatedFileList.add((WorkFile)cd.getSelectedFiles()[i]);
            }            
            cd.initializeFileList(updatedFileList.toArray(new WorkFile[0]));
			this.getContentPane().setCursor(Cursor.getDefaultCursor());
            try {
                is.close();
            } catch (IOException ex) {
                Logger.getLogger(AppletSigner.class.getName()).log(Level.SEVERE, null, ex);
            }
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

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {
    	try {
    		for(File f:this.cd.getSelectedFiles()){
    			String docid =uploadFile(documentServiceUrl, cookie, "0", (WorkFile)f);
//    			String status = "{\"status\":\"complete\",\"result\":{\"name\":\"" + ((WorkFile)f).getFilename() + "\",\"id\":\"" +docid+ "\",\"varname\":\"" +((WorkFile)f).getVariable()+ "\"}}";
//    			this.executeScript("setTimeout('updateForm(\\'"+status+"\\')', 100);");
    		}
    		JOptionPane.showMessageDialog(this, "Documentos carregados com sucesso!");
            cd.initializeFileList(new WorkFile[0]); 
            frameSetVisible(false);            
		} catch (IOException | PrivilegedActionException e) {
			Logger.getLogger(AppletSigner.class.getName()).log(Level.SEVERE, null, e);
			JOptionPane.showMessageDialog(this, e.toString());			
		}    	
    }

    public void escolherDocumentos(String documentServiceUrl, String cookie, String fid, String pid, String subpid, String variable) throws PrivilegedActionException{
    	this.documentServiceUrl=documentServiceUrl;
    	this.cookie=cookie;
    	this.frameSetVisible(true);
    	AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
	        public String run() throws ParseException, IOException {
	        	cd = new CarregarDocumentos(jTable1, fid, pid, subpid, variable);
	            cd.setVisible(true);
	            return "OK";
	        }
	      });
    	        
    }
        
    public void downloadFile(String documentServiceUrl, String cookie, String fid, String pid,
			String subpid, String docid, String variable) throws ParseException, IOException, PrivilegedActionException{
    	this.documentServiceUrl=documentServiceUrl;
    	this.cookie=cookie;
    	this.frameSetVisible(true);    	
    	Logger.getLogger(AppletSigner.class.getName()).log(Level.FINE, "AppletSigner.downloadFile START", documentServiceUrl+" "+ cookie+" "+ fid+" "+ pid+" "+ subpid+" "+ docid+" "+ variable);
    	String result = AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
    	        public String run() throws ParseException, IOException {
    	        	File[] selectedFiles = {WebClient.downloadFile(documentServiceUrl, cookie, fid, pid, subpid, docid, variable)};
                	cd.addToFileList(selectedFiles); 
                	Logger.getLogger(AppletSigner.class.getName()).log(Level.FINE, "AppletSigner.downloadFile END", docid);
                	return docid;
    	        }
    	      });    
//    	if(result!=null)
//    		JOptionPane.showMessageDialog(frame, "Documento carregado com sucesso");
    }     
    
    public void downloadFile(String documentServiceUrl, String cookie, String fid, String pid,
			String subpid, String[] docids, String variable) throws ParseException, IOException, PrivilegedActionException{
    	this.documentServiceUrl=documentServiceUrl;
    	this.cookie=cookie;
    	String result = AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
			public String run() throws ParseException, IOException {
				File[] selectedFiles = new File[docids.length];
				for (int i = 0; i < docids.length; i++)
					selectedFiles[i] = WebClient.downloadFile(documentServiceUrl, cookie, fid, pid, subpid, docids[i],
							variable);
				cd.addToFileList(selectedFiles);
				return docids.toString();
			}
		}); 
    }
    
    private String uploadFile(String documentServiceUrl, String cookie, String numass, WorkFile f) throws FileNotFoundException, IOException, PrivilegedActionException{
    	String result = AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
			public String run() throws IOException {
				return WebClient.uploadFile(documentServiceUrl, cookie, f, numass);				
			}
		});    	
    	if (result ==null)
    		throw new IOException("Upload error");
    	else{
    		String status = "{\"status\":\"complete\",\"result\":{\"name\":\"" + ((WorkFile)f).getFilename() + "\",\"id\":\"" +result+ "\",\"varname\":\"" +((WorkFile)f).getVariable()+ "\"}}";
			this.executeScript("setTimeout('updateForm(\\'"+status+"\\')', 100);");
    	}
    		
    	return result;
    }
    
    
    
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
        appearance.setLocation("qualquer localizaÃ§Ã£o");
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
                
//                PdfContentByte cb = stamper.getOverContent(totPages);
//                Rectangle rect = new Rectangle(rp.getLeft() + 40+(assNumber*130)+20, rp.getBottom() + 100, rp.getLeft() + 170+(assNumber*130)+20, rp.getBottom() + 150);
//                ColumnText ct = new ColumnText(cb);
//                Rectangle rect2 = new Rectangle(rp.getLeft() + 50+(assNumber*130)+20, rp.getBottom() + 88, rp.getLeft() + 180+(assNumber*130)+20, rp.getBottom() + 118);
//                ct.setSimpleColumn(rect2);
//                Chunk t = new Chunk("(Assinado digitalmente)", new Font(Font.FontFamily.HELVETICA, 9.0f, Font.NORMAL, BaseColor.BLACK));
//                ct.addElement(new Paragraph(t));
//                ct.go(); //<--
//                cb.rectangle(rect2);
//                appearance.setVisibleSignature(rect, totPages, "mysignature"+assNumber);
            	PdfContentByte cb = stamper.getOverContent(totPages);

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
     * @return the pk
     */
    public PrivateKey getPk() {
        return pk;
    }

    /**
     * @param pk the pk to set
     */
    public void setPk(PrivateKey pk) {
        this.pk = pk;
    }

    /**
     * @return the chain
     */
    public Certificate[] getChain() {
        return chain;
    }


    /**
     * @param chain the chain to set
     */
    public void setChain(Certificate[] chain) {
        this.chain = chain;
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

	public String getRubricServiceURL() {
		return rubricServiceURL;
	}

	public void setRubricServiceURL(String rubricServiceURL) {
		this.rubricServiceURL = rubricServiceURL;
	}
    
}
