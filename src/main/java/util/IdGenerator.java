package util;

import org.springframework.stereotype.Component;

@Component
public class IdGenerator {
    private Integer counter = 0;
    private static final String prefix = "AD-";

    public String nextId() {
        counter++;
        return prefix + counter;
    }
}
