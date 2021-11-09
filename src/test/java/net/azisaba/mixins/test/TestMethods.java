package net.azisaba.mixins.test;

import net.azisaba.mixins.test.base.EntityPlayer;

public class TestMethods {
    public static int calledMixinEntityPlayer_setHealth = 0;
    public static int calledMixinEntityPlayer_init = 0;

    public static void testEntityPlayer() {
        EntityPlayer ep = new EntityPlayer() {};
        ep.setHealth(5.0f);
        assert calledMixinEntityPlayer_init == 1;
        assert calledMixinEntityPlayer_setHealth == 2;
    }
}
