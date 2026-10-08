package vazkii.botania.neoforge;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegisterEvent;

import vazkii.botania.api.BotaniaAPI;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/** Resolves names stored by Botania 448 without registering duplicate items or blocks. */
final class LegacyRegistryAliases {
	private LegacyRegistryAliases() {}

	static void register(RegisterEvent event) {
		if (!event.getRegistryKey().equals(Registries.ITEM) && !event.getRegistryKey().equals(Registries.BLOCK)) {
			return;
		}
		try (var reader = new InputStreamReader(Objects.requireNonNull(
				LegacyRegistryAliases.class.getResourceAsStream("/botania-legacy-aliases.json")), StandardCharsets.UTF_8)) {
			JsonObject data = JsonParser.parseReader(reader).getAsJsonObject();
			if (event.getRegistryKey().equals(Registries.ITEM)) {
				apply(event.getRegistry(Registries.ITEM), data.getAsJsonObject("item"));
			} else {
				apply(event.getRegistry(Registries.BLOCK), data.getAsJsonObject("block"));
			}
		} catch (java.io.IOException e) {
			throw new IllegalStateException("Cannot read Botania legacy registry aliases", e);
		}
	}

	private static void apply(Registry<?> registry, JsonObject aliases) {
		for (var entry : aliases.entrySet()) {
			ResourceLocation oldId = BotaniaAPI.botaniaRL(entry.getKey());
			ResourceLocation newId = BotaniaAPI.botaniaRL(entry.getValue().getAsString());
			if (!registry.containsKey(oldId) && registry.containsKey(newId)) {
				registry.addAlias(oldId, newId);
			}
		}
	}
}
