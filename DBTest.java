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
	String rootPath;
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
		this.rootPath = rootPath;
		insertIntoFolderValues(0,"root",0,-1,-1,-1);
		cur_id=0;
		DFS(this.rootPath,cur_id,1);
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
		if(absolutePath.equals("")) return 1;
		String[] splitedName=absolutePath.split("\\\\");
		int ID=1;
		for(int i=0;i<splitedName.length;i++)
			ID=getIDDirect(splitedName[i],ID);
		return ID;
	}
	public static void main(String args[]){
		System.out.println("start..");
		DBTest dbt=new DBTest();
		dbt.rootPath = "e:\\newFolder";
		//dbt.initDB("e:\\newFolder");
		System.out.println(dbt.getAbsolutePathByID(123));
		System.out.println("end..");

	}
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
		int ID=getID(absolutePath);
		PreparedStatement pStmt1,pStmt2;
		String Query1="select * from my_file where ID=(?)";
		String Query2="select * from my_folder where ID=(?)";
		try{
			pStmt1=conn.prepareStatement(Query1);
			pStmt2=conn.prepareStatement(Query2);
			pStmt1.setInt(1, ID);
			pStmt2.setInt(1, ID);
			ResultSet rs1=pStmt1.executeQuery();
			if(rs1.next()) {
				deleteFileDirect(ID);
			}else{
				deleteFolderDirect(ID);
			}
		}catch(SQLException e){
			e.printStackTrace();
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
	public int getCurMaxID(){
		String Query1="select max(ID) from my_file";
		String Query2="select max(ID) from my_folder";
		PreparedStatement pStmt1;
		PreparedStatement pStmt2;
		int curMaxID=0;
		try{
			pStmt1=conn.prepareStatement(Query1);
			pStmt2=conn.prepareStatement(Query2);
			ResultSet rs1=pStmt1.executeQuery();
			ResultSet rs2=pStmt2.executeQuery();
			if(rs1.next()) if(rs1.getInt(1)>curMaxID) curMaxID=rs1.getInt(1);
			if(rs2.next()) if(rs2.getInt(1)>curMaxID) curMaxID=rs2.getInt(1);
		}catch(SQLException e){
			e.printStackTrace();
		}
		return curMaxID;
	}
	public void setCurID(int cur_id){
		this.cur_id=cur_id;
	}
	public String getAbsolutePathByID(int ID){
		
		String Query1="select * from my_file where ID=(?)";
		String Query2="select * from my_folder where ID=(?)";
		int parentID=0;
		String name="";
		PreparedStatement pStmt1,pStmt2;
		try{
			pStmt1=conn.prepareStatement(Query1);
			pStmt2=conn.prepareStatement(Query2);
			pStmt1.setInt(1, ID);
			pStmt2.setInt(1, ID);
			ResultSet rs2=pStmt2.executeQuery();
			ResultSet rs1=pStmt1.executeQuery();
			
			if(rs1.next()){
				//it's a file
				parentID=rs1.getInt(3);
				name=rs1.getString(2);
			}
			else if(rs2.next()){
				parentID=rs2.getInt(3);
				name=rs2.getString(2);
			}
			
		}catch(SQLException e){
			e.printStackTrace();
		}
		if(parentID==0) return name;
		return getAbsolutePathByID(parentID)+"\\"+name;
	}
	@Override
	public void onModify(String rootPath, String name){
		int ID=getID(name); //��ȡ��ǰ�ļ���ID
		File currentFile=new File(rootPath+"\\"+name);
		String Query;
		long size=currentFile.length();
		long occupiedSpace=(size+1024)/1024*1024;
		PreparedStatement pStmt;
		try{
			if(!currentFile.isDirectory()) Query="update my_file set lastModifyTime = (?),size=(?),occupied_space=(?) where ID=(?)";
			else Query="update my_folder set lastModifyTime = (?) where ID=(?)";
			pStmt=conn.prepareStatement(Query);
			if(currentFile.isDirectory()){
				pStmt.setLong(1,currentFile.lastModified());
				pStmt.setInt(2, ID);
			}else{
				pStmt.setLong(1, currentFile.lastModified());
				pStmt.setLong(2,size);
				pStmt.setLong(3, occupiedSpace);
				pStmt.setInt(4, ID);
			}
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
		//String parentAbsolutePath=""; //新建文件父目录的绝对路径
		String parentAbsolutePath="";
		String absolutePath=rootPath+"\\"+name;
		//String [] sub=absolutePath.split("\\\\");
		String [] sub=name.split("\\\\");
		if(sub.length>1) parentAbsolutePath+=sub[0];
		for(int i=1;i<sub.length-1;i++) parentAbsolutePath=parentAbsolutePath+"\\"+sub[i];
		//parentAbsolutePath=parentAbsolutePath+sub[sub.length-2];
		File currentFile=new File(absolutePath);
		//System.out.println(parentAbsolutePath);
		int parentID=getID(parentAbsolutePath); //获取新建文件父目录的ID
		//System.out.println(parentID);
		//int parentID=getID(rootPath+parentAbsolutePath);
		//System.out.println(parentID);
		int ID=++cur_id; //给新建的文件分配ID
		//获取新建文件的信息
		//System.out.println(ID);
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
		if(currentFile.isDirectory()) insertIntoFolderValues(ID,sub[sub.length-1],parentID,lastModifiedTime,access,depth);
		else insertIntoFileValues(ID,sub[sub.length-1],parentID,lastModifiedTime,size,occupiedSpace,access,depth);
	}
	@Override
	public void onDelete(String rootPath, String name) {
		// TODO Auto-generated method stub
		deleteFile(name);
	}
	@Override
	public void onRename(String rootPath, String oldName, String newName) {
		// TODO Auto-generated method stub
		//String absolutePath=rootPath+oldName;
		int ID=getID(oldName);
		File currentFile=new File(rootPath+"\\"+newName);
		String [] sub=newName.split("\\\\");
		
		String Query;
		PreparedStatement pStmt;
		try{
			if(currentFile.isDirectory()) Query="update my_folder set name = (?) where ID=(?)";
			else Query="update my_file set name = (?) where ID=(?)";
			pStmt=conn.prepareStatement(Query);
			pStmt.setString(1, sub[sub.length-1]);
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
		/*String Query="select ID from my_file where name likes(?)";
		PreparedStatement pStmt;
		ResultSet rs;
		LinkedList<String> lst=new LinkedList<String>();
		try{
			if(mode.equals("acs")) Query+="order by name acs";
			else if(mode.equals("desc")) Query+="order by name desc";
			else{
				System.err.println("不能识别模式");
				return null;
			}
			pStmt=conn.prepareStatement(Query);
			pStmt.setString(0, "%"+name+"%");
			rs=pStmt.executeQuery();
			while(rs.next()){
				int ID=rs.getInt(1);
				String absolutePath=getAbsolutePathByID(ID);
				lst.add(absolutePath);
			}
		}catch(SQLException e){
			e.printStackTrace();
		}*/
		LinkedList<String> lst=executeFileQuery(name,0,0,0,0,0,mode);
		return lst;
	}

	@Override
	public LinkedList<String> fileFilterBySize(long minKb, long maxKb, String mode) {
		/*String Query="select ID from my_file where size>(?) and size<(?)";
		PreparedStatement pStmt;
		ResultSet rs;
		LinkedList<String> lst=new LinkedList<String>();
		try{
			if(mode.equals("acs")) Query+="order by size acs";
			else if(mode.equals("desc")) Query+="order by size desc";
			else{
				System.err.println("不能识别模式");
				return null;
			}
			pStmt=conn.prepareStatement(Query);
			pStmt.setLong(1,minKb*1024);
			pStmt.setLong(2, maxKb*1024);
			rs=pStmt.executeQuery();
			while(rs.next()){
				int ID=rs.getInt(1);
				String absolutePath=getAbsolutePathByID(ID);
				lst.add(absolutePath);
			}
		}catch(SQLException e){
			e.printStackTrace();
		}*/
		LinkedList<String> lst=executeFileQuery("",minKb,maxKb,0,0,1,mode);
		return lst;
	}

	@Override
	public LinkedList<String> fileFilterByLastModifiedTime(long startTime, long endTime, String mode) {
		LinkedList<String> lst=executeFileQuery("",0,0,startTime,endTime,2,mode);
		return lst;
	}

	@Override
	public LinkedList<String> fileFilterByLastModifiedTimeAndSize(long startTime, long endTime, long minKb, long maxKb, String mode) {
		LinkedList<String> lst=executeFileQuery("",minKb,maxKb,startTime,endTime,3,mode);
		return lst;
	}

	@Override
	public LinkedList<String> fileFilterBySizeAndName(String name, long minKb, long maxKb, String mode) {
		LinkedList<String> lst=executeFileQuery(name,minKb,maxKb,0,0,4,mode);
		return lst;
	}

	@Override
	public LinkedList<String> fileFilterByLastModifiedTimeAndName(String name, long startTime, long endTime, String mode) {
		LinkedList<String> lst=executeFileQuery(name,0,0,startTime,endTime,5,mode);
		return lst;
	}

	@Override
	public LinkedList<String> fileFilterByLastModifiedTimeAndSizeAndName(String name, long startTime, long endTime, long minKb, long maxKb, String mode) {
		LinkedList<String> lst=executeFileQuery(name,minKb,maxKb,startTime,endTime,6,mode);
		return lst;
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
	
	public String generateFileQuery(int mode,String order){
		String query="select ID from my_file where ";
		String queryName=" name like (?) ";
		String querySize=" size>(?) and size<(?) ";
		String queryTime=" lastModifyTime>(?) and lastModifyTime<(?) ";
		String orderBy=" order by ";
		switch(mode){
		case 0:{
			query+=queryName+orderBy+" name ";
			break;
		}
		case 1:{
			query+=querySize+orderBy+" size ";
			break;
		}
		case 2:{
			query+=queryTime+orderBy+" lastModifyTime ";
			break;
		}
		case 3:{
			query+=queryTime+" and "+querySize+orderBy+" lastModifyTime , size ";
			break;
		}
		case 4:{
			query+=querySize+" and "+queryName+orderBy+" size,name ";
			break;
		}
		case 5:{
			query+=queryTime+" and "+queryName+orderBy+" lastModifyTime,name ";
			break;
		}
		case 6:{
			query+=queryName+" and "+queryTime+" and "+querySize+orderBy+" lastModifyTime,size,name ";
			break;
 		}
		}
		if(order.equals("asc")) query+=" asc ;";
		else query+=" desc ;";
		return query;
	}
	public LinkedList<String> executeFileQuery(String name,long minKb,long maxKb,long startTime,long endTime,int mode,String order){
		String query=generateFileQuery(mode,order);
		PreparedStatement pStmt;
		LinkedList<String> lst=new LinkedList<String>();
		try{
			pStmt=conn.prepareStatement(query);
			switch(mode){
			case 0:{
				pStmt.setString(1, "%"+name+"%");
				break;
			}
			case 1:{
				pStmt.setLong(1,minKb*1024);
				pStmt.setLong(2, maxKb*1024);
				break;
			}
			case 2:{
				pStmt.setLong(1, startTime);
				pStmt.setLong(2, endTime);
				break;
			}
			case 3:{
				pStmt.setLong(1, startTime);
				pStmt.setLong(2, endTime);
				pStmt.setLong(3, minKb*1024);
				pStmt.setLong(4, maxKb*1024);
				break;
			}
			case 4:{
				pStmt.setLong(1,minKb*1024);
				pStmt.setLong(2, maxKb*1024);
				pStmt.setString(3, "%"+name+"%");
				break;
			}
			case 5:{
				pStmt.setLong(1, startTime);
				pStmt.setLong(2, endTime);
				pStmt.setString(3, "%"+name+"%");
				break;
			}
			case 6:{
				pStmt.setString(1, "%"+name+"%");
				pStmt.setLong(2, startTime);
				pStmt.setLong(3, endTime);
				pStmt.setLong(4,minKb*1024);
				pStmt.setLong(5, maxKb*1024);
				break;
			}
			}
			ResultSet rs=pStmt.executeQuery();
			while(rs.next()){
				int ID=rs.getInt(1);
				String absolutePath=getAbsolutePathByID(ID);
				lst.add(absolutePath);
				
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		return lst;
		
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
