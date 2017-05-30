package com.db;

/**
 * Created by darwin on 2017/5/30.
 */

import java.awt.EventQueue;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.awt.FlowLayout;
import javax.swing.JTextField;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class MainWindow {

    private JFrame frame;
    private JTextField textField;//最上面的那个输入名字的编辑框
    private JTextField textField_1;//Min Size 编辑框
    private JTextField textField_2;//Max Size编辑框
    private JTextField textField_3;//StartTime编辑框
    private JTextField textField_4;//EndTime编辑框
    private int typefile;//记录是否搜索文件，是为1，否为0
    private int typefolder;//记录是否搜索文件夹，是为1，否为0
    private int order;//0升序  1降序
    private String name;//记录搜索框中输入的文件名
    private boolean useTime;//是否通过时间筛选
    private boolean useSize;//是否通过大小筛选
    private boolean useName;//是否通过名称筛选
    private DBTest dbt;
    private LinkedList<String> fl;//储存返回的列表(绝对路径列表)
    private LinkedList<String> nl;//储存姓名的列表
    JList<String> nameList;		//GUI中显示姓名的列表
    JList<String> posList;      //GUI中显示绝对路径的列表
    JPopupMenu popmenu;
    String selectpath;			//
    

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MainWindow window = new MainWindow();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public MainWindow() {
        typefile=0;
        typefolder=0;

        order=0;
        useSize=false;
        useTime=false;
        useName=false;
        dbt=new DBTest();
        fl=new LinkedList<String>();
        nl=new LinkedList<String>();
        selectpath=null;
        initialize();

    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        //窗口大小
        frame.setBounds(100, 100, 550, 620);
        //frame.setSize( 550, 620);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        JMenu mnNewMenu = new JMenu("File");
        menuBar.add(mnNewMenu);

        JMenuItem mntmClose = new JMenuItem("Close");
        mnNewMenu.add(mntmClose);

        JMenu mnSearch = new JMenu("Search");
        menuBar.add(mnSearch);

        JCheckBoxMenuItem chckbxmntmNewCheckItem = new JCheckBoxMenuItem("File");
        mnSearch.add(chckbxmntmNewCheckItem);
        chckbxmntmNewCheckItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO 自动生成的方法存根
                if(chckbxmntmNewCheckItem.isSelected())
                    typefile=1;
                else
                    typefile=0;

            }
        });
        //选项菜单search by folder
        JCheckBoxMenuItem chckbxmntmNewCheckItem_1 = new JCheckBoxMenuItem("Folder");
        mnSearch.add(chckbxmntmNewCheckItem_1);
        chckbxmntmNewCheckItem_1.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO 自动生成的方法存根
                if(chckbxmntmNewCheckItem_1.isSelected())
                    typefolder=1;
                else
                    typefolder=0;

            }
        });
        //选项菜单search by name
        JCheckBoxMenuItem chckbxmntmNewCheckItem_byname = new JCheckBoxMenuItem("search by name");
        mnSearch.add(chckbxmntmNewCheckItem_byname);
        chckbxmntmNewCheckItem_byname.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO 自动生成的方法存根
                if(chckbxmntmNewCheckItem_byname.isSelected())
                    useName=true;
                else
                    useName=false;

            }
        });
        //选项菜单search by size
        JCheckBoxMenuItem chckbxmntmNewCheckItem_2 = new JCheckBoxMenuItem("search by size");
        mnSearch.add(chckbxmntmNewCheckItem_2);
        chckbxmntmNewCheckItem_2.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO 自动生成的方法存根
                if(chckbxmntmNewCheckItem_2.isSelected())
                    useSize=true;
                else
                    useSize=false;

            }
        });
        //选项菜单search by lastmodified
        JCheckBoxMenuItem chckbxmntmNewCheckItem_3 = new JCheckBoxMenuItem("search by lastmotified");
        mnSearch.add(chckbxmntmNewCheckItem_3);
        chckbxmntmNewCheckItem_3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO 自动生成的方法存根
                if(chckbxmntmNewCheckItem_3.isSelected())
                    useTime=true;
                else
                    useTime=false;

            }
        });


        JPanel panel = new JPanel();
        frame.getContentPane().add(panel, BorderLayout.NORTH);
        panel.setLayout(new GridLayout(2, 1, 0, 0));

        JPanel panel_1 = new JPanel();
        panel.add(panel_1);
        panel_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        textField = new JTextField();
        panel_1.add(textField);
        textField.setColumns(40);

        JButton btnNewButton = new JButton("Search");
        panel_1.add(btnNewButton);
        btnNewButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO 自动生成的方法存根

				/*if(typefile==1&&typefolder==1)
					textField.setText("文件+文件夹");
				if(typefile==0&&typefolder==1)
					textField.setText("文件夹");
				if(typefile==1&&typefolder==0)
				textField.setText("文件");
				if(typefile==0&&typefolder==0)
					textField.setText("啥也没有");
				if(order==0)
					textField.setText("111");
				else
					textField.setText("2222");

				if(useSize&&useTime)
					textField.setText("3");
				if(!useSize&&useTime)
					textField.setText("2");
				if(useSize&&!useTime)
					textField.setText("1");
				if(!useSize&&!useTime)
					textField.setText("0");*/
                //fl.add("E:\\sdsd\\aaaa");
                if(typefile==0&&typefolder==0){
                    JOptionPane.showMessageDialog(frame, "请选择搜索类型");
                    return;
                }

                String st,et;
                DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                long minKb=0;
                long maxKb=0;
                long startTime=0 ;
                long endTime=0;
                st=textField_3.getText();
                et=textField_4.getText();
                //String转化为long
                if(useTime){
                    try {
                        startTime=sdf.parse(st).getTime();
                        endTime=sdf.parse(et).getTime();
                    } catch (ParseException e1) {
                        e1.printStackTrace();}
                }

                minKb=Long.parseLong(textField_1.getText());
                maxKb=Long.parseLong(textField_2.getText());
                name=textField.getText();
                //若输入姓名区域为空，则按其他筛选项搜索
                if(name==null){
                    //文件+文件夹
                    if(typefile==1&&typefolder==1){
                        //通过大小和时间
                        if(useSize&&useTime){
                            if(order==0)
                                fl=dbt.filterByLastModifiedTimeAndSize(startTime, endTime,minKb,maxKb, "asc");
                            else
                                fl=dbt.filterByLastModifiedTimeAndSize(startTime, endTime,minKb,maxKb, "desc");
                        }
                        //通过时间
                        if(!useSize&&useTime){
                            if(order==0)
                                fl=dbt.filterByLastModifiedTime(startTime, endTime, "asc");
                            else
                                fl=dbt.filterByLastModifiedTime(startTime, endTime,"desc");
                        }
                        //通过大小
                        if(useSize&&!useTime){
                            if(order==0)
                                fl=dbt.filterBySize(minKb,maxKb, "asc");
                            else
                                fl=dbt.filterBySize(minKb,maxKb,"desc");
                        }
                        //不通过大小也不通过时间，因为也不通过姓名，所以给出错误提示
                        if(!useSize&&!useTime){
                            JOptionPane.showMessageDialog(frame, "请输入文件名或选择筛选条件");
                            return;
                        }

                    }


                    //文件夹 其余代码同功能类比上述
                    if(typefile==0&&typefolder==1){
                        if(useSize&&useTime){
                            if(order==0)
                                fl=dbt.folderFilterByLastModifiedTimeAndSize(startTime, endTime,minKb,maxKb, "asc");
                            else
                                fl=dbt.folderFilterByLastModifiedTimeAndSize(startTime, endTime,minKb,maxKb, "desc");
                        }

                        if(!useSize&&useTime){
                            if(order==0)
                                fl=dbt.folderFilterByLastModifiedTime(startTime, endTime, "asc");
                            else
                                fl=dbt.folderFilterByLastModifiedTime(startTime, endTime,"desc");
                        }

                        if(useSize&&!useTime){
                            if(order==0)
                                fl=dbt.folderFilterBySize(minKb,maxKb, "asc");
                            else
                                fl=dbt.folderFilterBySize(minKb,maxKb,"desc");
                        }

                        if(!useSize&&!useTime){
                            JOptionPane.showMessageDialog(frame, "请输入文件名或选择筛选条件");
                            return;
                        }
                    }

                    //文件
                    if(typefile==1&&typefolder==0){
                        if(useSize&&useTime){
                            if(order==0)
                                fl=dbt.fileFilterByLastModifiedTimeAndSize(startTime, endTime,minKb,maxKb, "asc");
                            else
                                fl=dbt.fileFilterByLastModifiedTimeAndSize(startTime, endTime,minKb,maxKb, "desc");
                        }

                        if(!useSize&&useTime){
                            if(order==0)
                                fl=dbt.fileFilterByLastModifiedTime(startTime, endTime, "asc");
                            else
                                fl=dbt.fileFilterByLastModifiedTime(startTime, endTime,"desc");
                        }

                        if(useSize&&!useTime){
                            if(order==0)
                                fl=dbt.fileFilterBySize(minKb,maxKb, "asc");
                            else
                                fl=dbt.fileFilterBySize(minKb,maxKb,"desc");
                        }

                        if(!useSize&&!useTime){
                            JOptionPane.showMessageDialog(frame, "请输入文件名或选择筛选条件");
                            return;
                        }
                    }
                }
                //名称不为空，则通过名字加其他搜索条件一起搜索
                else{
                    if(typefile==1&&typefolder==1){
                        if(useSize&&useTime){
                            if(order==0)
                                fl=dbt.filterByLastModifiedTimeAndSizeAndName(name,startTime, endTime,minKb,maxKb, "asc");
                            else
                                fl=dbt.filterByLastModifiedTimeAndSizeAndName(name,startTime, endTime,minKb,maxKb, "desc");
                        }

                        if(!useSize&&useTime){
                            if(order==0)
                                fl=dbt.filterByLastModifiedTimeAndName(name,startTime, endTime, "asc");
                            else
                                fl=dbt.filterByLastModifiedTimeAndName(name,startTime, endTime,"desc");
                        }

                        if(useSize&&!useTime){
                            if(order==0)
                                fl=dbt.filterBySizeAndName(name,minKb,maxKb, "asc");
                            else
                                fl=dbt.filterBySizeAndName(name,minKb,maxKb,"desc");
                        }

                        if(!useSize&&!useTime){
                            if(order==0)
                                fl=dbt.filterByName(name, "asc");
                            else
                                fl=dbt.filterByName(name,"desc");
                        }

                    }



                    if(typefile==0&&typefolder==1){
                        if(useSize&&useTime){
                            if(order==0)
                                fl=dbt.folderFilterByLastModifiedTimeAndSizeAndName(name,startTime, endTime,minKb,maxKb, "asc");
                            else
                                fl=dbt.folderFilterByLastModifiedTimeAndSizeAndName(name,startTime, endTime,minKb,maxKb, "desc");
                        }

                        if(!useSize&&useTime){
                            if(order==0)
                                fl=dbt.folderFilterByLastModifiedTimeAndName(name,startTime, endTime, "asc");
                            else
                                fl=dbt.folderFilterByLastModifiedTimeAndName(name,startTime, endTime,"desc");
                        }

                        if(useSize&&!useTime){
                            if(order==0)
                                fl=dbt.folderFilterBySizeAndName(name,minKb,maxKb, "asc");
                            else
                                fl=dbt.folderFilterBySizeAndName(name,minKb,maxKb,"desc");
                        }

                        if(!useSize&&!useTime){
                            if(order==0)
                                fl=dbt.folderFilterByName(name, "asc");
                            else
                                fl=dbt.folderFilterByName(name,"desc");
                        }
                    }


                    if(typefile==1&&typefolder==0){
                        if(useSize&&useTime){
                            if(order==0)
                                fl=dbt.fileFilterByLastModifiedTimeAndSizeAndName(name,startTime, endTime,minKb,maxKb, "asc");
                            else
                                fl=dbt.fileFilterByLastModifiedTimeAndSizeAndName(name,startTime, endTime,minKb,maxKb, "desc");
                        }

                        if(!useSize&&useTime){
                            if(order==0)
                                fl=dbt.fileFilterByLastModifiedTimeAndName(name,startTime, endTime, "asc");
                            else
                                fl=dbt.fileFilterByLastModifiedTimeAndName(name,startTime, endTime,"desc");
                        }

                        if(useSize&&!useTime){
                            if(order==0)
                                fl=dbt.fileFilterBySizeAndName(name,minKb,maxKb, "asc");
                            else
                                fl=dbt.fileFilterBySizeAndName(name,minKb,maxKb,"desc");
                        }

                        if(!useSize&&!useTime){
                            if(order==0)
                                fl=dbt.fileFilterByName(name, "asc");
                            else
                                fl=dbt.fileFilterByName(name,"desc");
                        }
                    }
                }
                //fl=new LinkedList();
                //fl.add("E:\\aaaa\\bbbb");
                //fl.add("D:\\aaaa\\bddddb");
                //将两个列表输出到namelist和poslist视图区
                DefaultListModel dlm1=new DefaultListModel();
                DefaultListModel dlm2=new DefaultListModel();
                if(fl==null){
                    System.out.println("fl is null!");
                }
                if(fl!=null&&!fl.isEmpty()){
                    for(String str:fl){
                        //将fl中元素str的文件名取出
                        int a=str.lastIndexOf("\\");
                        String Name=str.substring(a+1, str.length());
                        nl.add(Name);
                    }
                    for(String str:nl){
                        dlm1.addElement(str);
                    }
                    for(String str:fl){
                        dlm2.addElement(str);
                    }
                    nameList.setModel(dlm1);
                    posList.setModel(dlm2);

                    fl.clear();
                    nl.clear();
                }


                //

            }
        });

        JPanel panel_2 = new JPanel();
        panel.add(panel_2);
        panel_2.setLayout(new GridLayout(2, 5, 0, 0));

        JLabel lblNewLabel = new JLabel("Min Size");
        lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        panel_2.add(lblNewLabel);

        textField_1 = new JTextField();
        panel_2.add(textField_1);
        textField_1.setColumns(10);
        textField_1.setText("0");


        JLabel lblNewLabel_1 = new JLabel("Max Size");
        lblNewLabel_1.setHorizontalAlignment(SwingConstants.RIGHT);
        panel_2.add(lblNewLabel_1);

        textField_2 = new JTextField();
        panel_2.add(textField_2);
        textField_2.setColumns(10);
        textField_2.setText("0");

        JRadioButton jrbuttonsx = new JRadioButton("升序");
        panel_2.add(jrbuttonsx);

        JLabel lblNewLabel_3 = new JLabel("Start Time");
        lblNewLabel_3.setHorizontalAlignment(SwingConstants.RIGHT);
        panel_2.add(lblNewLabel_3);



        textField_3 = new JTextField();
        panel_2.add(textField_3);
        textField_3.setColumns(10);
        textField_3.setText("0");
        //日期格式yy-mm-dd hh:mm:ss

        JLabel lblNewLabel_4 = new JLabel("End Time");
        lblNewLabel_4.setHorizontalAlignment(SwingConstants.RIGHT);
        panel_2.add(lblNewLabel_4);


        textField_4 = new JTextField();
        panel_2.add(textField_4);
        textField_4.setColumns(15);
        textField_4.setText("0");

        JRadioButton jrbuttonjx = new JRadioButton("降序");
        panel_2.add(jrbuttonjx);

        jrbuttonsx.setSelected(true);
        jrbuttonsx.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // TODO 自动生成的方法存根
                if(jrbuttonjx.isSelected())
                    order=1;
            }
        });
        jrbuttonsx.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // TODO 自动生成的方法存根
                if(jrbuttonsx.isSelected())
                    order=0;
            }
        });
        ButtonGroup paixu=new ButtonGroup();
        paixu.add(jrbuttonjx);
        paixu.add(jrbuttonsx);

        JPanel listpanel=new JPanel();
        listpanel.setLayout(new GridLayout(1, 1));

        nameList=new JList<String>();
        posList=new JList<String>();
        JScrollPane nsp=new JScrollPane(nameList);
        JScrollPane psp=new JScrollPane(posList);
        JScrollBar nsb= nsp.getVerticalScrollBar();
        JScrollBar psb= psp.getVerticalScrollBar();
        nsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        psb.addAdjustmentListener(new AdjustmentListener() {
			
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				// TODO 自动生成的方法存根
				nsb.setValue(psb.getValue());
			}
		});
        listpanel.add(nsp);
        listpanel.add(psp);
        DefaultListModel dlm = new DefaultListModel();
        dlm.addElement("日期格式yy-mm-dd hh:mm:ss");
        nameList.setModel(dlm);
        
        frame.add(listpanel);
        nameList.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// TODO 自动生成的方法存根
				int index=nameList.getSelectedIndex();
				posList.setSelectedIndex(index);				
			}
		});
        posList.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// TODO 自动生成的方法存根
				int index=posList.getSelectedIndex();
				nameList.setSelectedIndex(index);				
			}
		});
        
        
        nameList.addMouseListener(new MouseListener() {			
			@Override
			public void mouseReleased(MouseEvent e) {}
			
			@Override
			public void mousePressed(MouseEvent e) {}
			
			@Override
			public void mouseExited(MouseEvent e) {	}
			
			@Override
			public void mouseEntered(MouseEvent e) {}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO 自动生成的方法存根
				if (e.getButton()==MouseEvent.BUTTON3) {  
					 int index = nameList.locationToIndex(e.getPoint());  
				      nameList.setSelectedIndex(index);  
				      posList.setSelectedIndex(index);
				      selectpath=posList.getSelectedValue();
				      popmenu.show(nameList, e.getX(), e.getY());
				      
				}
			}
		});       
        popmenu=new JPopupMenu();
        JMenuItem openfile1=new JMenuItem("打开此文件");
        openfile1.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO 自动生成的方法存根
				try {
					String sp=new String("e:\\"+selectpath);
					java.awt.Desktop.getDesktop().open(new java.io.File(sp));
				} catch (IOException e1) {
					// TODO 自动生成的 catch 块
					e1.printStackTrace();
				}
				
			}
		});
        JMenuItem openfile2=new JMenuItem("打开此文件所在文件夹");
        openfile2.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO 自动生成的方法存根
				try {
	
					int lastslash=selectpath.lastIndexOf("\\");
					String sp=new String("e:\\"+selectpath.substring(0, lastslash));
					java.awt.Desktop.getDesktop().open(new java.io.File(sp));
				} catch (IOException e1) {
					// TODO 自动生成的 catch 块
					e1.printStackTrace();
				}
				
			}
		});
        popmenu.add(openfile1);
        popmenu.add(openfile2);
    }


}
