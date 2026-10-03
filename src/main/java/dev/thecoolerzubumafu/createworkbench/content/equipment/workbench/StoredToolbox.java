package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.equipment.toolbox.ToolboxInventory;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;

public record StoredToolbox(ToolboxInventory inventory, DyeColor color, UUID uuid) {

	public static final Codec<StoredToolbox> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ToolboxInventory.CODEC.fieldOf("inventory")
			.forGetter(StoredToolbox::inventory),
		StringRepresentable.fromEnum(DyeColor::values)
			.fieldOf("color")
			.forGetter(StoredToolbox::color),
		UUIDUtil.CODEC.fieldOf("uuid")
			.forGetter(StoredToolbox::uuid)
	).apply(instance, StoredToolbox::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, StoredToolbox> STREAM_CODEC = StreamCodec.composite(
		ToolboxInventory.STREAM_CODEC, StoredToolbox::inventory,
		ByteBufCodecs.VAR_INT.map(DyeColor::byId, DyeColor::getId), StoredToolbox::color,
		UUIDUtil.STREAM_CODEC, StoredToolbox::uuid,
		StoredToolbox::new);
}
