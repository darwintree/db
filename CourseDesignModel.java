package com.db;

import java.security.Timestamp;
import java.util.LinkedList;

/**
 * Created by darwin on 2017/5/26.
 */
public interface CourseDesignModel {

    //连接数据库
    void connect();
    //关闭连接
    void close();

    //用于监视,rootPath是监视的目录， name为目录下的相对路径
    void onModify(String rootPath, String name);
    void onCreate(String rootPath, String name);
    void onDelete(String rootPath, String name);
    void onRename(String rootPath, String oldName, String newName);


    //搜索名字含 name 的文件, mode 代表最后数据的排列顺序（如按照从大到小或从小到大排序），可暂不实现
    //返回值为 满足条件的所有文件的绝对路径 的链表
    LinkedList<String> filterByName(String name, String mode);
    LinkedList<String> filterBySize(long minKb, long maxKb, String mode);
    //筛选从 startTime 到 endTime 之间进行最后一次修改的文件 ( startTime 早于 endTime )
    LinkedList<String> filterByLastModifiedTime(Timestamp startTime, Timestamp endTime, String mode);
}
