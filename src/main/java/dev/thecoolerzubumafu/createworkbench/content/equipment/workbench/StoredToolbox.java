package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.equipment.toolbox.ToolboxInventory;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;

public record StoredToolbox(ToolboxInventory inventory, DyeColor color, UUID uuid, Component customName) {

	public StoredToolbox(ToolboxInventory inventory, DyeColor color, UUID uuid) {
		this(inventory, color, uuid, null);
	}

	public static final Codec<StoredToolbox> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ToolboxInventory.CODEC.fieldOf("inventory")
			.forGetter(StoredToolbox::inventory),
		StringRepresentable.fromEnum(DyeColor::values)
			.fieldOf("color")
			.forGetter(StoredToolbox::color),
		UUIDUtil.CODEC.fieldOf("uuid")
			.forGetter(StoredToolbox::uuid),
		ComponentSerialization.CODEC.optionalFieldOf("name")
			.forGetter(toolbox -> Optional.ofNullable(toolbox.customName()))
	).apply(instance, StoredToolbox::fromParts));

	public static final StreamCodec<RegistryFriendlyByteBuf, StoredToolbox> STREAM_CODEC = StreamCodec.composite(
		ToolboxInventory.STREAM_CODEC, StoredToolbox::inventory,
		ByteBufCodecs.VAR_INT.map(DyeColor::byId, DyeColor::getId), StoredToolbox::color,
		UUIDUtil.STREAM_CODEC, StoredToolbox::uuid,
		ByteBufCodecs.optional(ComponentSerialization.STREAM_CODEC), toolbox -> Optional.ofNullable(toolbox.customName()),
		StoredToolbox::fromParts);

	private static StoredToolbox fromParts(ToolboxInventory inventory, DyeColor color, UUID uuid,
	                                       Optional<Component> customName) {
		return new StoredToolbox(inventory, color, uuid, customName.orElse(null));
	}
}
