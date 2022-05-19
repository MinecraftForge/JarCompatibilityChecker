package net.minecraftforge.jarcompatibilitychecker.test;

import org.junit.Test;

import java.nio.file.Path;

public class InterfaceTests extends BaseCompatibilityTest {
    @Override
    protected Path getRoot() {
        return super.getRoot().resolve("Interface");
    }

    @Test
    public void testInterfaceOrder() {
        assertCompatible(true, "InterfaceOrder", "A");
    }
}
