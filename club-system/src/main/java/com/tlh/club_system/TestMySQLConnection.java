package com.tlh.club_system;

import java.sql.*;

public class TestMySQLConnection {
    public static void main(String[] args) {
        String[] urls = {
                "jdbc:mysql://localhost:3306/club_system_db?serverTimezone=Asia/Shanghai",
                "jdbc:mysql://localhost:3306/club_system_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai",
                "jdbc:mysql://localhost:3306/club_system_db"
        };

        String user = "root";
        String password = "0000";

        for (String url : urls) {
            System.out.println("\n尝试连接: " + url);
            try {
                Connection conn = DriverManager.getConnection(url, user, password);
                System.out.println("✓ 连接成功!");

                // 测试查询
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT DATABASE() as db, USER() as user");
                if (rs.next()) {
                    System.out.println("当前数据库: " + rs.getString("db"));
                    System.out.println("当前用户: " + rs.getString("user"));
                }

                conn.close();
            } catch (SQLException e) {
                System.out.println("✗ 连接失败: " + e.getMessage());
                System.out.println("错误代码: " + e.getErrorCode());
                System.out.println("SQL状态: " + e.getSQLState());
            }
        }
    }
}