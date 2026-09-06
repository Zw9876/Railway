/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
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

package com.railwayteam.railways.neoforge;

import com.railwayteam.railways.multiloader.neoforge.RailwaysPayloads;
import net.neoforged.fml.common.EventBusSubscriber;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.config.neoforge.CRConfigsImpl;
import com.railwayteam.railways.multiloader.CommandRegistrar;
import com.railwayteam.railways.content.fuel.tank.FuelTankBlock;
import com.railwayteam.railways.multiloader.Env;
import com.railwayteam.railways.registry.neoforge.CRBlockEntitiesImpl;
import com.railwayteam.railways.registry.neoforge.CRBlocksImpl;
import com.railwayteam.railways.registry.neoforge.CRCreativeModeTabsImpl;
import com.railwayteam.railways.registry.neoforge.CRMountedStorageTypesImpl;
import com.railwayteam.railways.registry.neoforge.CRParticleTypesParticleEntryImpl;
import com.railwayteam.railways.util.Utils;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.api.contraption.BlockMovementChecks;
import com.simibubi.create.api.contraption.BlockMovementChecks.CheckResult;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

@Mod(Railways.MOD_ID)
@EventBusSubscriber
public class RailwaysImpl {
	static IEventBus bus;

	public RailwaysImpl() {
		restoreLoggers();
		bus = FMLJavaModLoadingContext.get().getModEventBus();
		CRCreativeModeTabsImpl.register(RailwaysImpl.bus);
		Railways.init();
		CRConfigsImpl.register(ModLoadingContext.get());
		CRParticleTypesParticleEntryImpl.register(bus);
		//noinspection Convert2MethodRef
		Env.CLIENT.runIfCurrent(() -> () -> RailwaysClientImpl.init());

		bus.addListener(RailwaysImpl::onCommonSetup);
		// 1.21 payload networking: PacketSet's channels have to be registered on the mod bus.
		bus.addListener(RailwaysPayloads::register);
	}

	public static void onCommonSetup(final FMLCommonSetupEvent event) {
		event.enqueueWork(Railways::postRegistrationInit);
	}

	public static void finalizeRegistrate() {
		Railways.registrate().registerEventListeners(bus);
	}

	private static final Set<CommandRegistrar> commandRegistrars = new HashSet<>();

	public static void registerCommands(CommandRegistrar registrar) {
		commandRegistrars.add(registrar);
	}

	@SubscribeEvent
	public static void onCommandRegistration(RegisterCommandsEvent event) {
		CommandSelection selection = event.getCommandSelection();
		boolean dedicated = selection == CommandSelection.ALL || selection == CommandSelection.DEDICATED;
		commandRegistrars.forEach(registrar -> registrar.register(event.getDispatcher(), dedicated, event.getBuildContext()));
	}

	private static void restoreLoggers() {
		if (Utils.isDevEnv()) {
			// restore our logging config, since forge likes to nuke it for fun
			for (String prop : new String[] {"log4j.configurationFile", "log4j2.configurationFile"}) {
				String file = System.getProperty(prop);
				if (file != null) {
					Configurator.reconfigure(ConfigurationFactory.getInstance().getConfiguration(
						LoggerContext.getContext(),
						ConfigurationSource.fromUri(URI.create(file))
					));
					break;
				}
			}
		}
	}

	public static void platformBasedRegistration() {
		BlockMovementChecks.registerAttachedCheck((BlockState state, Level world, BlockPos pos, Direction direction) -> {
			if (state.getBlock() instanceof FuelTankBlock && ConnectivityHandler.isConnected(world, pos, pos.relative(direction)))
				return CheckResult.SUCCESS;
			return CheckResult.PASS;
		});

		CRMountedStorageTypesImpl.init();
		CRBlocksImpl.init();
		CRBlockEntitiesImpl.init();
	}
}
