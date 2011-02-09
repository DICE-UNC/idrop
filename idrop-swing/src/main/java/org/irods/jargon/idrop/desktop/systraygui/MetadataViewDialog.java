/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MetadataViewDialog.java
 *
 * Created on Jul 28, 2010, 7:24:27 AM
 */
package org.irods.jargon.idrop.desktop.systraygui;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.idrop.desktop.systraygui.services.IRODSFileService;
import org.irods.jargon.idrop.desktop.systraygui.viscomponents.MetadataTableModel;
import org.irods.jargon.idrop.exceptions.IdropException;

/**
 * @author mikeconway
 */
public class MetadataViewDialog extends javax.swing.JDialog {

    private final iDrop idropGui;
    private final IRODSAccount irodsAccount;
    private final String irodsAbsolutePath;
    private final String fileName;
    private final boolean collection;

    /** Creates new form MetadataViewDialog */
    public MetadataViewDialog(final iDrop idropGui, final IRODSAccount irodsAccount, final String absolutePath) {
        super(idropGui, true);
        this.idropGui = idropGui;
        this.irodsAccount = irodsAccount;
        this.irodsAbsolutePath = absolutePath;
        this.collection = true;
        this.fileName = "";
        initComponents();
        initializeMetadata();
    }

    /** Creates new form MetadataViewDialog */
    public MetadataViewDialog(final iDrop idropGui, final IRODSAccount irodsAccount, final String absolutePath, final String fileName) {
        super(idropGui, true);
        this.idropGui = idropGui;
        this.irodsAccount = irodsAccount;
        this.irodsAbsolutePath = absolutePath;
        this.collection = false;
        this.fileName = fileName;
        initComponents();
        initializeMetadata();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlTop = new javax.swing.JPanel();
        lblAbsolutePath = new javax.swing.JLabel();
        pnlCenter = new javax.swing.JPanel();
        scrollMetadata = new javax.swing.JScrollPane();
        tableMetadata = new javax.swing.JTable();
        pnlBottom = new javax.swing.JPanel();
        btnCancel = new javax.swing.JButton();
        btnOK = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Metadata View");

        lblAbsolutePath.setText("test");

        org.jdesktop.layout.GroupLayout pnlTopLayout = new org.jdesktop.layout.GroupLayout(pnlTop);
        pnlTop.setLayout(pnlTopLayout);
        pnlTopLayout.setHorizontalGroup(
            pnlTopLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlTopLayout.createSequentialGroup()
                .addContainerGap()
                .add(lblAbsolutePath, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 596, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlTopLayout.setVerticalGroup(
            pnlTopLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlTopLayout.createSequentialGroup()
                .add(10, 10, 10)
                .add(lblAbsolutePath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 33, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(pnlTop, java.awt.BorderLayout.NORTH);

        pnlCenter.setLayout(new java.awt.GridLayout());

        scrollMetadata.setViewportView(null);

        tableMetadata.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tableMetadata.setMaximumSize(new java.awt.Dimension(2147483647, 32000));
        tableMetadata.setPreferredSize(new java.awt.Dimension(700, 64));
        tableMetadata.setSize(new java.awt.Dimension(700, 200));
        scrollMetadata.setViewportView(tableMetadata);

        pnlCenter.add(scrollMetadata);

        getContentPane().add(pnlCenter, java.awt.BorderLayout.CENTER);

        pnlBottom.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });
        pnlBottom.add(btnCancel);

        btnOK.setText("OK");
        btnOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOKActionPerformed(evt);
            }
        });
        pnlBottom.add(btnOK);

        getContentPane().add(pnlBottom, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOKActionPerformed

        this.dispose();
    }//GEN-LAST:event_btnOKActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        this.dispose();
}//GEN-LAST:event_btnCancelActionPerformed

    private void initializeMetadata() {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                try {
                    IRODSFileService irodsFileService = new IRODSFileService(irodsAccount, idropGui.getIrodsFileSystem());
                    MetadataTableModel metadataTableModel;

                    if (collection) {
                        lblAbsolutePath.setText(irodsAbsolutePath);
                        metadataTableModel = new MetadataTableModel(irodsFileService.getMetadataForCollection(irodsAbsolutePath));
                    } else {
                        lblAbsolutePath.setText(irodsAbsolutePath + "/" + fileName);
                        metadataTableModel = new MetadataTableModel(irodsFileService.getMetadataForDataObject(irodsAbsolutePath, fileName));
                    }
                    tableMetadata.setModel(metadataTableModel);
                    tableMetadata.validate();
                }
                catch (IdropException ex) {
                    Logger.getLogger(MetadataViewDialog.class.getName()).log(Level.SEVERE, null, ex);
                    idropGui.showIdropException(ex);
                }
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOK;
    private javax.swing.JLabel lblAbsolutePath;
    private javax.swing.JPanel pnlBottom;
    private javax.swing.JPanel pnlCenter;
    private javax.swing.JPanel pnlTop;
    private javax.swing.JScrollPane scrollMetadata;
    private javax.swing.JTable tableMetadata;
    // End of variables declaration//GEN-END:variables
}