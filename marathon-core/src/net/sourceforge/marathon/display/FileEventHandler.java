package net.sourceforge.marathon.display;

import java.io.File;

import javax.swing.event.EventListenerList;

import net.sourceforge.marathon.navigator.IFileEventListener;

public class FileEventHandler {
    private EventListenerList listeners = new EventListenerList();

    public void addFileEventListener(IFileEventListener fileEventListener) {
        listeners.add(IFileEventListener.class, fileEventListener);
    }

    public void fireRenameEvent(File from, File to) {
        IFileEventListener[] la = listeners.getListeners(IFileEventListener.class);
        for (IFileEventListener l : la) {
            l.fileRenamed(from, to);
        }
    }

    public void fireDeleteEvent(File file) {
        IFileEventListener[] la = listeners.getListeners(IFileEventListener.class);
        for (IFileEventListener l : la) {
            l.fileDeleted(file);
        }
    }

    public void fireCopyEvent(File from, File to) {
        IFileEventListener[] la = listeners.getListeners(IFileEventListener.class);
        for (IFileEventListener l : la) {
            l.fileCopied(from, to);
        }
    }

    public void fireMoveEvent(File from, File to) {
        IFileEventListener[] la = listeners.getListeners(IFileEventListener.class);
        for (IFileEventListener l : la) {
            l.fileMoved(from, to);
        }
    }

    public void fireNewEvent(File file, boolean openInEditor) {
        IFileEventListener[] la = listeners.getListeners(IFileEventListener.class);
        for (IFileEventListener l : la) {
            l.fileCreated(file, openInEditor);
        }
    }

    public void fireUpdateEvent(File file) {
        IFileEventListener[] la = listeners.getListeners(IFileEventListener.class);
        for (IFileEventListener l : la) {
            l.fileUpdated(file);
        }
    }
}
