/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.irods.jargon.idrop.desktop.systraygui.viscomponents;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.LoggerFactory;

/**
 * Transferrale coming from the swing tree depicting a remote iRODS file system
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public class IRODSTreeTransferable implements Transferable, ClipboardOwner {

	private List<File> files;
	private IRODSTree stagingViewTree;
	public static org.slf4j.Logger log = LoggerFactory
			.getLogger(IRODSTreeTransferable.class);
	public static DataFlavor irodsTreeDataFlavor = null;

	static {
		try {

			irodsTreeDataFlavor = new DataFlavor(
					org.irods.jargon.idrop.desktop.systraygui.viscomponents.IRODSTreeTransferable.class,
					"Local phymove");
		} catch (Exception e) {
			log.error("error creating transferrable", e);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("staging view tree transferrable:");
		if (files == null) {
			sb.append("\n  files is null!");
		}

		for (File file : files) {
			sb.append("\n   file:");
			sb.append(file.getAbsolutePath());
		}

		return sb.toString();

	}

	IRODSTreeTransferable(final List<File> transferFiles,
			final IRODSTree stagingViewTree) {

		if (transferFiles == null) {
			throw new IllegalArgumentException("null files");
		}

		if (stagingViewTree == null) {
			throw new IllegalArgumentException("null stagingViewTree");
		}
		files = transferFiles;
		this.stagingViewTree = stagingViewTree;

	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		log.debug("getting data flavors from idrop series (will be a list with one irods file for file or collection");
		return new DataFlavor[] { DataFlavor.javaFileListFlavor,
				irodsTreeDataFlavor };
	}

	@Override
	public boolean isDataFlavorSupported(final DataFlavor flavor) {
		return (flavor.equals(DataFlavor.javaFileListFlavor) || flavor
				.equals(irodsTreeDataFlavor));
	}

	@Override
	public Object getTransferData(final DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		log.info("getting files from transfer data:{}", files);
		if (!isDataFlavorSupported(flavor)) {
			throw new UnsupportedFlavorException(flavor);
		}

		return files;
	}

	@Override
	public void lostOwnership(final Clipboard clpbrd, final Transferable t) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
