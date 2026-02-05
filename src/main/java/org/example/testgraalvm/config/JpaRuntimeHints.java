package org.example.testgraalvm.config;

import org.example.testgraalvm.entity.Demo;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

/**
 * GraalVM Native Image runtime hints for JPA/Hibernate with SQLite.
 */
@Configuration
@ImportRuntimeHints(JpaRuntimeHints.JpaHintsRegistrar.class)
public class JpaRuntimeHints {

    static class JpaHintsRegistrar implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // Register Demo entity
            hints.reflection().registerType(Demo.class,
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_METHODS,
                    MemberCategory.ACCESS_DECLARED_FIELDS);

            // Register SQLite Dialect
            hints.reflection().registerTypeIfPresent(classLoader,
                    "org.hibernate.community.dialect.SQLiteDialect",
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_METHODS,
                    MemberCategory.ACCESS_DECLARED_FIELDS);

            // Register SQLite Identity Column Support
            hints.reflection().registerTypeIfPresent(classLoader,
                    "org.hibernate.community.dialect.identity.SQLiteIdentityColumnSupport",
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_METHODS);

            // Register SQLite JDBC Driver
            hints.reflection().registerTypeIfPresent(classLoader,
                    "org.sqlite.JDBC",
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_METHODS);

            hints.reflection().registerTypeIfPresent(classLoader,
                    "org.sqlite.jdbc4.JDBC4Connection",
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_METHODS);
        }
    }
}
