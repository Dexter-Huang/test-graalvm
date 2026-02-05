package org.example.testgraalvm.panama;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

/**
 * Spring Boot AOT Runtime Hints 配置
 * 用于 GraalVM Native Image 编译时注册反射和资源
 */
@Configuration
@ImportRuntimeHints(PanamaRuntimeHints.PanamaHintsRegistrar.class)
public class PanamaRuntimeHints {

    static class PanamaHintsRegistrar implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // 注册 Panama 相关类的反射
            hints.reflection()
                .registerType(Mtef2LatexDTO.class, 
                    MemberCategory.ACCESS_PUBLIC_FIELDS,
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_METHODS)
                .registerType(MathType2LatexLib.class,
                    MemberCategory.ACCESS_PUBLIC_FIELDS,
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_METHODS)
                .registerType(MathTypeToLatexUtil.class,
                    MemberCategory.ACCESS_PUBLIC_FIELDS,
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_METHODS);

            // 注册本地库资源（如果从 classpath 加载）
            hints.resources()
                .registerPattern("bin/*");
        }
    }
}
