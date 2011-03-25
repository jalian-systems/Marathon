package net.sourceforge.marathon.navigator;

import java.io.File;
import java.util.EventListener;

public interface IFileEventListener extends EventListener {

    void fileRenamed(File from, File to);

    void fileDeleted(File file);

    void fileCopied(File from, File to);

    void fileMoved(File from, File to);

    void fileCreated(File file, boolean openInEditor);
    
    void fileUpdated(File file);
}