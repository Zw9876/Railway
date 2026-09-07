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

loom {
    val common = project(":common")
    accessWidenerPath = common.loom.accessWidenerPath

    runs {
        configureEach {
            // force proper color logs
            vmArg("-Dterminal.jline=true")
        }

        // Datagen. The output lives in :common because that is the source set which picks the
        // generated resources back up; --existing points at the hand-written resources so the
        // providers can see what already exists.
        create("data") {
            data()
            programArgs(
                "--all", "--mod", "railways",
                "--output", rootProject.file("common/src/generated/resources").absolutePath,
                "--existing", rootProject.file("common/src/main/resources").absolutePath,
                // Our track models extend Create's, so its assets must be on the ExistingFileHelper path or
                // blockstate generation fails with "Model at create:block/... does not exist".
                "--existing-mod", "create"
            )
        }
    }
}

dependencies {
    "neoForge"("net.neoforged:neoforge:${"neoforge_version"()}")

    // Create and its dependencies
    modImplementation("com.simibubi.create:create-${"minecraft_version"()}:${"create_neoforge_version"()}:slim") { isTransitive = false }
    modImplementation("net.createmod.ponder:ponder-neoforge:${"ponder_version"()}")
    // Catnip intentionally omitted: ponder-neoforge bundles it (see :common notes).
    modImplementation("com.tterrag.registrate:Registrate:${"registrate_version"()}")
    modCompileOnly("dev.engine-room.flywheel:flywheel-neoforge-api-${"minecraft_version"()}:${"flywheel_version"()}")
    modRuntimeOnly("dev.engine-room.flywheel:flywheel-neoforge-${"minecraft_version"()}:${"flywheel_version"()}")

    // JEI: the compat plugin (RailwaysJeiPlugin) needs the API at compile time.
    modCompileOnly("mezz.jei:jei-${"minecraft_version"()}-neoforge-api:${"jei_neoforge_version"()}")
    // Compile-time only for now: JEI 19.53 requires NeoForge 21.1.238+, and we are pinned to
    // 21.1.219. The plugin still builds; add the runtime jar back after bumping NeoForge.
    // modLocalRuntime("mezz.jei:jei-${"minecraft_version"()}-neoforge:${"jei_neoforge_version"()}")

    // NeoForge 21.1 bundles MixinExtras at runtime; we only need it at compile time.
    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:${"mixin_extras_version"()}")!!)!!
}

operator fun String.invoke(): String {
    return rootProject.ext[this] as? String
        ?: throw IllegalStateException("Property $this is not defined")
}
