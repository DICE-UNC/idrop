package org.irods.jargon.idrop.desktop.systraygui.viscomponents;

import java.util.Date;
import java.util.List;

import javax.swing.table.DefaultTableModel;
import org.irods.jargon.idrop.desktop.systraygui.utils.IDropUtils;

import org.irods.jargon.idrop.exceptions.IdropRuntimeException;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;
import org.slf4j.LoggerFactory;

/**
 * * Model for a table viewing queue master data
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public class QueueManagerMasterTableModel extends DefaultTableModel {

    public static org.slf4j.Logger log = LoggerFactory.getLogger(QueueManagerMasterTableModel.class);

    @Override
    public Class<?> getColumnClass(final int columnIndex) {

        if (columnIndex >= getColumnCount()) {
            throw new IdropRuntimeException("column unavailable, out of bounds");
        }

        // translate indexes to object values

        // 0 = start date

        if (columnIndex == 0) {
            return Date.class;
        }

        // 1 = status

        if (columnIndex == 1) {
            return String.class;
        }

        // 2 = state

        if (columnIndex == 2) {
            return String.class;
        }

        // 3 = global error

        if (columnIndex == 3) {
            return String.class;
        }

        // source

        if (columnIndex == 4) {
            return String.class;
        }

        // 5 = target path

        if (columnIndex == 5) {
            return String.class;
        }

        throw new IdropRuntimeException("unknown column");
    }

    @Override
    public String getColumnName(final int columnIndex) {
        if (columnIndex >= getColumnCount()) {
            throw new IdropRuntimeException("column unavailable, out of bounds");
        }

        // translate indexes to object values

        // 0 = start date

        if (columnIndex == 0) {
            return "Start Date";
        }

        // 1 = status

        if (columnIndex == 1) {
            return "Status";
        }

        // 2 = state

        if (columnIndex == 2) {
            return "Error Status";
        }

        // 3 = Type

        if (columnIndex == 3) {
            return "Operation";
        }

        // 5 = local path

        if (columnIndex == 4) {
            return "Source";
        }

        // 6 = target path

        if (columnIndex == 5) {
            return "Destination";
        }

        throw new IdropRuntimeException("unknown column");
    }
    private List<LocalIRODSTransfer> localIRODSTransfers = null;

    public QueueManagerMasterTableModel(
            final List<LocalIRODSTransfer> localIRODSTransfers) {
        if (localIRODSTransfers == null) {
            throw new IdropRuntimeException("null localIRODSTransfers");
        }

        this.localIRODSTransfers = localIRODSTransfers;
    }

    @Override
    public synchronized int getRowCount() {
        if (localIRODSTransfers == null) {
            return 0;
        } else {
            return localIRODSTransfers.size();
        }
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public synchronized Object getValueAt(final int rowIndex,
            final int columnIndex) {

        if (rowIndex >= getRowCount()) {
            throw new IdropRuntimeException("row unavailable, out of bounds");
        }

        if (columnIndex >= getColumnCount()) {
            throw new IdropRuntimeException("column unavailable, out of bounds");
        }

        LocalIRODSTransfer localIRODSTransfer = localIRODSTransfers.get(rowIndex);

        // translate indexes to object values

        // 0 = start date

        if (columnIndex == 0) {
            return localIRODSTransfer.getTransferStart();
        }

        // 1 = status

        if (columnIndex == 1) {
            return localIRODSTransfer.getTransferState();
        }

        // 2 = state

        if (columnIndex == 2) {
            return localIRODSTransfer.getTransferStatus();
        }

        // 3 =operation

        if (columnIndex == 3) {
            return localIRODSTransfer.getTransferType();
        }

        // 4 = source path

        String path = null;
        if (columnIndex == 4) {
            switch (localIRODSTransfer.getTransferType()) {
                case GET:
                    path = IDropUtils.abbreviateFileName(localIRODSTransfer.getIrodsAbsolutePath());
                    break;
                case PUT:
                case REPLICATE:
                    path = IDropUtils.abbreviateFileName(localIRODSTransfer.getLocalAbsolutePath());
                    break;
                case COPY:
                    path = IDropUtils.abbreviateFileName(localIRODSTransfer.getLocalAbsolutePath());
                    break;
                case SYNCH:
                    path = IDropUtils.abbreviateFileName(localIRODSTransfer.getLocalAbsolutePath());
                    break;
                default:
                    log.error(
                            "unable to build details for transfer with transfer type of:{}",
                            localIRODSTransfer.getTransferType());
                    path = "";
                    break;
            }
            return path;
        }

        // 5 = target path
        if (columnIndex == 5) {
            switch (localIRODSTransfer.getTransferType()) {
                case GET:
                    path = IDropUtils.abbreviateFileName(localIRODSTransfer.getLocalAbsolutePath());
                    break;
                case PUT:
                    path = IDropUtils.abbreviateFileName(localIRODSTransfer.getIrodsAbsolutePath());
                    break;
                case REPLICATE:
                    path = "";
                    break;
                       case COPY:
                    path = IDropUtils.abbreviateFileName(localIRODSTransfer.getIrodsAbsolutePath());
                    break;
                case SYNCH:
                    path = IDropUtils.abbreviateFileName(localIRODSTransfer.getIrodsAbsolutePath()); // FIXME: should really be a get/put at transfer item level
                    break;
                default:
                    log.error(
                            "unable to build details for transfer with transfer type of:{}",
                            localIRODSTransfer.getTransferType());
                    path = "";
                    break;
            }
            return path;
        }

        throw new IdropRuntimeException("unknown column");

    }

    public synchronized LocalIRODSTransfer getTransferAtRow(final int rowIndex) {
        if (localIRODSTransfers == null) {
            log.warn("attempt to access a null model");
            return null;
        }

        if (rowIndex >= localIRODSTransfers.size()) {
            log.warn("attempt to access a row that does not exist");
            return null;
        }

        return localIRODSTransfers.get(rowIndex);
    }
}
