import org.junit.Test;

import java.lang.annotation.Target;
import java.util.Optional;

public class TTT {
    @Test
    public void test() {
        System.out.println(test2());
    }

    public boolean test2() {
        Object b = "dd";
        return Optional.ofNullable(b)
                .map(o -> {

                    Object a = null;
                    return (boolean)a;
                })
                .orElse(false);
    }
}
