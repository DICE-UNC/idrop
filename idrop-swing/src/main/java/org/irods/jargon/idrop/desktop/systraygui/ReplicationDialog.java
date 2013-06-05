/*
 * ReplicationDialog.java
 *
 * Created on Jul 7, 2010, 12:13:18 PM
 */
package org.irods.jargon.idrop.desktop.systraygui;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JCheckBox;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.Resource;
import org.irods.jargon.idrop.desktop.systraygui.services.IRODSFileService;
import org.irods.jargon.idrop.exceptions.IdropException;
import org.slf4j.LoggerFactory;

/**
 * Dialog that displays the replication state of data objects, and can enqueue a
 * replication operation on a data object or a collection (recursive).
 * 
 * @author mikeconway
 */
public class ReplicationDialog extends javax.swing.JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1125925865121672751L;
	private final iDrop idropParentGui;
	private final String seriesAbsolutePath;
	private final String fileName;
	private final boolean isFile;
	private List<JCheckBox> boxes = new ArrayList<JCheckBox>();
	public static org.slf4j.Logger log = LoggerFactory
			.getLogger(ReplicationDialog.class);

	/** Creates new form ReplicationDialog for a collection */
	public ReplicationDialog(final iDrop idropParentGui, final boolean modal,
			final String seriesAbsolutePath) {
		super(idropParentGui, modal);
		initComponents();
		this.idropParentGui = idropParentGui;
		this.seriesAbsolutePath = seriesAbsolutePath;
		fileName = "";
		isFile = false;
		lblFileName.setText("Replicate contents of:" + seriesAbsolutePath);
		setUpDialog();
	}

	/** Creates new form ReplicationDialog for a file */
	public ReplicationDialog(final iDrop idropParentGui, final boolean modal,
			final String fileAbsolutePath, final String fileName) {
		super(idropParentGui, modal);
		initComponents();
		this.idropParentGui = idropParentGui;
		seriesAbsolutePath = fileAbsolutePath;
		this.fileName = fileName;
		isFile = true;
		lblFileName.setText("Replicate:" + fileName);
		setUpDialog();
	}

	private List<Resource> buildCurrentResourcesList() throws IdropException {
		// if a file, then see if it's already on the resc, otherwise add a
		// replicate
		// if this is a file, list current resources for this data object
		List<Resource> currentResources = null;
		if (isFile) {
			IRODSFileService irodsFileService;
			try {
				irodsFileService = new IRODSFileService(
						idropParentGui.getIrodsAccount(), idropParentGui
								.getiDropCore().getIrodsFileSystem());
				currentResources = irodsFileService.getResourcesForDataObject(
						seriesAbsolutePath, fileName);
			} catch (IdropException ex) {
				Logger.getLogger(ReplicationDialog.class.getName()).log(
						Level.SEVERE, null, ex);
				throw new IdropException(ex);
			}
		} else {
			currentResources = new ArrayList<Resource>();
		}
		return currentResources;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed"
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		pnlHeader = new javax.swing.JPanel();
		lblFileName = new javax.swing.JLabel();
		pnlDialogToolbar = new javax.swing.JPanel();
		btnOK = new javax.swing.JButton();
		btnCancel = new javax.swing.JButton();
		pnlOptions = new javax.swing.JPanel();
		scrollReplicationResources = new javax.swing.JScrollPane();
		pnlReplicationResources = new javax.swing.JPanel();
		pnlReplicaionTools = new javax.swing.JPanel();
		btnReplicate = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setMinimumSize(new java.awt.Dimension(400, 300));

		pnlHeader.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

		lblFileName.setText("jLabel1");
		pnlHeader.add(lblFileName);

		getContentPane().add(pnlHeader, java.awt.BorderLayout.NORTH);

		btnOK.setText("OK");
		btnOK.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				btnOKActionPerformed(evt);
			}
		});
		pnlDialogToolbar.add(btnOK);

		btnCancel.setText("Cancel");
		btnCancel.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				btnCancelActionPerformed(evt);
			}
		});
		pnlDialogToolbar.add(btnCancel);

		getContentPane().add(pnlDialogToolbar, java.awt.BorderLayout.SOUTH);

		pnlOptions.setMinimumSize(new java.awt.Dimension(300, 200));
		pnlOptions.setLayout(new java.awt.BorderLayout());

		pnlReplicationResources.setLayout(new java.awt.GridLayout(0, 1));
		scrollReplicationResources.setViewportView(pnlReplicationResources);

		pnlOptions
				.add(scrollReplicationResources, java.awt.BorderLayout.CENTER);

		pnlReplicaionTools.setLayout(new java.awt.FlowLayout(
				java.awt.FlowLayout.RIGHT));

		btnReplicate.setText("Replicate");
		btnReplicate
				.setToolTipText("Replicate the given file to selected resources");
		btnReplicate.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				btnReplicateActionPerformed(evt);
			}
		});
		pnlReplicaionTools.add(btnReplicate);

		pnlOptions.add(pnlReplicaionTools, java.awt.BorderLayout.SOUTH);

		getContentPane().add(pnlOptions, java.awt.BorderLayout.CENTER);

		pack();
	}// </editor-fold>//GEN-END:initComponents

    private void btnOKActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnOKActionPerformed
        this.dispose();
    }// GEN-LAST:event_btnOKActionPerformed

    private void btnCancelActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnCancelActionPerformed
        this.dispose();
    }// GEN-LAST:event_btnCancelActionPerformed

    private void btnReplicateActionPerformed(
            final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnReplicateActionPerformed

        List<Resource> currentResources;
        int replicatedCount = 0;
        // build a list of the current resources, if a data object, will be used
        // to decide what to replicate
        try {
            currentResources = buildCurrentResourcesList();
        } catch (IdropException ex) {
            Logger.getLogger(ReplicationDialog.class.getName()).log(
                    Level.SEVERE, null, ex);
            idropParentGui.showIdropException(ex);
            return;
        }

        for (JCheckBox checkBox : boxes) {
            if (checkBox.isSelected()) {
                log.info("getting ready to replicate:{}", checkBox.getText());

                boolean foundResource = false;
                for (Resource currentResource : currentResources) {
                    if (currentResource.getName().equals(checkBox.getText())) {
                        foundResource = true;
                        break;
                    }
                }

                /**
                 * FIXME: conveyor
                 */
                /*
                try {
                    if (isFile && !foundResource) {
                        log.info("file not yet replicated to resource");

                        StringBuilder sb = new StringBuilder();
                        sb.append(seriesAbsolutePath);
                        sb.append("/");
                        sb.append(fileName);
                        replicatedCount++;
                        // FIXME: conveyor
                        /*
                        idropParentGui.getiDropCore().getTransferManager().enqueueAReplicate(sb.toString(),
                                checkBox.getText(),
                                idropParentGui.getIrodsAccount());
                                
                    } else if (!isFile) {
                        log.info("this is a collection, do the replication");
                        replicatedCount++;
                        idropParentGui.getiDropCore().getTransferManager().enqueueAReplicate(seriesAbsolutePath,
                                checkBox.getText(),
                                idropParentGui.getIrodsAccount());

                    }
                    
                } catch (JargonException ex) {
                    Logger.getLogger(ReplicationDialog.class.getName()).log(
                            Level.SEVERE, null, ex);
                    idropParentGui.showIdropException(ex);
                    return;
                }
                */
            }
        }

        final int replicationsDone = replicatedCount;

        // now dispose
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (replicationsDone > 0) {
                    idropParentGui.showMessageFromOperation("Replication has been placed into the queue for processing");
                } else {
                    idropParentGui.showMessageFromOperation("Nothing to replicate");

                }

            }
        });

        this.dispose();

    }// GEN-LAST:event_btnReplicateActionPerformed

    private void setUpDialog() {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    IRODSFileService irodsFileService = new IRODSFileService(
                            idropParentGui.getIrodsAccount(), idropParentGui.getiDropCore().getIrodsFileSystem());
                    List<Resource> resources = irodsFileService.getResources();
                    boxes = new ArrayList<JCheckBox>();

                    // if this is a file, list current resources for this data
                    // object
                    List<Resource> currentResources = buildCurrentResourcesList();

                    for (Resource resource : resources) {
                        JCheckBox rescBox = new JCheckBox();
                        rescBox.setText(resource.getName());

                        // if this resource is already replicated, a checkbox is
                        // initialized for that resource

                        for (Resource dataObjectResource : currentResources) {
                            if (dataObjectResource.getName().equals(
                                    resource.getName())) {
                                log.debug(
                                        "resource already replicates data object:{}",
                                        resource);
                                rescBox.setSelected(true);
                                break;
                            }
                        }

                        boxes.add(rescBox);
                    }

                    for (JCheckBox checkBox : boxes) {
                        pnlReplicationResources.add(checkBox);
                    }

                    scrollReplicationResources.validate();

                } catch (IdropException ex) {
                    Logger.getLogger(ReplicationDialog.class.getName()).log(
                            Level.SEVERE, null, ex);
                    idropParentGui.showIdropException(ex);
                    return;
                }
            }
        });

    }

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton btnCancel;

	private javax.swing.JButton btnOK;

	private javax.swing.JButton btnReplicate;

	private javax.swing.JLabel lblFileName;

	private javax.swing.JPanel pnlDialogToolbar;

	private javax.swing.JPanel pnlHeader;

	private javax.swing.JPanel pnlOptions;

	private javax.swing.JPanel pnlReplicaionTools;

	private javax.swing.JPanel pnlReplicationResources;

	private javax.swing.JScrollPane scrollReplicationResources;
	// End of variables declaration//GEN-END:variables
}
