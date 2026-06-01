package com.supplier.test.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * 架构规范测试（ArchUnit）
 * <p>
 * 验证后端代码是否符合分层架构规范。规则经实际项目情况调整：
 * - 排除匿名内部类（$1/$2等）
 * - event 包中的消费者允许跨模块依赖（事件桥接）
 * - BaseEntity 作为通用基类允许 Entity 后缀
 */
@DisplayName("架构规范测试")
class ArchitectureTest {

    private static final String BASE_PACKAGE = "com.supplier";
    private static JavaClasses classes;

    /** 排除匿名内部类（类名含 $） */
    private static final DescribedPredicate<JavaClass> NOT_ANONYMOUS =
            DescribedPredicate.describe("不是匿名内部类", clazz -> !clazz.getName().contains("$"));

    /** 排除事件消费者（允许跨模块依赖） */
    private static final DescribedPredicate<JavaClass> NOT_EVENT_CONSUMER =
            DescribedPredicate.describe("不是事件消费者",
                    clazz -> !clazz.getPackageName().contains(".common.event"));

    @BeforeAll
    static void loadClasses() {
        classes = new ClassFileImporter()
                .withImportOption(location -> !location.contains("test"))
                .importPackages(BASE_PACKAGE);
    }

    @Nested
    @DisplayName("分层架构")
    class LayeredArchitectureTests {

        @Test
        @DisplayName("Controller 不应直接访问 Mapper")
        void controllers_shouldNotAccessMappers() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("..controller..")
                    .and(NOT_ANONYMOUS)
                    .should().accessClassesThat().resideInAPackage("..mapper..")
                    .because("Controller 不应直接访问 Mapper，必须通过 Service 层");
            rule.check(classes);
        }

        @Test
        @DisplayName("Mapper 只应被 Service 或同级 Mapper 访问")
        void mappers_shouldOnlyBeAccessedByServices() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..mapper..")
                    .and(NOT_ANONYMOUS)
                    .should().onlyBeAccessed().byAnyPackage(
                            "..service..", "..mapper..", "..common..");
            rule.check(classes);
        }

        @Test
        @DisplayName("Service 不应依赖 Controller")
        void services_shouldNotDependOnControllers() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("..service..")
                    .and(NOT_ANONYMOUS)
                    .should().dependOnClassesThat().resideInAPackage("..controller..")
                    .because("Service 层不应反向依赖 Controller 层");
            rule.check(classes);
        }
    }

    @Nested
    @DisplayName("命名规范（排除匿名内部类）")
    class NamingConventionTests {

        @Test
        @DisplayName("Controller 类名应以 Controller 结尾")
        void controllerNames_shouldEndWithController() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..controller..")
                    .and(NOT_ANONYMOUS)
                    .should().haveSimpleNameEndingWith("Controller");
            rule.check(classes);
        }

        @Test
        @DisplayName("Service 实现类名应以 ServiceImpl 结尾")
        void serviceImplNames_shouldEndWithServiceImpl() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..service.impl..")
                    .and(NOT_ANONYMOUS)
                    .should().haveSimpleNameEndingWith("ServiceImpl");
            rule.check(classes);
        }

        @Test
        @DisplayName("Mapper 类名应以 Mapper 结尾")
        void mapperNames_shouldEndWithMapper() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..mapper..")
                    .and(NOT_ANONYMOUS)
                    .should().haveSimpleNameEndingWith("Mapper");
            rule.check(classes);
        }

        @Test
        @DisplayName("Entity 类名不应有 Entity 后缀（BaseEntity 除外）")
        void entityNames_shouldNotHaveEntitySuffix() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("..entity..")
                    .and(NOT_ANONYMOUS)
                    .and(DescribedPredicate.describe(
                            "不是 BaseEntity", clazz -> !"BaseEntity".equals(clazz.getSimpleName())))
                    .should().haveSimpleNameEndingWith("Entity")
                    .because("业务 Entity 类不应带 Entity 后缀（BaseEntity 是通用基类除外）");
            rule.check(classes);
        }
    }

    @Nested
    @DisplayName("注解规范（排除匿名内部类）")
    class AnnotationTests {

        @Test
        @DisplayName("所有 Controller 必须有 @RestController 或 @Controller")
        void allControllers_shouldHaveControllerAnnotation() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..controller..")
                    .and(NOT_ANONYMOUS)
                    .should().beAnnotatedWith(
                            org.springframework.web.bind.annotation.RestController.class)
                    .orShould().beAnnotatedWith(
                            org.springframework.stereotype.Controller.class);
            rule.check(classes);
        }

        @Test
        @DisplayName("所有 Service 实现类必须有 @Service")
        void allServiceImpls_shouldHaveServiceAnnotation() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..service.impl..")
                    .and(NOT_ANONYMOUS)
                    .should().beAnnotatedWith(
                            org.springframework.stereotype.Service.class);
            rule.check(classes);
        }
    }

    @Nested
    @DisplayName("公共模块隔离（排除事件消费者）")
    class CommonModuleTests {

        @Test
        @DisplayName("common 包不应依赖业务模块（事件消费者除外）")
        void commonPackage_shouldNotDependOnBusinessModules() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(BASE_PACKAGE + ".common..")
                    .and(NOT_ANONYMOUS)
                    .and(NOT_EVENT_CONSUMER)
                    .should().dependOnClassesThat().resideInAnyPackage(
                            BASE_PACKAGE + ".order..",
                            BASE_PACKAGE + ".logistics..",
                            BASE_PACKAGE + ".quality..",
                            BASE_PACKAGE + ".settlement..",
                            BASE_PACKAGE + ".sourcing..",
                            BASE_PACKAGE + ".integration..",
                            BASE_PACKAGE + ".portal..",
                            BASE_PACKAGE + ".message..")
                    .because("common 模块（事件消费者除外）不应依赖任何业务模块");
            rule.check(classes);
        }
    }
}