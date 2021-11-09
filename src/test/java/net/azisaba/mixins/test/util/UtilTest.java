package net.azisaba.mixins.test.util;

import net.azisaba.mixins.util.Util;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

public class UtilTest {
    @Test
    public void bytecodeSignatureTest() throws ReflectiveOperationException {
        Method method = UtilTest.class.getDeclaredMethod("test", int[][][][][].class, String.class, byte[].class, String[].class);
        assert Util.toBytecodeSignature(method).equals("([[[[[ILjava/lang/String;[B[Ljava/lang/String;)V");
    }

    @SuppressWarnings("unused")
    private static void test(int[][][][][] array, String type, byte[] byteArray, String[] typeArray) {
    }
}
