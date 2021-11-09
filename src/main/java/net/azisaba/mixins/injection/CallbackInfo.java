package net.azisaba.mixins.injection;

import org.jetbrains.annotations.Nullable;

public class CallbackInfo {
    public static final Object NULL = new Object();

    private Object returnValue = null;

    public void cancel() {
        this.returnValue = NULL;
    }

    public void returns(@Nullable Object o) {
        if (o == null) {
            this.returnValue = NULL;
        } else {
            this.returnValue = o;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getReturnValue() {
        if (returnValue == NULL) return null;
        return (T) returnValue;
    }

    public void dontCancel() {
        this.returnValue = null;
    }

    public boolean isCancelled() {
        return returnValue != null;
    }
}
