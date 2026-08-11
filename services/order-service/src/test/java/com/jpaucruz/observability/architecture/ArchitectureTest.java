package com.jpaucruz.observability.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(
        packages = "com.jpaucruz.observability",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class ArchitectureTest {

    @ArchTest
    static final ArchRule domain_should_not_depend_on_application_or_infrastructure =
            noClasses()
                    .that()
                    .resideInAPackage("..domain..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "..application..",
                            "..infrastructure.."
                    )
                    .because(
                            "the domain must remain independent from application and infrastructure"
                    );

    @ArchTest
    static final ArchRule application_should_not_depend_on_infrastructure =
            noClasses()
                    .that()
                    .resideInAPackage("..application..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..infrastructure..")
                    .because(
                            "the application core must not know its infrastructure adapters"
                    );

    @ArchTest
    static final ArchRule core_should_not_depend_on_frameworks =
            noClasses()
                    .that()
                    .resideInAnyPackage(
                            "..domain..",
                            "..application.."
                    )
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework..",
                            "jakarta.persistence..",
                            "jakarta.validation..",
                            "org.mapstruct..",
                            "com.fasterxml.jackson.."
                    )
                    .because(
                            "the domain and application layers must remain framework independent"
                    );

    @ArchTest
    static final ArchRule inbound_adapters_should_not_access_outbound_adapters =
            noClasses()
                    .that()
                    .resideInAPackage("..infrastructure.adapter.in..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..infrastructure.adapter.out..")
                    .because(
                            "inbound adapters must access persistence through application ports"
                    );

    @ArchTest
    static final ArchRule top_level_packages_should_be_free_of_cycles =
            slices()
                    .matching("com.jpaucruz.observability.(*)..")
                    .should()
                    .beFreeOfCycles()
                    .because(
                            "top-level architectural packages must not form dependency cycles"
                    );

    @ArchTest
    static final ArchRule input_ports_should_follow_the_project_convention =
            classes()
                    .that()
                    .resideInAPackage("..application.port.in")
                    .should()
                    .beInterfaces()
                    .andShould()
                    .haveSimpleNameEndingWith("UseCase")
                    .because("input ports represent capabilities offered by the application");

    @ArchTest
    static final ArchRule output_ports_should_follow_the_project_convention =
            classes()
                    .that()
                    .resideInAPackage("..application.port.out")
                    .should()
                    .beInterfaces()
                    .andShould()
                    .haveSimpleNameEndingWith("Port")
                    .because("output ports represent dependencies required by the application")
                    .allowEmptyShould(true);

}
