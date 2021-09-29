package com.company;

import java.io.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.sql.Date;
import java.sql.Time;



public class todolistform extends JFrame {

    Connection con;
    int jobId;
    private JPanel mainFrame;
    private JTabbedPane tabbedPane1;
    private JPanel createjobpanel;
    private JTextArea textArea1;
    private JButton SAVEButton;
    private JTable table1;
    private JTextArea textArea2;
    private JComboBox comboBox1;
    private JButton UPDATEButton;
    private JButton DELETEButton;
    private JLabel jobidlabel;
    private JLabel datelabel;
    private JLabel timelabel;
    private JLabel headlabel;
    private JPanel modifypane;

    public void getMySqlConnection(){

        try{
            con=DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/db1","root","Mkaussie2003$");
            System.out.println("Connected database successfully...");
        }catch(SQLException e){
            System.out.println("SQL Exception: "+e.getMessage());
        }
    }

    public void fetchJobs(){
        //Must run when view jobs tab is selected
        //fetch jobs and display on Jtable
        Object [][]data=null;
        try{
            PreparedStatement ps=con.prepareStatement("select * from jobs");
            int rowCount=0;
            ResultSet rs=ps.executeQuery();
            while(rs.next()){
                rowCount++;
            }
            data=new Object[rowCount][5];
            rowCount=0;
            rs=ps.executeQuery();
            while(rs.next()){
                data[rowCount][0]=rs.getInt("jobid");
                data[rowCount][1]=rs.getDate("date");
                data[rowCount][2]=rs.getTime("time");
                data[rowCount][3]=rs.getString("job");
                data[rowCount][4]=rs.getString("status");
                rowCount++;
            }
        }catch (SQLException e){
            System.out.println("SQL exception in select");
        }
        String []colNames={"JOB ID","DATE","TIME","JOB","STATUS"};
        createTable(data,colNames);
    }

    public todolistform(){
        registerListeners();
        getMySqlConnection();

        SAVEButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String jobText=textArea1.getText();
                PreparedStatement ps=null;
                if(jobText.length()!=0) {
                    /*Calendar cal=Calendar.getInstance();
                    SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd");
                    String date= formatter.format(cal.getTime());
                    formatter=new SimpleDateFormat("HH:mm:ss");
                    String time= formatter.format(cal.getTime());*/
                    long now=System.currentTimeMillis();
                    Date date=new Date(now);
                    Time time=new Time(now);
                    try {
                        ps = con.prepareStatement("insert into jobs(date,time,job,status)values(?,?,?,?)");
                        System.out.println("Inserting records into the table...");
                        ps.setDate(1,date);
                        ps.setTime(2,time);
                        ps.setString(3,jobText);
                        ps.setString(4,"pending");
                        ps.execute();
                        System.out.println("Data inserted successfully");
                    } catch (Exception a) {
                        System.out.println("Exception in insertion: "+a.getMessage());
                    }
                    textArea1.setText("");
                }

            }
        });

        UPDATEButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    PreparedStatement ps=con.prepareStatement("update jobs set status=? where jobid=?");
                    int index;
                    index=comboBox1.getSelectedIndex();
                   // ps.setInt(1,comboBox1.getSelectedIndex());
                    if(index==0){
                        ps.setString(1,"pending");
                    }else if(index==1){
                        ps.setString(1,"done");
                    }else{
                        ps.setString(1,"aborted");
                    }
                    ps.setInt(2,jobId);
                    ps.execute();
                }catch (SQLException m){

                }
            }
        });
        DELETEButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    PreparedStatement ps=con.prepareStatement("delete from jobs where jobid=?");
                    ps.setInt(1,jobId);
                    ps.execute();
                }catch (SQLException n){

                }
            }
        });
    }
    public void registerListeners(){
        tabbedPane1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int selectedIndex=tabbedPane1.getSelectedIndex();
                if(selectedIndex==1){
                    fetchJobs();
                }
            }
        });
        table1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount()==2){
                    JTable target=(JTable)e.getSource();
                    int row=target.getSelectedRow();
                    jobId=Integer.parseInt(table1.getValueAt(row,0).toString());
                    loadData();
                }
            }
        });
    }
    public void loadData(){

        try{
            PreparedStatement ps=con.prepareStatement("SELECT * from jobs where jobid=?");
            ps.setInt(1,jobId);
            ResultSet rs=ps.executeQuery();
            if(rs.next()){
                jobidlabel.setText("Job ID: "+jobId);
                datelabel.setText("Date: "+rs.getDate("date"));
                timelabel.setText("Time: "+rs.getTime("time"));
                textArea2.setText(rs.getString("job"));
                int itemIndex=0;
                String status=rs.getString("status");
                if(status.equals("pending"))
                    itemIndex=0;
                else if(status.equals("done"))
                    itemIndex=1;
                else
                    itemIndex=2;
                comboBox1.setSelectedIndex(itemIndex);
            }
        }catch(SQLException e){
            System.out.println("SQL Exception in SELECT");
        }
    }

    private void createTable(Object [][]data,String []colNames){
        DefaultTableModel dtm=new DefaultTableModel(data,colNames){

            public boolean isCellEditable(int row,int column){
                return false;
            }
        };
        table1.setModel(dtm);
    }

    public static void main(String[] args) {
        todolistform j=new todolistform();
        j.setContentPane(new todolistform().mainFrame);
        j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        j.setVisible(true);
        j.pack();
    }
}
