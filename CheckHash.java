
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class CheckHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String raw = "demo-go-secret";
        String hash = "$2a$10$PcQUEoZL7XcqCSN7YHWuZeQzqLyRXWVOLNHnUHIJywl3FY3qqoX.G";
        boolean matches = encoder.matches(raw, hash);
        System.out.println("Matches: " + matches);
    }
}
