/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.irods.jargon.idrop.desktop.systraygui;

import java.util.List;
import javax.swing.DefaultListModel;
import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.idrop.desktop.systraygui.viscomponents.ItemListPagingTableModel;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.irods.jargon.transfer.dao.domain.TransferItem;
import org.openide.util.Exceptions;

/**
 *
 * @author lisa
 */
public class TransferFileListDialog extends javax.swing.JDialog {
    
    private final Long transferId;
    private final IDROPCore idropCore;
    private final int itemsPerPage = 30;
    private ItemListPagingTableModel tableModel;

    /**
     * Creates new form TransferFileListDialog
     */
    public TransferFileListDialog(javax.swing.JDialog parent, Long transferId, IDROPCore idropCore) {
        super(parent, true);
        initComponents();
        
        this.transferId = transferId;
        this.idropCore = idropCore;
        
        try {
            this.tableModel = new ItemListPagingTableModel(
                    itemsPerPage,
                    transferId,
                    idropCore.getConveyorService().getQueueManagerService());
            tblItemList.setModel(tableModel);
        } catch (ConveyorExecutionException ex) {
            Exceptions.printStackTrace(ex);
            // TODO: PUT ERROR MESSAGE HERE
        }
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlMain = new javax.swing.JPanel();
        pnlList = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblItemList = new javax.swing.JTable();
        pnlButtons = new javax.swing.JPanel();
        btnNextPage = new javax.swing.JButton();
        btnPrevPage = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(1000, 400));

        pnlMain.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        pnlMain.setPreferredSize(new java.awt.Dimension(1000, 400));
        pnlMain.setLayout(new java.awt.BorderLayout());

        pnlList.setPreferredSize(new java.awt.Dimension(500, 300));
        pnlList.setLayout(new java.awt.BorderLayout());

        tblItemList.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(tblItemList);

        pnlList.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        pnlMain.add(pnlList, java.awt.BorderLayout.CENTER);

        btnNextPage.setText(org.openide.util.NbBundle.getMessage(TransferFileListDialog.class, "TransferFileListDialog.btnNextPage.text")); // NOI18N
        btnNextPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextPageActionPerformed(evt);
            }
        });
        pnlButtons.add(btnNextPage);

        btnPrevPage.setText(org.openide.util.NbBundle.getMessage(TransferFileListDialog.class, "TransferFileListDialog.btnPrevPage.text")); // NOI18N
        btnPrevPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrevPageActionPerformed(evt);
            }
        });
        pnlButtons.add(btnPrevPage);

        btnClose.setText(org.openide.util.NbBundle.getMessage(TransferFileListDialog.class, "TransferFileListDialog.btnClose.text")); // NOI18N
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });
        pnlButtons.add(btnClose);

        pnlMain.add(pnlButtons, java.awt.BorderLayout.SOUTH);

        getContentPane().add(pnlMain, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnNextPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextPageActionPerformed
        try {
            tableModel.pageDown();
        } catch (ConveyorExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_btnNextPageActionPerformed

    private void btnPrevPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevPageActionPerformed
        try {
            tableModel.pageUp();
        } catch (ConveyorExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_btnPrevPageActionPerformed

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnCloseActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnNextPage;
    private javax.swing.JButton btnPrevPage;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel pnlButtons;
    private javax.swing.JPanel pnlList;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JTable tblItemList;
    // End of variables declaration//GEN-END:variables
}