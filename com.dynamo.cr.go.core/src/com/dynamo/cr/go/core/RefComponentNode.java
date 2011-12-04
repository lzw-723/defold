package com.dynamo.cr.go.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;

import com.dynamo.cr.properties.NotEmpty;
import com.dynamo.cr.properties.Property;
import com.dynamo.cr.properties.Resource;
import com.dynamo.cr.sceneed.core.ISceneModel;

public class RefComponentNode extends ComponentNode {

    @Property(isResource=true)
    @Resource
    @NotEmpty
    private String component;
    private ComponentTypeNode type;

    public RefComponentNode(ComponentTypeNode type) {
        super();
        this.type = type;
        if (this.type != null) {
            this.type.setModel(this.getModel());
        }
    }

    public String getComponent() {
        return this.component;
    }

    public void setComponent(String component) {
        if (this.component != null ? !this.component.equals(component) : component != null) {
            this.component = component;
            reloadType();
            notifyChange();
        }
    }

    public ComponentTypeNode getType() {
        return this.type;
    }

    @Override
    public void setModel(ISceneModel model) {
        super.setModel(model);
        if (this.type != null) {
            this.type.setModel(model);
        }
    }

    public IStatus validateComponent() {
        if (this.component != null && !this.component.isEmpty()) {
            if (this.type != null) {
                IStatus status = this.type.validate();
                if (!status.isOK()) {
                    return new Status(IStatus.ERROR, Constants.PLUGIN_ID, Messages.RefComponentNode_component_INVALID_REFERENCE);
                }
            } else {
                int index = this.component.lastIndexOf('.');
                String message = null;
                if (index < 0) {
                    message = NLS.bind(Messages.RefComponentNode_component_UNKNOWN_TYPE, this.component);
                } else {
                    String ext = this.component.substring(index+1);
                    message = NLS.bind(Messages.RefComponentNode_component_INVALID_TYPE, ext);
                }
                return new Status(IStatus.ERROR, Constants.PLUGIN_ID, message);
            }
        }
        return Status.OK_STATUS;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", getId(), this.component);
    }

    @Override
    public void handleReload(IFile file) {
        IFile componentFile = getModel().getFile(this.component);
        if (componentFile.exists() && componentFile.equals(file)) {
            if (reloadType()) {
                notifyChange();
            }
        }
    }

    private boolean reloadType() {
        ISceneModel model = getModel();
        if (model != null) {
            try {
                this.type = (ComponentTypeNode)model.loadNode(this.component);
                if (this.type != null) {
                    this.type.setModel(model);
                }
            } catch (Throwable e) {
                // no reason to handle exception since having a null type is invalid state, will be caught in validateComponent below
            }
            return true;
        }
        return false;
    }

    @Override
    public Image getIcon() {
        if (this.type != null) {
            return this.type.getIcon();
        } else {
            return super.getIcon();
        }
    }

}
