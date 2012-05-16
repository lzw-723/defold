package com.dynamo.cr.properties.descriptors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.resources.IContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.dynamo.cr.properties.IPropertyEditor;
import com.dynamo.cr.properties.IPropertyModel;
import com.dynamo.cr.properties.IPropertyObjectWorld;
import com.dynamo.cr.properties.Property.EditorType;
import com.dynamo.cr.properties.PropertyDesc;
import com.dynamo.cr.properties.PropertyUtil;

public abstract class ScalarPropertyDesc<S, T, U extends IPropertyObjectWorld> extends PropertyDesc<T, U>  {

    private EditorType editorType;

    public ScalarPropertyDesc(String id, String name, EditorType editorType) {
        super(id, name);
        this.editorType = editorType;
    }

    public abstract S fromString(String text);

    /**
     * Widet abstraction classs for Text/Combo-box
     * @author chmu
     */
    private class EditorWidget {
        private Text text;
        private Combo combo;

        public EditorWidget(Composite parent) {
            if (editorType == EditorType.DEFAULT) {
                text = new Text(parent, SWT.BORDER);
            } else {
                combo = new Combo(parent, SWT.DROP_DOWN);
            }
        }

        private void setText(String text) {
            if (editorType == EditorType.DEFAULT)
                this.text.setText(text);
            else
                this.combo.setText(text);
        }

        private String getText() {
            if (editorType == EditorType.DEFAULT)
                return this.text.getText();
            else
                return this.combo.getText();
        }

        public Control getControl() {
            if (editorType == EditorType.DEFAULT)
                return this.text;
            else
                return this.combo;
        }

        private void updateOptions(IPropertyModel<T, U>[] models) {
            if (editorType == EditorType.DROP_DOWN) {
                if (models.length == 1) {
                    /*
                     * When only set options for single selections in order
                     * to avoid potential performance problems
                     */
                    List<String> lst = new ArrayList<String>();
                    for (Object o : models[0].getPropertyOptions(getId())) {
                        lst.add(o.toString());
                    }
                    combo.setItems(lst.toArray(new String[lst.size()]));
                } else {
                    combo.setItems(new String[] {});
                }
            }
        }

        public void addListener(int eventType, Listener listener) {
            if (editorType == EditorType.DEFAULT) {
                text.addListener(eventType, listener);
            } else {
                combo.addListener(eventType, listener);
            }
        }
    }

    private class Editor implements IPropertyEditor<T, U>, Listener {

        private String oldValue;
        private IPropertyModel<T, U>[] models;
        private EditorWidget widget;
        Editor(Composite parent) {
            widget = new EditorWidget(parent);
            widget.addListener(SWT.KeyDown, this);
            widget.addListener(SWT.FocusOut, this);
            widget.addListener(SWT.Selection, this);
        }

        @Override
        public void dispose() {
        }

        @Override
        public Control getControl() {
            return widget.getControl();
        }

        @SuppressWarnings("unchecked")
        @Override
        public void refresh() {
            widget.updateOptions(models);
            boolean editable = models[0].isPropertyEditable(getId());
            getControl().setEnabled(editable);
            S firstValue = (S) models[0].getPropertyValue(getId());
            boolean overridden = models[0].isPropertyOverridden(getId());
            for (int i = 1; i < models.length; ++i) {
                if (models[i].isPropertyOverridden(getId())) {
                    overridden = true;
                }
                S value = (S) models[i].getPropertyValue(getId());
                if (!firstValue.equals(value)) {
                    widget.setText("");
                    widget.getControl().setBackground(new Color(getControl().getDisplay(), 255, 255, 255));
                    oldValue = "";
                    return;
                }
            }
            widget.setText(firstValue.toString());
            if (overridden) {
                widget.getControl().setBackground(new Color(getControl().getDisplay(), 214, 230, 255));
            } else {
                widget.getControl().setBackground(new Color(getControl().getDisplay(), 255, 255, 255));
            }
            oldValue = firstValue.toString();
        }

        @Override
        public void setModels(IPropertyModel<T, U>[] models) {
            this.models = models;
        }

        @Override
        public void handleEvent(Event event) {
            S value = fromString(widget.getText());
            if (value == null)
                value = fromString("0");

            boolean updateValue = false;
            if (event.type == SWT.KeyDown && (event.character == '\r' || event.character == '\n')) {
                updateValue = true;
            } else if (event.type == SWT.FocusOut && !value.equals(oldValue)) {
                updateValue = true;
            } else if (event.type == SWT.Selection && !value.equals(oldValue)) {
                updateValue = true;
            }
            if (updateValue) {
                IUndoableOperation combinedOperation = PropertyUtil.setProperty(models, getId(), value);
                if (combinedOperation != null)
                    models[0].getCommandFactory().execute(combinedOperation, models[0].getWorld());

                oldValue = value.toString();
            }
        }
    }

    @Override
    public IPropertyEditor<T, U> createEditor(Composite parent, IContainer contentRoot) {
        return new Editor(parent);
    }

}
