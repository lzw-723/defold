package com.dynamo.cr.tileeditor.scene;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.dynamo.cr.go.core.ComponentTypeNode;
import com.dynamo.cr.properties.NotEmpty;
import com.dynamo.cr.properties.Property;
import com.dynamo.cr.properties.Resource;
import com.dynamo.cr.sceneed.core.ISceneModel;
import com.dynamo.cr.tileeditor.Activator;
import com.dynamo.cr.tileeditor.core.TileSetModel;

public class Sprite2Node extends ComponentTypeNode {

    @Property(isResource=true)
    @Resource
    @NotEmpty
    private String tileSet = "";

    @Property
    @NotEmpty
    private String defaultAnimation = "";

    private TileSetModel tileSetModel = null;

    public String getTileSet() {
        return tileSet;
    }

    public void setTileSet(String tileSet) {
        if (!this.tileSet.equals(tileSet)) {
            this.tileSet = tileSet;
            reloadTileSet();
            notifyChange();
        }
    }

    public IStatus validateTileSet() {
        if (this.tileSetModel != null) {
            IStatus status = this.tileSetModel.validate();
            boolean valid = status.isOK()
                    || (status.getSeverity() == IStatus.INFO && (!this.tileSetModel.getImage().isEmpty() || !this.tileSetModel.getCollision().isEmpty()));
            if (!valid) {
                return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.Sprite2Node_tileSet_INVALID_REFERENCE);
            }
        }
        return Status.OK_STATUS;
    }

    public String getDefaultAnimation() {
        return this.defaultAnimation;
    }

    public void setDefaultAnimation(String defaultAnimation) {
        if (!this.defaultAnimation.equals(defaultAnimation)) {
            this.defaultAnimation = defaultAnimation;
            notifyChange();
        }
    }

    public IStatus validateDefaultAnimation() {
        if (this.defaultAnimation.isEmpty()) {
            return new Status(IStatus.INFO, Activator.PLUGIN_ID, Messages.Sprite2Node_defaultAnimation_EMPTY);
        }
        return Status.OK_STATUS;
    }

    @Override
    public void setModel(ISceneModel model) {
        super.setModel(model);
        if (model != null && this.tileSetModel == null) {
            if (reloadTileSet()) {
                notifyChange();
            }
        }
    }

    @Override
    public void handleReload(IFile file) {
        IFile tileSetFile = getModel().getFile(this.tileSet);
        if (tileSetFile.exists() && tileSetFile.equals(file)) {
            if (reloadTileSet()) {
                notifyChange();
            }
        }
        if (this.tileSetModel != null) {
            if (this.tileSetModel.handleReload(file)) {
                notifyChange();
            }
        }
    }

    private boolean reloadTileSet() {
        ISceneModel model = getModel();
        if (model != null) {
            try {
                if (this.tileSet != null && !this.tileSet.isEmpty()) {
                    IFile tileSetFile = model.getFile(this.tileSet);
                    if (tileSetFile.exists()) {
                        if (this.tileSetModel == null) {
                            this.tileSetModel = new TileSetModel(model.getContentRoot(), null, null, null);
                        }
                        this.tileSetModel.load(tileSetFile.getContents());
                    }
                }
            } catch (IOException e) {
                // no reason to handle exception since having a null type is invalid state, will be caught in validateComponent below
                this.tileSetModel = null;
            } catch (CoreException e) {
                // no reason to handle exception since having a null type is invalid state, will be caught in validateComponent below
                this.tileSetModel = null;
            }
            return true;
        }
        return false;
    }

}
