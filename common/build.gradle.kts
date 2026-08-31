/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2025 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

// Skeleton mode (-Pskeleton): compile no common sources, so the NeoForge module's
// build + runtime plumbing can be validated before common is ported to 1.21.1.
val skeletonMode = project.hasProperty("skeleton")

loom {
    // Skeleton mode uses an empty widener: the real one targets 1.20.1 members
    // (e.g. AbstractMinecart.getDropItem) that no longer exist in 1.21.1. Phase 2 fixes it.
    accessWidenerPath = if (skeletonMode) file("src/main/resources/railways-skeleton.accesswidener")
                        else file("src/main/resources/railways.accesswidener")
}

architectury {
    common {
        for(p in rootProject.subprojects) {
            if(p != project) {
                this@common.add(p.name)
            }
        }
    }
}

dependencies {
    // We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
    // Do NOT use other classes from fabric loader
    modImplementation("net.fabricmc:fabric-loader:${"fabric_loader_version"()}")

    // Compile against Create NeoForge in common.
    // Create Fabric has no 1.21.1 build, so unlike the 1.20.1 tree this compiles
    // against the NeoForge artifacts directly. This is a NeoForge-only source tree.
    modCompileOnly("com.simibubi.create:create-${"minecraft_version"()}:${"create_neoforge_version"()}:slim") { isTransitive = false }
    modCompileOnly("net.createmod.ponder:ponder-neoforge:${"ponder_version"()}")
    // Catnip 0.8.54's POM references a nonexistent group (dev.engine_room), so resolve it non-transitively
    modCompileOnly("net.createmod.catnip:Catnip-NeoForge-${"minecraft_version"()}:${"catnip_version"()}") { isTransitive = false }
    modCompileOnly("com.tterrag.registrate:Registrate:${"registrate_version"()}")
    modCompileOnly("dev.engine-room.flywheel:flywheel-neoforge-api-${"minecraft_version"()}:${"flywheel_version"()}")

    modCompileOnly("de.maxhenkel.voicechat:voicechat-api:${"voicechat_api_version"()}")

    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:${"mixin_extras_version"()}")!!)
}

tasks.processResources {
    // must be part of primary mod to be findable
    exclude("resourcepacks/")

    // don't add development or to-do files into built jar
    exclude("**/*.bbmodel", "**/*.lnk", "**/*.xcf", "**/*.md", "**/*.txt", "**/*.blend", "**/*.blend1")
}

sourceSets.main {
    if (skeletonMode) {
        java.setSrcDirs(emptyList<String>())
        resources.setSrcDirs(emptyList<String>())
    } else {
        resources { // include generated resources in resources
            srcDir("src/generated/resources")
            exclude(".cache/**")
            exclude("assets/create/**")
        }
    }
    blossom.javaSources {
        property("version", "mod_version"())
        property("gitCommit", rootProject.extra["gitHash"].toString())
        property("includeDevCommands", rootProject.extra["includeDevCommands"].toString())
    }
}

operator fun String.invoke(): String {
    return rootProject.ext[this] as? String
        ?: throw IllegalStateException("Property $this is not defined")
}
