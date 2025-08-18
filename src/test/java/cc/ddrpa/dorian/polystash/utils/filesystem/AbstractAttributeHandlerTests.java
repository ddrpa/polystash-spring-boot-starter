package cc.ddrpa.dorian.polystash.utils.filesystem;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class AbstractAttributeHandlerTests {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractAttributeHandlerTests.class);

    protected abstract IAttributeHandler getAttributeHandler();

    @Test
    void writeAndReadAttributeTest() {
        getAttributeHandler().userDefinedAttributes(Path.of("Awa-Subaru.png"),
                Map.of("last-m", "2024-12-31 11:10:00"));
        Map<String, String> userDefined = getAttributeHandler().userDefinedAttributes(Path.of("Awa-Subaru.png"));
        assertEquals("2024-12-31 11:10:00", userDefined.get("last-m"));
    }
}