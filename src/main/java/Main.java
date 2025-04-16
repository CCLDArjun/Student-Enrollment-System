import java.sql.*;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/mydb";
        String user = "root";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connected to database!");

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT NOW();");

            while (rs.next()) {
                System.out.println("Current Time: " + rs.getString(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
