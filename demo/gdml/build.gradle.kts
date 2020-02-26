import org.openjfx.gradle.JavaFXOptions

plugins {
    id("scientifik.mpp")
    id("org.openjfx.javafxplugin")
    id("application")
}

kotlin {

    jvm {
        withJava()
    }

    js {
        browser {
            webpackTask {
                //sourceMaps = false
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":dataforge-vis-spatial"))
                api(project(":dataforge-vis-spatial-gdml"))
            }
        }
        jvmMain{
            dependencies {
                api("org.fxyz3d:fxyz3d:0.5.2")
            }
        }
    }
}

application {
    mainClassName = "hep.dataforge.vis.spatial.gdml.demo.GDMLDemoAppKt"
}

configure<JavaFXOptions> {
    modules("javafx.controls")
}