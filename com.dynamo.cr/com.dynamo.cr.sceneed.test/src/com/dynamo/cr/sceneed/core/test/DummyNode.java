package com.dynamo.cr.sceneed.core.test;

import org.eclipse.core.resources.IFile;

import com.dynamo.cr.properties.DynamicPropertyAccessor;
import com.dynamo.cr.properties.IPropertyAccessor;
import com.dynamo.cr.properties.Property;
import com.dynamo.cr.sceneed.core.ISceneModel;
import com.dynamo.cr.sceneed.core.Node;

@SuppressWarnings("serial")
public class DummyNode extends Node {

    public static String DYNAMIC_PROPERTY = "dynamicProperty";

    @Property
    private int dummyProperty;

    private Integer dynamicProperty;

    public int getDummyProperty() {
        return this.dummyProperty;
    }

    public void setDummyProperty(int dummyProperty) {
        this.dummyProperty = dummyProperty;
    }

    public void addChild(DummyChild child) {
        super.addChild(child);
    }

    @Override
    public boolean handleReload(IFile file) {
        return file.exists();
    }

    class Accessor implements IPropertyAccessor<DummyNode, ISceneModel> {

        @Override
        public void setValue(DummyNode obj, String property, Object value, ISceneModel world) {
            if (property.equals(DYNAMIC_PROPERTY)) {
                dynamicProperty = (Integer)value;
            } else {
                throw new RuntimeException();
            }
        }

        @Override
        public void resetValue(DummyNode obj, String property, ISceneModel world) {
            if (property.equals(DYNAMIC_PROPERTY)) {
                dynamicProperty = null;
            } else {
                throw new RuntimeException();
            }
        }

        @Override
        public Object getValue(DummyNode obj, String property, ISceneModel world) {
            if (property.equals(DYNAMIC_PROPERTY)) {
                if (dynamicProperty != null) {
                    return dynamicProperty;
                } else {
                    return 0;
                }
            } else {
                throw new RuntimeException();
            }
        }

        @Override
        public boolean isEditable(DummyNode obj, String property, ISceneModel world) {
            return true;
        }

        @Override
        public boolean isVisible(DummyNode obj, String property, ISceneModel world) {
            return true;
        }

        @Override
        public boolean isOverridden(DummyNode obj, String property, ISceneModel world) {
            if (property.equals(DYNAMIC_PROPERTY)) {
                return dynamicProperty != null;
            } else {
                throw new RuntimeException();
            }
        }

        @Override
        public Object[] getPropertyOptions(DummyNode obj, String property, ISceneModel world) {
            return null;
        }
    }

    @DynamicPropertyAccessor
    public IPropertyAccessor<DummyNode, ISceneModel> getDynamicAccessor(ISceneModel world) {
        return new Accessor();
    }
}
