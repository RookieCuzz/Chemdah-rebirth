import io.izzel.taboolib.gradle.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    `maven-publish`
    id("io.izzel.taboolib") version "2.0.22" // 最低要求
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.dokka") version "1.6.0"
}

taboolib {
    description {
        contributors {
            name("坏黑")
        }
        dependencies {
            name("Adyeshach")
            name("MythicMobs")
            name("BetterHud")
//            name("ItemsAdder")
//            name("RookieCooking")
        }
    }
    env {
        // ...
//        install(NMS_UTIL, UI,NAVIGATION,METRICS,LANG,KETHER,EFFECT, LANG,AI)
//        install(EXPANSION_COMMAND_HELPER, EXPANSION_JAVASCRIPT)
//        install(BUKKIT_ALL)
//        install(UNIVERSAL)
//        install(CHAT)
//        install(DATABASE)
            install(BukkitNMSUtil, BukkitUI, BukkitNavigation, XSeries,Metrics, I18n, Kether, MinecraftEffect,
                BukkitNMSEntityAI
            )
            install(Bukkit, BukkitUtil,Basic)
            install(CommandHelper,JavaScript)
            install(BukkitHook)
            install(MinecraftChat)
            install(Database)

    }
    version { taboolib = "6.2.3-20d868d" }
    relocate("ink.ptms.um", "ink.ptms.chemdah.um")
}

repositories {
    maven { url = uri("https://repo.spongepowered.org/maven") }
    maven { url = uri("https://jitpack.io") }
    mavenLocal()
    mavenCentral()
}

dependencies {
    // adyeshach
    compileOnly("ink.ptms.adyeshach:all:2.0.0-snapshot-4"   )
    taboo("ink.ptms:um:1.1.5")
    // server
    compileOnly("ink.ptms.core:v11604:11604")
    compileOnly("ink.ptms:nms-all:1.0.0")
    compileOnly("com.google.code.gson:gson:2.8.5")
    compileOnly("com.google.guava:guava:21.0")
    compileOnly("ink.ptms:error_reporter:1.0.0")
    compileOnly("net.milkbowl.vault:Vault:1")
    compileOnly("org.serverct.ersha.dungeon:DungeonPlus:1.1.3")
//    compileOnly("com.github.angeschossen:LandsAPI:5.13.0")
//    compileOnly("at.pcgamingfreaks:MarriageMaster-API-Bukkit:2.4")
    compileOnly("me.badbones69:crazycrates-plugin:1.10")
    compileOnly("com.sk89q.worldedit:WorldEdit:7")
    compileOnly("public:FriendsAPI:1.1.0.9.1")
    compileOnly("public:QuickShop:4.0.9.1")
    compileOnly("public:nuvotifier:1.0.0")
    compileOnly("public:Jobs:1.0.0")
    implementation(files("${project.rootDir}/libs/BetterHud-bukkit-1.11.4.jar"))
    implementation(files("${project.rootDir}/libs/RookieBlackSmith-1.0-SNAPSHOT.jar"))
    implementation(files("${project.rootDir}/libs/CustomCrops-3.3.1.10.jar"))
    implementation(files("${project.rootDir}/libs/ItemsAdder_3.5.0c (1).jar"))
    implementation(files("${project.rootDir}/libs/RookieFreeRes-1.0-SNAPSHOT.jar"))
    implementation(files("${project.rootDir}/libs/RookieCooking-1.0-SNAPSHOT.jar"))
    implementation(files("${project.rootDir}/libs/RookieCropShop-1.0-SNAPSHOT.jar"))
    implementation(files("${project.rootDir}/libs/MMOItems-6.10-SNAPSHOT.jar"))
    implementation(files("${project.rootDir}/libs/MythicLib-1.6.1 (1).jar"))
    implementation(files("${project.rootDir}/libs/MythicMobs-5.8.0-SNAPSHOT.jar"))
    compileOnly("public:even-more-fish:1.0.0")
    compileOnly("public:ChatReaction:1.0.0")
    compileOnly("public:Team:1.0.0:7")
    compileOnly("public:Team:1.0.0:9")
    compileOnly("public:Team:1.0.0:10")
    compileOnly("public:Team2:1.0.0:1")
    compileOnly("public:Team2:1.0.0:2")
    compileOnly("public:Team2:1.0.0:3")
    compileOnly("public:Team3:1.0.0:1")
    compileOnly("public:Team3:1.0.0:2")
    compileOnly("public:Team3:1.0.0:3")
    compileOnly("public:CustomGo:1.0.0")
    compileOnly("public:Skript:1.0.0")
    compileOnly("public:SkillAPI:s1.98")
    //compileOnly("com.promcteam:proskillapi:1.1.8")
    //compileOnly("com.promcteam:promccore:1.0.4")
    compileOnly("public:mcMMO:1.0.0")
    compileOnly("public:MMOLib:1.0.0")
    compileOnly("public:MMOCore:1.10.2")
    compileOnly("public:MMOItems:1.0.0")
    compileOnly("public:Parties:1.0.0")
    compileOnly("public:NexEngine:1.0.0")
    compileOnly("public:QuantumRPG:1.0.0")
    compileOnly("public:JulyItems:1.0.0")
    compileOnly("public:RPGItems:1.0.0")
    compileOnly("public:Citizens:1.0.0")
//    compileOnly("public:MythicMobs:1.0.1")
//    compileOnly("public:MythicMobs5:5.0.4")
//    implementation("public:MythicLib:1.0.0")
//    compileOnly("public:MythicMobs:1.0.1")
//    compileOnly("public:MythicMobs5:5.7.0")
    compileOnly("public:ExecutableItems:1.0.0")
    compileOnly("public:Brewery:1.0.0")
    // compileOnly("ink.ptms:Blockdb:1.1.0")
    compileOnly("ink.ptms:Zaphkiel:1.6.0")
    // compileOnly("ink.ptms:Adyeshach:1.5.13-op16")
    compileOnly("ink.ptms:Sandalphon:1.3.0")
    compileOnly("ink.ptms.core:v12101:12101-minimize:mapped")
    compileOnly("ink.ptms.core:v11904:11904:mapped")
    compileOnly("ink.ptms.core:v11400:11400")
    compileOnly("ink.ptms:nms-all:1.0.0")
    implementation(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.6.0")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

publishing {
    repositories {
        maven {
            url = uri("https://repo.tabooproject.org/repository/releases")
            credentials {
                username = project.findProperty("taboolibUsername").toString()
                password = project.findProperty("taboolibPassword").toString()
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("library") {
            from(components["java"])
            groupId = "ink.ptms"
        }
    }
}
