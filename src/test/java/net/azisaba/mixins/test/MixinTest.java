package net.azisaba.mixins.test;

import net.azisaba.mixins.Mixins;
import net.azisaba.mixins.configuration.MixinConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class MixinTest {
    @Test
    @BeforeAll
    public static void init() {
        Mixins.setup();
        Mixins.addConfiguration(
                new MixinConfiguration(
                        "net.azisaba.mixins.test.mixins",
                        Arrays.asList("MixinEntityPlayer")
                )
        );
    }

    @Test
    public void testEntityPlayer() {
        TestMethods.testEntityPlayer();
    }
}
