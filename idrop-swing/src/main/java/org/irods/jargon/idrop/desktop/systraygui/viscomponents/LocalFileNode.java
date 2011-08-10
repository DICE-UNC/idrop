package org.irods.jargon.idrop.desktop.systraygui.viscomponents;

import java.io.File;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.slf4j.LoggerFactory;

/**
 * (New) tree node for local file tree
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public class LocalFileNode extends DefaultMutableTreeNode {

    private boolean cached = false;

    public boolean isCached() {
        return cached;
    }
    public static org.slf4j.Logger log = LoggerFactory.getLogger(LocalFileNode.class);

    public LocalFileNode(final File file) {
        super(file);
    }

    @Override
    public boolean isLeaf() {
        File thisFile = (File) this.getUserObject();
        return thisFile.isFile();
    }

    public void lazyLoadOfChildrenOfThisNode() {

        if (cached) {
            log.debug("already cached");
            return;
        }

        log.debug("lazily loading children of:{}", this);
        File parentFile = (File) this.getUserObject();
        File[] childFiles = parentFile.listFiles();

        if (childFiles != null) {
            for (File file : childFiles) {
                this.insert(new LocalFileNode(file), this.getChildCount());
            }
        }

        cached = true;

    }

    @Override
    public void insert(final MutableTreeNode arg0, final int arg1) {
        super.insert(arg0, arg1);
    }

    public void forceReloadOfChildrenOfThisNode() {
        cached = false;
        this.removeAllChildren();
    }

    @Override
    public String toString() {
        File localFile = (File) this.getUserObject();
        String returnedString = "";

        if (localFile.getName().equals("")) {
            returnedString = "/";
        } else {
            returnedString = localFile.getName();
        }
        // log.debug("name for node is: {}", returnedString);
        return returnedString;
    }

    @Override
    public int hashCode() {
        return this.getUserObject().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {

        if (!(obj instanceof LocalFileNode)) {
            return false;
        }

        LocalFileNode comparableAsNode = (LocalFileNode) obj;

        File thisFile = (File) getUserObject();
        File thatFile = (File) comparableAsNode.getUserObject();

        return thisFile.getAbsolutePath().equals(thatFile.getAbsolutePath());
    }
}
