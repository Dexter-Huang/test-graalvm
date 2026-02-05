package org.example.testgraalvm.panama;

import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeForeignAccess;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.ValueLayout;

/**
 * GraalVM Native Image Feature 用于注册 Panama FFM downcall 签名
 */
public class PanamaFeature implements Feature {

    @Override
    public void duringSetup(DuringSetupAccess access) {
        // 注册 Convert 函数签名: (ADDRESS) -> ADDRESS
        RuntimeForeignAccess.registerForDowncall(
            FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
        );

        // 注册 ConvertList/ConvertListAsync 函数签名: (ADDRESS) -> ADDRESS
        // 与 Convert 相同，无需重复注册

        // 注册 FreeCStr 函数签名: (ADDRESS) -> void
        RuntimeForeignAccess.registerForDowncall(
            FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );
    }

    @Override
    public String getDescription() {
        return "Register Panama FFM downcall signatures for MathType2Latex native library";
    }
}
