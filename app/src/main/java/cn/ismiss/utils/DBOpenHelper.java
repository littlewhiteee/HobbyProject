package cn.ismiss.utils;


import com.mysql.jdbc.Connection;

import java.sql.DriverManager;
import java.sql.SQLException;

public class DBOpenHelper {
   public static String diver = "com.mysql.jdbc.Driver";
   //加入utf-8是为了后面往表中输入中文，表中不会出现乱码的情况
   public static String url = "jdbc:mysql://47.100.206.185:3306/yyy?characterEncoding=utf-8";
    public static String user = "yyy";//用户名
    public static String password = "dbq820";//密码
    /*
    * 连接数据库
    * */
   public static Connection getConn(){
       Connection conn = null;
       try {
           Class.forName(diver);
           conn = (Connection) DriverManager.getConnection(url,user,password);//获取连接
       } catch (ClassNotFoundException e) {
           e.printStackTrace();
       } catch (SQLException e) {
           e.printStackTrace();
       }
       return conn;
   }
}