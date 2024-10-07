package com.lying.network;

import java.util.List;

import com.google.common.collect.Lists;
import com.lying.init.HOPacketHandler;
import com.lying.utility.VillageComponent;

import dev.architectury.networking.NetworkManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ShowCubesPacket
{
	private static final Identifier PACKET_ID = HOPacketHandler.SHOW_CUBE_ID;
	public static final CustomPayload.Id<Payload> PACKET_TYPE = new CustomPayload.Id<>(PACKET_ID);
	public static final PacketCodec<RegistryByteBuf, Payload> PACKET_CODEC = CustomPayload.codecOf(Payload::write, Payload::new);
	
	public static void send(ServerPlayerEntity player, VillageComponent component)
	{
		send(player, List.of(component));
	}
	
	public static void send(ServerPlayerEntity player, List<VillageComponent> components)
	{
		NetworkManager.sendToPlayer(player, new Payload(components));
	}
	
	public static record Payload(List<VillageComponent> highlights) implements CustomPayload
	{
		public Payload(RegistryByteBuf buffer)
		{
			this(nbtToList(buffer.readNbt().getList("List", NbtElement.COMPOUND_TYPE)));
		}
		
		public void write(RegistryByteBuf buffer)
		{
			NbtCompound nbt = new NbtCompound();
			NbtList list = new NbtList();
			highlights.forEach(entry -> list.add(entry.toNbt()));
			nbt.put("List", list);
			buffer.writeNbt(nbt);
		}
		
		private static List<VillageComponent> nbtToList(NbtElement nbt)
		{
			List<VillageComponent> entries = Lists.newArrayList();
			NbtList list = (NbtList)nbt;
			list.forEach(element -> 
			{
				VillageComponent entry = VillageComponent.fromNbt((NbtCompound)element);
				if(entry != null)
					entries.add(entry);
			});
			return entries;
		}
		
		public Id<? extends CustomPayload> getId() { return PACKET_TYPE; }
	}
}
