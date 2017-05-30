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

    //搜索名字含 name 的文件和文件夹, mode 代表最后数据的排列顺序 ("asc", "desc")
    LinkedList<String> filterByName(String name, String mode);
    //通过大小筛选文件
    LinkedList<String> filterBySize(long minKb, long maxKb, String mode);
    //筛选从 startTime 到 endTime 之间进行最后一次修改的文件和文件夹 ( startTime 早于 endTime )
    LinkedList<String> filterByLastModifiedTime(long startTime, long endTime, String mode);
    //通过大小和修改时间筛选
    LinkedList<String> filterByLastModifiedTimeAndSize(long startTime, long endTime,long minKb,long maxKb, String mode);
    //通过大小和名字筛选
    LinkedList<String> filterBySizeAndName(String name,long minKb, long maxKb, String mode);
    //通过修改时间和名字筛选
    LinkedList<String> filterByLastModifiedTimeAndName(String name,long startTime, long endTime, String mode);
    //通过大小和修改时间和名字筛选
    LinkedList<String> filterByLastModifiedTimeAndSizeAndName(String name,long startTime, long endTime,long minKb,long maxKb, String mode);


    //搜索名字含 name 的文件, mode 代表最后数据的排列顺序
    LinkedList<String> fileFilterByName(String name, String mode);
    LinkedList<String> fileFilterBySize(long minKb, long maxKb, String mode);
    //筛选从 startTime 到 endTime 之间进行最后一次修改的文件 ( startTime 早于 endTime )
    LinkedList<String> fileFilterByLastModifiedTime(long startTime, long endTime, String mode);
    LinkedList<String> fileFilterByLastModifiedTimeAndSize(long startTime, long endTime,long minKb,long maxKb, String mode);
    LinkedList<String> fileFilterBySizeAndName(String name,long minKb, long maxKb, String mode);
    LinkedList<String> fileFilterByLastModifiedTimeAndName(String name,long startTime, long endTime, String mode);
    LinkedList<String> fileFilterByLastModifiedTimeAndSizeAndName(String name,long startTime, long endTime,long minKb,long maxKb, String mode);


    //搜索名字含 name 的文件夹, mode 代表最后数据的排列顺序
    LinkedList<String> folderFilterByName(String name, String mode);
    LinkedList<String> folderFilterBySize(long minKb, long maxKb, String mode);
    //筛选从 startTime 到 endTime 之间进行最后一次修改的文件夹 ( startTime 早于 endTime )
    LinkedList<String> folderFilterByLastModifiedTime(long startTime, long endTime, String mode);
    LinkedList<String> folderFilterByLastModifiedTimeAndSize(long startTime, long endTime,long minKb,long maxKb, String mode);
    LinkedList<String> folderFilterBySizeAndName(String name,long minKb, long maxKb, String mode);
    LinkedList<String> folderFilterByLastModifiedTimeAndName(String name,long startTime, long endTime, String mode);
    LinkedList<String> folderFilterByLastModifiedTimeAndSizeAndName(String name,long startTime, long endTime,long minKb,long maxKb, String mode);
}
