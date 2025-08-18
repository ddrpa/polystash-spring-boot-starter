package cc.ddrpa.dorian.polystash.utils.filesystem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledOnOs(OS.MAC)
class XATTRHandlerForMacOSTests extends AbstractAttributeHandlerTests {

    private final XATTRHandler xattrHandler = new XATTRHandler();

    @Test
    void isSupportedTest() {
        assertTrue(XATTRHandler.support(Path.of("Awa-Subaru.png")));
    }

    @Override
    protected IAttributeHandler getAttributeHandler() {
        return xattrHandler;
    }
}