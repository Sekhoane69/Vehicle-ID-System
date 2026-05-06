import org.mindrot.jbcrypt.BCrypt;

public class HashTest {
    public static void main(String[] args) {
        String hash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh2y";
        String pass = "admin123";
        boolean match = BCrypt.checkpw(pass, hash);
        System.out.println("Match: " + match);
        
        System.out.println("New hash: " + BCrypt.hashpw(pass, BCrypt.gensalt()));
    }
}
