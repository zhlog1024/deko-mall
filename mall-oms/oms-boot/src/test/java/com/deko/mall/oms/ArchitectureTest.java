package com.deko.mall.oms;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

/**
 * 架构约束（ArchUnit），随 {@code mvn test} 执行。
 */
@AnalyzeClasses(packages = "com.deko.mall.oms", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    /** Controller 必须落在 ..controller.. 包下，避免 Web 层散落到 service/util。 */
    @ArchTest
    static final ArchRule controllers_should_reside_in_controller_package =
            classes().that().haveSimpleNameEndingWith("Controller")
                    .should().resideInAPackage("..controller..")
                    .because("Web 入口统一放 controller 包，便于 API 前缀与权限审计");

    /** 标注 @RestController 的类同样必须在 controller 包（防漏网之鱼）。 */
    @ArchTest
    static final ArchRule rest_controllers_should_reside_in_controller_package =
            classes().that().areAnnotatedWith(RestController.class)
                    .should().resideInAPackage("..controller..")
                    .because("三端 API 前缀审计以 controller 包为范围");

    /** Controller 不得直接依赖 mapper：必须经 service，否则事务/缓存/权限全部失效。 */
    @ArchTest
    static final ArchRule controllers_should_not_depend_on_mappers =
            noClasses().that().resideInAPackage("..controller..")
                    .should().dependOnClassesThat().resideInAPackage("..mapper..")
                    .because("数据访问必须经 service 层，controller 直连 mapper 会绕过事务与缓存");

    /** service 不得反向依赖 controller，防止分层倒置与循环依赖。 */
    @ArchTest
    static final ArchRule services_should_not_depend_on_controllers =
            noClasses().that().resideInAPackage("..service..")
                    .should().dependOnClassesThat().resideInAPackage("..controller..")
                    .because("分层方向只能是 controller → service → mapper");

    /** Controller 方法不得直接返回 Map，必须用 Result/PageResult + VO 包装。 */
    @ArchTest
    static final ArchRule controller_methods_should_not_return_raw_map =
            noMethods().that().areDeclaredInClassesThat().resideInAPackage("..controller..")
                    .should().haveRawReturnType(Map.class)
                    .because("响应统一 Result<T> 包装，禁止 Map<String,Object> 传业务数据");
}
