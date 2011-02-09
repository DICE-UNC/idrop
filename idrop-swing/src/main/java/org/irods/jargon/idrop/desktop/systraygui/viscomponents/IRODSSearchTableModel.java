package org.irods.jargon.idrop.desktop.systraygui.viscomponents;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.idrop.exceptions.IdropRuntimeException;
import org.slf4j.LoggerFactory;

/**
 * Model for a JTable that represents iRODS files and collections using the <code>CollectionAndDataObjectListingEntry</code> domain object.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public class IRODSSearchTableModel extends AbstractTableModel {

    private List<CollectionAndDataObjectListingEntry> listingEntries = new ArrayList<CollectionAndDataObjectListingEntry>();
    private static final List<String> columnNames = new ArrayList<String>();
    public static org.slf4j.Logger log = LoggerFactory.getLogger(IRODSSearchTableModel.class);

    static {
        columnNames.add("Type");
        columnNames.add("Parent");
        columnNames.add("Name");
        columnNames.add("Created At");
        columnNames.add("Modified At");
    }

    public IRODSSearchTableModel() {
        this(new ArrayList<CollectionAndDataObjectListingEntry>());
    }

    public IRODSSearchTableModel(List<CollectionAndDataObjectListingEntry> entries) {
        log.info("in default constructor with table model:{}", entries);
        this.listingEntries = entries;
    }

    // type, parent, name, created, modified
    @Override
    public Class<?> getColumnClass(int i) {
        Class clazz = null;
        switch (i) {
            case 0:
                clazz = CollectionAndDataObjectListingEntry.ObjectType.class;
                break;
            case 1:
                clazz = String.class;
                break;
            case 2:
                clazz = String.class;
                break;
            case 3:
                clazz = Date.class;
                break;
            case 4:
                clazz = Date.class;
                break;
            default:
                throw new IdropRuntimeException("unknown column, cannot determine class");
        }
        return clazz;
    }

    public List<CollectionAndDataObjectListingEntry> getEntries() {
        return listingEntries;
    }

    public void setEntries(List<CollectionAndDataObjectListingEntry> entries) {
        this.listingEntries = entries;






    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public String getColumnName(int i) {
        return columnNames.get(i);
    }

    @Override
    public int getRowCount() {
        return listingEntries.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        CollectionAndDataObjectListingEntry entry = listingEntries.get(row);
        if (entry == null) {
            throw new IdropRuntimeException("null entry for row number:" + row);
        }

        if (column < 0 || column > 5) {
            throw new IllegalArgumentException("invalid column number:" + column);
        }

        /*
         * cols
         * 0: type
         * 1: parent
         * 2: name
         * 3: created
         * 4: modified
         */
        Object returnedVal = null;

        switch (column) {
            case (0):
                returnedVal = entry.getObjectType();
                break;
            case (1):
                returnedVal = entry.getParentPath();
                break;
            case (2):
                if (entry.getObjectType() == CollectionAndDataObjectListingEntry.ObjectType.COLLECTION) {
                    try {
                        returnedVal = entry.getLastPathComponentForCollectionName();
                    } catch (JargonException ex) {
                        Logger.getLogger(IRODSSearchTableModel.class.getName()).log(Level.SEVERE, null, ex);
                        throw new IdropRuntimeException("error getting collection last path");
                    }
                } else {
                    returnedVal = entry.getPathOrName();
                }
                break;
            case (3):
                returnedVal = entry.getCreatedAt();
                break;
            case (4):
                returnedVal = entry.getModifiedAt();
                break;
        }

        return returnedVal;

    }
}