package com.db;

import net.contentobjects.jnotify.JNotifyListener;
/**
 * Created by darwin on 2017/5/26.
 */
public class FileWatcher implements JNotifyListener{

    CourseDesignModel model;
    FileWatcher(CourseDesignModel m){
        model = m;
        model.connect();
    }
    @Override
    public void fileCreated(int wd, String rootPath, String name) {
        System.err.println("create: --->" + wd + "--->" + rootPath + "--->" + name);
        model.onCreate(rootPath, name);
    }

    @Override
    public void fileDeleted(int wd, String rootPath, String name) {

        System.err.println("delete: --->" + wd + "--->" + rootPath + "--->" + name);
        model.onDelete(rootPath, name);
    }

    @Override
    public void fileModified(int wd, String rootPath, String name) {
        System.err.println("modified: --->" + wd + "--->" + rootPath + "--->" + name);
        model.onModify(rootPath, name);
    }

    @Override
    public void fileRenamed(int wd, String rootPath, String oldName, String newName) {
        System.err.println("rename: --->" + wd + "--->" + rootPath + "--->" + oldName + "--->" + newName);
        model.onRename(rootPath, oldName, newName);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        model.close();
    }
}
