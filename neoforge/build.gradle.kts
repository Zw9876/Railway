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

architectury.neoForge()

// Skeleton mode (-Pskeleton): common contributes no sources, so we register no
// mixin configs and use a minimal entrypoint. Phase 2 re-enables the real content.
val skeletonMode = project.hasProperty("skeleton")

loom {
    val common = project(":common")
    accessWidenerPath = common.loom.accessWidenerPath

    runs.configureEach {
        // force proper color logs
        vmArg("-Dterminal.jline=true")
    }
}

dependencies {
    "neoForge"("net.neoforged:neoforge:${"neoforge_version"()}")

    // Create and its dependencies
    modImplementation("com.simibubi.create:create-${"minecraft_version"()}:${"create_neoforge_version"()}:slim") { isTransitive = false }
    modImplementation("net.createmod.ponder:ponder-neoforge:${"ponder_version"()}")
    modImplementation("net.createmod.catnip:Catnip-NeoForge-${"minecraft_version"()}:${"catnip_version"()}") { isTransitive = false }
    modImplementation("com.tterrag.registrate:Registrate:${"registrate_version"()}")
    modCompileOnly("dev.engine-room.flywheel:flywheel-neoforge-api-${"minecraft_version"()}:${"flywheel_version"()}")
    modRuntimeOnly("dev.engine-room.flywheel:flywheel-neoforge-${"minecraft_version"()}:${"flywheel_version"()}")

    // NeoForge 21.1 bundles MixinExtras at runtime; we only need it at compile time.
    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:${"mixin_extras_version"()}")!!)!!
}

operator fun String.invoke(): String {
    return rootProject.ext[this] as? String
        ?: throw IllegalStateException("Property $this is not defined")
}
