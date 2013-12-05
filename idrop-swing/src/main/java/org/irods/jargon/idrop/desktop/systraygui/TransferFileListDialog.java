/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.irods.jargon.idrop.desktop.systraygui;

import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.idrop.desktop.systraygui.viscomponents.ItemListPagingTableModel;
import org.openide.util.Exceptions;

/**
 *
 * @author lisa
 */
public class TransferFileListDialog extends javax.swing.JDialog {
    
    private final Long transferAttemptId;
    private final IDROPCore idropCore;
    private final int itemsPerPage = 30;
    private ItemListPagingTableModel tableModel;

    /**
     * Creates new form TransferFileListDialog
     */
    public TransferFileListDialog(javax.swing.JDialog parent, Long transferAttemptId, IDROPCore idropCore) {
        super(parent, true);
        initComponents();
        
        this.transferAttemptId = transferAttemptId;
        this.idropCore = idropCore;
        
        try {
            this.tableModel = new ItemListPagingTableModel(
                    itemsPerPage,
                    transferAttemptId,
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
        java.awt.GridBagConstraints gridBagConstraints;

        pnlMain = new javax.swing.JPanel();
        pnlTransferAttemptDetails = new javax.swing.JPanel();
        lblAttemptIdLabel = new javax.swing.JLabel();
        lblAttemptId = new javax.swing.JLabel();
        lblSequenceNumberLabel = new javax.swing.JLabel();
        lblSequenceNumber = new javax.swing.JLabel();
        lblTransferIdLabel = new javax.swing.JLabel();
        lblTransferId = new javax.swing.JLabel();
        lblTransferTypeLabel = new javax.swing.JLabel();
        lblTransferType = new javax.swing.JLabel();
        lblSourceLabel = new javax.swing.JLabel();
        lblSource = new javax.swing.JLabel();
        lblTargetLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        pnlList = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblItemList = new javax.swing.JTable();
        pnlButtons = new javax.swing.JPanel();
        btnPrevPage = new javax.swing.JButton();
        btnNextPage = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(1000, 800));

        pnlMain.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        pnlMain.setPreferredSize(new java.awt.Dimension(1000, 400));
        pnlMain.setLayout(new java.awt.BorderLayout());

        pnlTransferAttemptDetails.setLayout(new java.awt.GridBagLayout());

        lblAttemptIdLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblAttemptIdLabel.setText(org.openide.util.NbBundle.getMessage(TransferFileListDialog.class, "TransferFileListDialog.lblAttemptIdLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        pnlTransferAttemptDetails.add(lblAttemptIdLabel, gridBagConstraints);

        lblAttemptId.setText(org.openide.util.NbBundle.getMessage(TransferFileListDialog.class, "TransferFileListDialog.lblAttemptId.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        pnlTransferAttemptDetails.add(lblAttemptId, gridBagConstraints);

        lblSequenceNumberLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblSequenceNumberLabel.setText(org.openide.util.NbBundle.getMessage(TransferFileListDialog.class, "TransferFileListDialog.lblSequenceNumberLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        pnlTransferAttemptDetails.add(lblSequenceNumberLabel, gridBagConstraints);

        lblSequenceNumber.setText(org.openide.util.NbBundle.getMessage(TransferFileListDialog.class, "TransferFileListDialog.lblSequenceNumber.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        pnlTransferAttemptDetails.add(lblSequenceNumber, gridBagConstraints);

        lblTransferIdLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTransferIdLabel.setText(org.openide.util.NbBundle.getMessage(TransferFileListDialog.class, "TransferFileListDialog.lblTransferIdLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        pnlTransferAttemptDetails.add(lblTransferIdLabel, gridBagConstraints);

        lblTransferId.setText(org.openide.util.NbBundle.getMessage(TransferFileListDialog.class, "TransferFileListDialog.lblTransferId.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        pnlTransferAttemptDetails.add(lblTransferId, gridBagConstraints);

        lblTransferTypeLabel.setText(org.openide.util.NbBundle.getMessage(TransferFileListDialog.class, "TransferFileListDialog.lblTransferTypeLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        pnlTransferAttemptDetails.add(lblTransferTypeLabel, gridBagConstraints);

        lblTransferType.setText(org.openide.util.NbBundle.getMessage(TransferFileListDialog.class, "TransferFileListDialog.lblTransferType.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        pnlTransferAttemptDetails.add(lblTransferType, gridBagConstraints);

        lblSourceLabel.setText(org.openide.util.NbBundle.getMessage(TransferFileListDialog.class, "TransferFileListDialog.lblSourceLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 0, 0);
        pnlTransferAttemptDetails.add(lblSourceLabel, gridBagConstraints);

        lblSource.setText(org.openide.util.NbBundle.getMessage(TransferFileListDialog.class, "TransferFileListDialog.lblSource.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 0, 0);
        pnlTransferAttemptDetails.add(lblSource, gridBagConstraints);

        lblTargetLabel.setText(org.openide.util.NbBundle.getMessage(TransferFileListDialog.class, "TransferFileListDialog.lblTargetLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        pnlTransferAttemptDetails.add(lblTargetLabel, gridBagConstraints);

        jLabel1.setText(org.openide.util.NbBundle.getMessage(TransferFileListDialog.class, "TransferFileListDialog.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        pnlTransferAttemptDetails.add(jLabel1, gridBagConstraints);

        pnlMain.add(pnlTransferAttemptDetails, java.awt.BorderLayout.NORTH);

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

        btnPrevPage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/irods/jargon/idrop/desktop/systraygui/images/glyphicons_172_rewind.png"))); // NOI18N
        btnPrevPage.setMnemonic('S');
        btnPrevPage.setText(org.openide.util.NbBundle.getMessage(TransferFileListDialog.class, "TransferFileListDialog.btnPrevPage.text")); // NOI18N
        btnPrevPage.setMaximumSize(null);
        btnPrevPage.setMinimumSize(null);
        btnPrevPage.setPreferredSize(new java.awt.Dimension(40, 40));
        btnPrevPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrevPageActionPerformed(evt);
            }
        });
        pnlButtons.add(btnPrevPage);

        btnNextPage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/irods/jargon/idrop/desktop/systraygui/images/glyphicons_176_forward.png"))); // NOI18N
        btnNextPage.setMnemonic('<');
        btnNextPage.setText(org.openide.util.NbBundle.getMessage(TransferFileListDialog.class, "TransferFileListDialog.btnNextPage.text")); // NOI18N
        btnNextPage.setToolTipText(org.openide.util.NbBundle.getMessage(TransferFileListDialog.class, "TransferFileListDialog.btnNextPage.toolTipText")); // NOI18N
        btnNextPage.setMaximumSize(null);
        btnNextPage.setMinimumSize(null);
        btnNextPage.setPreferredSize(new java.awt.Dimension(40, 40));
        btnNextPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextPageActionPerformed(evt);
            }
        });
        pnlButtons.add(btnNextPage);

        btnClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/irods/jargon/idrop/desktop/systraygui/images/glyphicons_193_circle_ok.png"))); // NOI18N
        btnClose.setMnemonic('O');
        btnClose.setText(org.openide.util.NbBundle.getMessage(TransferFileListDialog.class, "TransferFileListDialog.btnClose.text")); // NOI18N
        btnClose.setToolTipText(org.openide.util.NbBundle.getMessage(TransferFileListDialog.class, "TransferFileListDialog.btnClose.toolTipText")); // NOI18N
        btnClose.setMaximumSize(null);
        btnClose.setMinimumSize(null);
        btnClose.setPreferredSize(new java.awt.Dimension(40, 40));
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
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblAttemptId;
    private javax.swing.JLabel lblAttemptIdLabel;
    private javax.swing.JLabel lblSequenceNumber;
    private javax.swing.JLabel lblSequenceNumberLabel;
    private javax.swing.JLabel lblSource;
    private javax.swing.JLabel lblSourceLabel;
    private javax.swing.JLabel lblTargetLabel;
    private javax.swing.JLabel lblTransferId;
    private javax.swing.JLabel lblTransferIdLabel;
    private javax.swing.JLabel lblTransferType;
    private javax.swing.JLabel lblTransferTypeLabel;
    private javax.swing.JPanel pnlButtons;
    private javax.swing.JPanel pnlList;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlTransferAttemptDetails;
    private javax.swing.JTable tblItemList;
    // End of variables declaration//GEN-END:variables
}
