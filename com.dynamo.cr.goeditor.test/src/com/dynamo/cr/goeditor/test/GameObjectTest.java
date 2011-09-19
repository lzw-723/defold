package com.dynamo.cr.goeditor.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;

import javax.inject.Singleton;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.DefaultOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.UndoContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dynamo.cr.editor.core.IResourceType;
import com.dynamo.cr.goeditor.Component;
import com.dynamo.cr.goeditor.EmbeddedComponent;
import com.dynamo.cr.goeditor.GameObjectModel;
import com.dynamo.cr.goeditor.GameObjectPresenter;
import com.dynamo.cr.goeditor.IGameObjectView;
import com.dynamo.cr.goeditor.ILogger;
import com.dynamo.cr.goeditor.ResourceComponent;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Message;

public class GameObjectTest {

    private IGameObjectView view;
    private IGameObjectView.Presenter presenter;
    private GameObjectModel model;
    private IOperationHistory history;
    private IUndoContext undoContext;
    private Injector injector;

    static class TestLogger implements ILogger {

        @Override
        public void logException(Throwable exception) {
            exception.printStackTrace();
        }

    }

    class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(IGameObjectView.class).toInstance(view);
            bind(GameObjectModel.class).in(Singleton.class);

            bind(IOperationHistory.class).toInstance(history);
            bind(IUndoContext.class).toInstance(undoContext);

            bind(ILogger.class).to(TestLogger.class);
        }
    }

    @Before
    public void setup() {
        history = new DefaultOperationHistory();
        undoContext = new UndoContext();
        view = mock(IGameObjectView.class);

        TestModule module = new TestModule();
        injector = Guice.createInjector(module);
        presenter = injector.getInstance(GameObjectPresenter.class);
        model = injector.getInstance(GameObjectModel.class);
    }

    @After
    public void shutdown() {
        presenter.dispose();
    }

    // Helper functions
    void undo() throws ExecutionException {
        history.undo(undoContext, null, null);
    }

    void redo() throws ExecutionException {
        history.redo(undoContext, null, null);
    }

    Component component(int index) {
        return model.getComponents().get(index);
    }

    String id(int index) {
        return component(index).getId();
    }

    String resource(int index) {
        return ((ResourceComponent) component(index)).getResource();
    }

    Message message(int index) {
        return ((EmbeddedComponent) component(index)).getMessage();
    }

    int componentCount() {
        return model.getComponents().size();
    }

    // Actual tests
    @Test
    public void testAddResourceComponent() throws Exception {
        when(view.openAddResourceComponentDialog()).thenReturn("test.script");
        presenter.onAddResourceComponent();

        assertThat(componentCount(), is(1));
        assertThat(resource(0), is("test.script"));

        undo();
        assertThat(componentCount(), is(0));

        redo();
        assertThat(componentCount(), is(1));
        assertThat(resource(0), is("test.script"));
    }

    @Test
    public void testAddEmbeddedComponent() throws Exception {
        Message m1 = DescriptorProtos.FileDescriptorProto.getDefaultInstance();
        Message m2 = DescriptorProtos.ServiceDescriptorProto.getDefaultInstance();

        IResourceType t1 = mock(IResourceType.class);
        when(t1.createTemplateMessage()).thenReturn(m1);
        when(t1.getFileExtension()).thenReturn("script");

        IResourceType t2 = mock(IResourceType.class);
        when(t2.createTemplateMessage()).thenReturn(m2);
        when(t2.getFileExtension()).thenReturn("sprite");

        when(view.openAddEmbeddedComponentDialog()).thenReturn(t1, t2);
        presenter.onAddEmbeddedComponent();
        presenter.onAddEmbeddedComponent();

        assertThat(componentCount(), is(2));
        assertThat(message(0), is(m1));
        assertThat(message(1), is(m2));

        undo();
        assertThat(componentCount(), is(1));

        undo();
        assertThat(componentCount(), is(0));

        redo();
        redo();
        assertThat(componentCount(), is(2));
        assertThat(message(0), is(m1));
        assertThat(message(1), is(m2));
    }

    @Test
    public void testUpdateView() throws Exception {
        int setCount = 0;
        verify(this.view, times(setCount++)).setComponents(anyListOf(Component.class));

        when(view.openAddResourceComponentDialog()).thenReturn("test.script");
        presenter.onAddResourceComponent();

        verify(this.view, times(setCount++)).setComponents(anyListOf(Component.class));
        undo();
        verify(this.view, times(setCount++)).setComponents(anyListOf(Component.class));
        redo();
        verify(this.view, times(setCount++)).setComponents(anyListOf(Component.class));
    }

    @Test
    public void testPreserveOrder() throws Exception {
        when(view.openAddResourceComponentDialog()).thenReturn("test1.script", "test2.script", "test3.script");
        presenter.onAddResourceComponent();
        presenter.onAddResourceComponent();
        presenter.onAddResourceComponent();

        assertThat(componentCount(), is(3));
        assertThat(resource(0), is("test1.script"));
        assertThat(resource(1), is("test2.script"));
        assertThat(resource(2), is("test3.script"));

        undo();
        undo();
        redo();
        redo();
        assertThat(componentCount(), is(3));
        assertThat(resource(0), is("test1.script"));
        assertThat(resource(1), is("test2.script"));
        assertThat(resource(2), is("test3.script"));

        presenter.onRemoveComponent(component(1));
        assertThat(componentCount(), is(2));
        assertThat(resource(0), is("test1.script"));
        assertThat(resource(1), is("test3.script"));

        undo();
        assertThat(componentCount(), is(3));
        assertThat(resource(0), is("test1.script"));
        assertThat(resource(1), is("test2.script"));
        assertThat(resource(2), is("test3.script"));
    }

    @Test
    public void testIds() throws Exception {
        when(view.openAddResourceComponentDialog()).thenReturn("test1.script", "test2.script", "test3.script");
        presenter.onAddResourceComponent();
        presenter.onAddResourceComponent();
        presenter.onAddResourceComponent();

        HashSet<String> ids = new HashSet<String>(Arrays.asList(id(0), id(1), id(2)));
        assertThat(ids, is(new HashSet<String>(Arrays.asList("script", "script0", "script1"))));

        assertThat(model.isOk(), is(true));
    }

    @Test
    public void testDuplicateId() throws Exception {
        when(view.openAddResourceComponentDialog()).thenReturn("test1.script", "test2.script", "test3.script");
        presenter.onAddResourceComponent();
        presenter.onAddResourceComponent();
        presenter.onAddResourceComponent();

        HashSet<String> ids = new HashSet<String>(Arrays.asList(id(0), id(1), id(2)));
        assertThat(ids, is(new HashSet<String>(Arrays.asList("script", "script0", "script1"))));
        assertThat(model.isOk(), is(true));

        presenter.onSetComponentId(component(0), "script0");
        ids = new HashSet<String>(Arrays.asList(id(0), id(1), id(2)));
        assertThat(ids, is(new HashSet<String>(Arrays.asList("script0", "script1"))));
        assertThat(model.isOk(), is(false));
    }

}
