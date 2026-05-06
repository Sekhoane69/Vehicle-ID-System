import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DbTest {
    public static void main(String[] args) {
        String DB_URL = "jdbc:postgresql://localhost:5432/Car_identificationdb";
        String DB_Person = "postgres";
        String DB_PASS = "123456";
        
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_Person, DB_PASS);
            System.out.println("Connected to DB!");
            
            String sql = "SELECT personname, password, email FROM Persons";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println("Person: " + rs.getString("personname") + 
                                   " | Pass: " + rs.getString("password") + 
                                   " | Email: " + rs.getString("email"));
            }
            if (count == 0) {
                System.out.println("No Persons found in the database. Did you run tables.sql?");
            }
            
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
