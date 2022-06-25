package de.rapha149.clearfog.version;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayInChat;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.util.MinecraftEncryption.b;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Wrapper1_19_R1 implements VersionWrapper {

    @Override
    public void addListener(Player player, Supplier<Boolean> removeSignature) {
        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().b.b.m.pipeline();
        if (pipeline.names().contains(HANDLER_NAME))
            pipeline.remove(HANDLER_NAME);

        pipeline.addAfter("decoder", HANDLER_NAME, new MessageToMessageDecoder<Packet<?>>() {

            @Override
            protected void decode(ChannelHandlerContext chc, Packet<?> obj, List<Object> out) {
                try {
                    if (removeSignature.get()) {
                        // TODO remove debug messages
                        if (obj instanceof PacketPlayInChat packet) {
                            b signature = packet.a(player.getUniqueId()).e();
                            System.out.println(packet.b() + "\n" + Arrays.toString(signature.b()) + " | " + signature.c() + " | " + Arrays.toString(signature.d()));
                            obj = new PacketPlayInChat(packet.b(), MessageSignature.a(), packet.d());
                        } else if (obj instanceof ServerboundChatCommandPacket packet) {
                            ArgumentSignatures signatures = packet.d();
                            System.out.println(packet.b() + "\n" + signatures.b() + "\n" + signatures.c().entrySet().stream()
                                    .map(entry -> entry.getKey() + ": " + Arrays.toString(entry.getValue()))
                                    .collect(Collectors.joining("\n")));
                            obj = new ServerboundChatCommandPacket(packet.b(), packet.c(), ArgumentSignatures.a(), packet.e());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                out.add(obj);
            }
        });
    }
}
