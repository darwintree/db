package com.db;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;

public class Main {

    public static void main(String[] args) {
        System.err.println(System.getProperty("java.library.path"));
        System.err.println("开始监听目录下内容......");
        try {
            Main.sample();
            //同步测试
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * JNotify监控方法
     * @throws JNotifyException
     * @throws InterruptedException
     */
    private static void sample() throws JNotifyException, InterruptedException {

        //要监控哪个目录
        String path = "F:\\";

        //监控用户的操作，增，删，改，重命名
        int mask = JNotify.FILE_CREATED | JNotify.FILE_DELETED | JNotify.FILE_MODIFIED | JNotify.FILE_RENAMED ;

        //是否监控子目录
        boolean subTree = true;

        //开始监控
        CourseDesignModel m = new DBTest();
        DBTest db = (DBTest) m;

        int watchID = JNotify.addWatch(path, mask, subTree, new FileWatcher(m));

        //睡一会，看看效果
        Thread.sleep(1000 * 60 * 3);

        //停止监控
        boolean res = JNotify.removeWatch(watchID);

        if (res) {
            System.err.println("已停止监听");
        }
        System.err.println(path);
    }
}
