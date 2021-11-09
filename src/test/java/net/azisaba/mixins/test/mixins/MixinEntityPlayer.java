package net.azisaba.mixins.test.mixins;

import net.azisaba.mixins.ConstructorCall;
import net.azisaba.mixins.test.TestMethods;
import net.azisaba.mixins.test.base.EntityPlayer;
import net.azisaba.mixins.At;
import net.azisaba.mixins.Mixin;
import net.azisaba.mixins.injection.CallbackInfo;
import net.azisaba.mixins.injection.Inject;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityPlayer {
    @Inject(at = {At.HEAD, At.TAIL})
    public void setHealth(float health, CallbackInfo ci) {
        TestMethods.calledMixinEntityPlayer_setHealth++;
    }

    @Inject(at = At.HEAD)
    @ConstructorCall
    public void init() {
        TestMethods.calledMixinEntityPlayer_init++;
    }
}
