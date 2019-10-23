import org.junit.Test;

import java.util.UUID;

public class UuidTest {
    @Test
    public void uuid() {
        UUID uuid = UUID.randomUUID();

        System.out.println(uuid);
    }
}
