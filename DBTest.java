package com.db;

import java.sql.*;
import java.util.*;
import java.io.File;
//import java.security.Timestamp;


public class DBTest implements CourseDesignModel{
	int cur_id;
	String url;//链接地址
	PreparedStatement insertIntoFile;
	PreparedStatement insertIntoFolder;
	Connection conn;
	DBTest(){
		try{
			Class.forName("com.mysql.jdbc.Driver");
		}catch(ClassNotFoundException e){
			e.printStackTrace();
		}
		cur_id=0;
		url="jdbc:mysql://localhost:3306/myFileSystem?useUnicode=true&characterEncoding=utf-8&useSSL=false";
		try{
			conn=DriverManager.getConnection(url,"newuser","newuser"); //用户名,密码
			insertIntoFile=conn.prepareStatement("insert into my_file values (?,?,?,?,?,?,?,?);");
			insertIntoFolder=conn.prepareStatement("insert into my_folder values (?,?,?,?,?,?)");
		}catch(SQLException e){
			e.printStackTrace();
		}
	}

	void insertIntoFileValues(int id,String name,int parentDirID,long lastModifiedTime,long size,long occupiedSpace,int access,int depth){
		//插入一条记录到my_file表中
		try{
			insertIntoFile.setInt(1, id);
    		insertIntoFile.setString(2, name);
    		insertIntoFile.setInt(3,parentDirID);
    		insertIntoFile.setLong(4, lastModifiedTime);
    		insertIntoFile.setLong(5, size);
    		insertIntoFile.setLong(6, occupiedSpace);
    		insertIntoFile.setInt(7, access);
    		insertIntoFile.setInt(8, depth);
    		insertIntoFile.executeUpdate();
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	void insertIntoFolderValues(int id,String name,int parentDirID,long lastModifiedTime,int access,int depth){
		//插入一条记录到my_folder表中
		try{
			insertIntoFolder.setInt(1, id);
			insertIntoFolder.setString(2, name);
			insertIntoFolder.setInt(3, parentDirID);
			insertIntoFolder.setLong(4, lastModifiedTime);
			insertIntoFolder.setInt(5, access);
			insertIntoFolder.setInt(6, depth);
			insertIntoFolder.executeUpdate();
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	public int DFS(String aPath,int parentDirID,int curDepth){//用DFS遍历根目录包含的子目录，并将其装入数据库中
		int ret=0;
        if ((aPath==null)||(aPath.isEmpty())){
            ret=-1;
            return ret;
        }
        File currentFile=new File(aPath);
        String name=currentFile.getName();
        
        long lastModifiedTime=currentFile.lastModified();
        long size=currentFile.length();
        long occupiedSpace=(size+1024)/1024*1024;
        
        boolean canRead=currentFile.canRead();
        boolean canWrite=currentFile.canWrite();
        boolean isFolder=currentFile.isDirectory();
        int access=-1;
        if(canRead&&canWrite) access=2;
        else if(canRead&&!canWrite) access=0;
        else if(!canRead&&canWrite) access=1;
        cur_id++;
        int FileID=cur_id;
        if(!isFolder){
    		insertIntoFileValues(FileID,name,parentDirID,lastModifiedTime,size,occupiedSpace,access,curDepth);
    	}else{
    		insertIntoFolderValues(FileID,name,parentDirID,lastModifiedTime,access,curDepth);
    		File [] flist=currentFile.listFiles();
    		if(flist!=null)
    			for(File i:flist)
    				DFS(i.getAbsolutePath(),FileID,curDepth+1);
    		
    	}
        return ret;
	}
	public void initDB(String rootPath){ //初始化数据库
		insertIntoFolderValues(0,"root",0,-1,-1,-1);
		cur_id=0;
		DFS(rootPath,cur_id,1); 
	}
	public int getIDDirect(String name,int parentDirID){ //通过文件name与父目录的ID直接在数据库中查找文件ID
		int ID=-1;
		String Query1,Query2;
		Query1="select ID from my_file where parentDirID=(?) and name=(?)";
		Query2="select ID from my_folder where parentDirID=(?) and name=(?)";
		try{
			PreparedStatement pStmt1=conn.prepareStatement(Query1);
			pStmt1.setInt(1, parentDirID);
			pStmt1.setString(2, name);
			PreparedStatement pStmt2=conn.prepareStatement(Query2);
			pStmt2.setInt(1, parentDirID);
			pStmt2.setString(2, name);
			ResultSet rs1=pStmt1.executeQuery();
			ResultSet rs2=pStmt2.executeQuery();
			if(rs1.next()){
				ID=rs1.getInt(1);
				return ID;
			}
			if(rs2.next()){
				ID=rs2.getInt(1);
				return ID;
			}
			
		}catch(SQLException e){
			e.printStackTrace();
		}
		System.err.println("查找的文件不存在");
		return ID;
	}
	public int getID(String absolutePath){ //通过绝对路径在数据库中查找文件ID
		String[] splitedName=absolutePath.split("\\\\");
		int ID=1;
		for(int i=1;i<splitedName.length;i++)
			ID=getIDDirect(splitedName[i],ID);
		return ID;
	}
	/*public static void main(String args[]){
		String rootPath="g:\\";
		DBTest dbt=new DBTest();
		//dbt.initDB(rootPath);
		//int ID=dbt.getID(rootPath);
		//System.out.println(ID);
		dbt.onCreate("g:\\", "newfolder01");

	}*/
	public void deleteFileDirect(int ID){ //直接按ID删除一条my_file 中的记录
		String Query="delete from my_file where ID=(?)";
		PreparedStatement pStmt;
		try{
			pStmt=conn.prepareStatement(Query);
			pStmt.setInt(1, ID);
			pStmt.executeUpdate();
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	public void deleteFolderDirect(int ID){ //直接按ID删除一条my_folder中的记录
		String Query="delete from my_folder where ID=(?)";
		PreparedStatement pStmt;
		try{
			pStmt=conn.prepareStatement(Query);
			pStmt.setInt(1, ID);
			pStmt.executeUpdate();
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	public void deleteFile(String absolutePath){ //按绝对路径删除一条记录
		File currentFile=new File(absolutePath);
		int ID=getID(absolutePath);
		//System.out.println("删除id为"+ID +", path为 "+absolutePath+" 的文件");
		if(currentFile.isFile()){
			deleteFileDirect(ID); //如果该文件是文件类型；直接删除即可
		}else{
			/*File [] flist=currentFile.listFiles();//否者先递归删除其子文件
			if(flist!=null){
				for(File i:flist) deleteFile(i.getAbsolutePath()); 
			}*/
			deleteFolderDirect(ID); //在直接删除目录本身
		}
		return;
	}
	public int getDepthDirect(int ID,int mode){ //查询文件深度，mode=0 是 查询文件的深度，否者查询目录的深度
		String Query;
		if(mode==0) Query="select depth from my_file where ID=(?)";
		else Query="select depth from my_folder where ID=(?)";
		PreparedStatement pStmt;
		int depth=-1;
		try{
			pStmt=conn.prepareStatement(Query);
			pStmt.setInt(1, ID);
			ResultSet rs=pStmt.executeQuery();
			if(rs.next())  depth=rs.getInt(1);
		}catch(SQLException e){
			e.printStackTrace();
		}
		return depth;
	}
	@Override
	public void onModify(String rootPath, String name){
		int ID=getID(rootPath+name); //获取当前文件的ID
		File currentFile=new File(rootPath+name);
		String Query;
		long size=currentFile.length();
		long occupiedSpace=(size+1024)/1024*1024;
		PreparedStatement pStmt;
		try{
			if(currentFile.isDirectory()) Query="update my_folder set lastModifyTime = (?) where ID=(?)";
			else Query="update my_file set lastModifyTime = (?) where ID=(?)";
			pStmt=conn.prepareStatement(Query);
			pStmt.setLong(1, currentFile.lastModified());
			pStmt.setInt(2, ID);
			pStmt.executeUpdate();
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	@Override
	public void connect() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void close() {
		// TODO Auto-generated method stub
		try{
			conn.close();
			insertIntoFile.close();
			insertIntoFolder.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
		
	}
	@Override
	public void onCreate(String rootPath, String name) {
		// TODO Auto-generated method stub
		String parentAbsolutePath=""; //新建文件父目录的绝对路径
		String absolutePath=rootPath+name;
		String [] sub=absolutePath.split("\\\\");
		for(int i=0;i<sub.length-2;i++) parentAbsolutePath=parentAbsolutePath+sub[i]+"\\";
		parentAbsolutePath=parentAbsolutePath+sub[sub.length-2];
		File currentFile=new File(absolutePath);
		int parentID=getID(parentAbsolutePath); //获取新建文件父目录的ID
		int ID=++cur_id; //给新建的文件分配ID
		//获取新建文件的信息
		long lastModifiedTime=currentFile.lastModified();
	    long size=currentFile.length();
        long occupiedSpace=size;
        boolean canRead=currentFile.canRead();
	    boolean canWrite=currentFile.canWrite();
	    int access=-1;
	    int depth=getDepthDirect(parentID,1);
        if(canRead&&canWrite) access=2;
        else if(canRead&&!canWrite) access=0;
	    else if(!canRead&&canWrite) access=1;
        //将新建文件插入到数据库中
		if(currentFile.isDirectory()) insertIntoFolderValues(ID,name,parentID,lastModifiedTime,access,depth); 
		else insertIntoFileValues(ID,name,parentID,lastModifiedTime,size,occupiedSpace,access,depth);
	}
	@Override
	public void onDelete(String rootPath, String name) {
		// TODO Auto-generated method stub
		deleteFile(rootPath+name);
	}
	@Override
	public void onRename(String rootPath, String oldName, String newName) {
		// TODO Auto-generated method stub
		String absolutePath=rootPath+oldName;
		int ID=getID(absolutePath);
		File currentFile=new File(rootPath+newName);
		String Query;
		PreparedStatement pStmt;
		try{
			if(currentFile.isDirectory()) Query="update my_folder set name = (?) where ID=(?)";
			else Query="update my_file set name = (?) where ID=(?)";
			pStmt=conn.prepareStatement(Query);
			pStmt.setString(1, newName);
			pStmt.setInt(2, ID);
			pStmt.executeUpdate();
		}catch(SQLException e){
			e.printStackTrace();
		}
		
	}
	@Override
	public LinkedList<String> filterByName(String name, String mode) {
		// TODO Auto-generated method stub
		
		return null;
	}
	@Override
	public LinkedList<String> filterBySize(long minKb, long maxKb, String mode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedList<String> filterByLastModifiedTime(long startTime, long endTime, String mode) {
		return null;
	}

	@Override
	public LinkedList<String> filterByLastModifiedTimeAndSize(long startTime, long endTime, long minKb, long maxKb, String mode) {
		return null;
	}

	@Override
	public LinkedList<String> filterBySizeAndName(String name, long minKb, long maxKb, String mode) {
		return null;
	}

	@Override
	public LinkedList<String> filterByLastModifiedTimeAndName(String name, long startTime, long endTime, String mode) {
		return null;
	}

	@Override
	public LinkedList<String> filterByLastModifiedTimeAndSizeAndName(String name, long startTime, long endTime, long minKb, long maxKb, String mode) {
		return null;
	}

	@Override
	public LinkedList<String> fileFilterByName(String name, String mode) {
		return null;
	}

	@Override
	public LinkedList<String> fileFilterBySize(long minKb, long maxKb, String mode) {
		return null;
	}

	@Override
	public LinkedList<String> fileFilterByLastModifiedTime(long startTime, long endTime, String mode) {
		return null;
	}

	@Override
	public LinkedList<String> fileFilterByLastModifiedTimeAndSize(long startTime, long endTime, long minKb, long maxKb, String mode) {
		return null;
	}

	@Override
	public LinkedList<String> fileFilterBySizeAndName(String name, long minKb, long maxKb, String mode) {
		return null;
	}

	@Override
	public LinkedList<String> fileFilterByLastModifiedTimeAndName(String name, long startTime, long endTime, String mode) {
		return null;
	}

	@Override
	public LinkedList<String> fileFilterByLastModifiedTimeAndSizeAndName(String name, long startTime, long endTime, long minKb, long maxKb, String mode) {
		return null;
	}

	@Override
	public LinkedList<String> folderFilterByName(String name, String mode) {
		return null;
	}

	@Override
	public LinkedList<String> folderFilterBySize(long minKb, long maxKb, String mode) {
		return null;
	}

	@Override
	public LinkedList<String> folderFilterByLastModifiedTime(long startTime, long endTime, String mode) {
		return null;
	}

	@Override
	public LinkedList<String> folderFilterByLastModifiedTimeAndSize(long startTime, long endTime, long minKb, long maxKb, String mode) {
		return null;
	}

	@Override
	public LinkedList<String> folderFilterBySizeAndName(String name, long minKb, long maxKb, String mode) {
		return null;
	}

	@Override
	public LinkedList<String> folderFilterByLastModifiedTimeAndName(String name, long startTime, long endTime, String mode) {
		return null;
	}

	@Override
	public LinkedList<String> folderFilterByLastModifiedTimeAndSizeAndName(String name, long startTime, long endTime, long minKb, long maxKb, String mode) {
		return null;
	}

//	@Override
//	public LinkedList<String> filterByLastModifiedTime(Timestamp startTime, Timestamp endTime, String mode) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	public void setCurID(int cur_id){
//		this.cur_id=cur_id;
//	}
}
