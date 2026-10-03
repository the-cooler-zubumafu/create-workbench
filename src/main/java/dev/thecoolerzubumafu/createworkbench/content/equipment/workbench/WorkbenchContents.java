package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record WorkbenchContents(Map<Integer, StoredToolbox> toolboxes) {

	public static final Codec<WorkbenchContents> CODEC = SlotContent.CODEC.listOf()
		.xmap(WorkbenchContents::fromList, WorkbenchContents::toList);

	public static final StreamCodec<RegistryFriendlyByteBuf, WorkbenchContents> STREAM_CODEC =
		SlotContent.STREAM_CODEC.apply(ByteBufCodecs.list())
			.map(WorkbenchContents::fromList, WorkbenchContents::toList);

	private static WorkbenchContents fromList(List<SlotContent> list) {
		Map<Integer, StoredToolbox> map = new LinkedHashMap<>();
		for (SlotContent entry : list)
			map.put(entry.slot(), entry.toolbox());
		return new WorkbenchContents(map);
	}

	private static List<SlotContent> toList(WorkbenchContents contents) {
		List<SlotContent> list = new ArrayList<>();
		contents.toolboxes()
			.forEach((slot, toolbox) -> list.add(new SlotContent(slot, toolbox)));
		return list;
	}

	record SlotContent(int slot, StoredToolbox toolbox) {

		static final Codec<SlotContent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("slot")
				.forGetter(SlotContent::slot),
			StoredToolbox.CODEC.fieldOf("toolbox")
				.forGetter(SlotContent::toolbox)
		).apply(instance, SlotContent::new));

		static final StreamCodec<RegistryFriendlyByteBuf, SlotContent> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, SlotContent::slot,
			StoredToolbox.STREAM_CODEC, SlotContent::toolbox,
			SlotContent::new);
	}
}
