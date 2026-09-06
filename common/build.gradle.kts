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
    // NeoForge on common's COMPILE classpath only.
    //
    // No file in common imports net.neoforged.* (only 2 of 657 reference any
    // loader package). The need is purely transitive: our classes extend Create
    // classes, and Create's extend NeoForge types (CopycatModel ->
    // BakedModelWrapper, etc.), so javac must see them to typecheck.
    //
    // This uses the plain `universal` artifact rather than loom's neoForge
    // configuration on purpose: setting loom.platform=neoforge here would make
    // common a platform project, which is incompatible with architectury's
    // common{} block ("SRG is not supported on NeoForge"). NeoForge 1.21 ships
    // Mojmap, matching our mappings, so no remapping is required.

    compileOnly("net.neoforged:neoforge:${"neoforge_version"()}:universal")
    // Requesting the `universal` classifier above bypasses POM dependency
    // resolution, so NeoForge's own libraries must be named explicitly.
    // night-config backs NeoForge's config system (hard errors in CRConfigs);
    // bus only silences "unknown enum constant EventPriority" warnings.
    compileOnly("com.electronwill.night-config:core:3.8.3")
    compileOnly("com.electronwill.night-config:toml:3.8.3")
    compileOnly("net.neoforged:bus:8.0.5")
    // net.neoforged.fml.config.ModConfig / IConfigSpec live in fancymodloader,
    // not in the neoforge universal jar.
    compileOnly("net.neoforged.fancymodloader:loader:4.0.42")
    // We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
    // Do NOT use other classes from fabric loader
    modImplementation("net.fabricmc:fabric-loader:${"fabric_loader_version"()}")

    // Compile against Create NeoForge in common.
    // Create Fabric has no 1.21.1 build, so unlike the 1.20.1 tree this compiles
    // against the NeoForge artifacts directly. This is a NeoForge-only source tree.
    modCompileOnly("com.simibubi.create:create-${"minecraft_version"()}:${"create_neoforge_version"()}:slim") { isTransitive = false }
    modCompileOnly("net.createmod.ponder:ponder-neoforge:${"ponder_version"()}")
    // NOTE: do NOT declare Catnip here. ponder-neoforge bundles 274 catnip classes,
    // and declaring both makes two JPMS modules export net.createmod.catnip.* ->
    // ResolutionException at runtime. Create's own POM depends on ponder, never catnip.
    modCompileOnly("com.tterrag.registrate:Registrate:${"registrate_version"()}")
    modCompileOnly("dev.engine-room.flywheel:flywheel-neoforge-api-${"minecraft_version"()}:${"flywheel_version"()}")

    modCompileOnly("de.maxhenkel.voicechat:voicechat-api:${"voicechat_api_version"()}")
    // The three voicechat mixins target internals (Server, ServerWorldUtils,
    // EntityAudioChannelImpl) that live in the mod jar, not the API jar.
    modCompileOnly("maven.modrinth:simple-voice-chat:neoforge-${"voicechat_version"()}")

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
