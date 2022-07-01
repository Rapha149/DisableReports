package de.rapha149.clearfog.version;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatBaseComponent.ChatSerializer;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.PacketPlayInChat;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.util.MinecraftEncryption.b;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Wrapper1_19_R1 implements VersionWrapper {

    private static final Field ADVENTURE_FIELD;

    static {
        Field adventureField;
        try {
            //noinspection JavaReflectionMemberAccess
            adventureField = ClientboundPlayerChatPacket.class.getDeclaredField("adventure$message");
            adventureField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            adventureField = null;
        }
        ADVENTURE_FIELD = adventureField;
    }

    @Override
    public void addListener(Player player, Function<Player, Boolean> removeSignature, Supplier<Boolean> replaceWithSystemMessage) {
        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().b.b.m.pipeline();
        if (pipeline.names().contains(INCOMING_HANDLER_NAME))
            pipeline.remove(INCOMING_HANDLER_NAME);
        if (pipeline.names().contains(OUTGOING_HANDLER_NAME))
            pipeline.remove(OUTGOING_HANDLER_NAME);

        // incoming packets
        pipeline.addAfter("decoder", INCOMING_HANDLER_NAME, new MessageToMessageDecoder<Packet<?>>() {

            @Override
            protected void decode(ChannelHandlerContext chc, Packet<?> obj, List<Object> out) {
                try {
                    if (removeSignature.apply(player)) {
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

        // outgoing packets
        pipeline.addAfter("packet_handler", OUTGOING_HANDLER_NAME, new ChannelDuplexHandler() {

            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                try {
                    if (replaceWithSystemMessage.get() && msg instanceof ClientboundPlayerChatPacket packet) {
                        ChatSender sender = packet.f();
                        UUID senderUuid = sender.a();
                        if (removeSignature.apply(Bukkit.getPlayer(senderUuid))) {
                            IChatBaseComponent message = null;
                            if (ADVENTURE_FIELD != null) {
                                try {
                                    Object adventure = ADVENTURE_FIELD.get(packet);
                                    if (adventure != null)
                                        message = ChatSerializer.a(adventureToJson(adventure));
                                } catch (IllegalAccessException ignore) {
                                }
                            }

                            if (message == null)
                                message = packet.d().orElse(packet.c());

                            int type = packet.e();
                            if (type >= 3 && type <= 6) {
                                JSONObject json = new JSONObject();
                                JSONArray with = new JSONArray();
                                JSONObject jsonMessage = new JSONObject(ChatSerializer.a(message));

                                String senderName = sender.b().getString();
                                JSONObject player = new JSONObject();
                                player.put("text", "");
                                player.put("insertion", senderName);
                                JSONObject clickEvent = new JSONObject();
                                clickEvent.put("action", "suggest_command");
                                clickEvent.put("value", "/tell " + senderName + " ");
                                player.put("clickEvent", clickEvent);
                                JSONObject hoverEvent = new JSONObject();
                                hoverEvent.put("action", "show_entity");
                                JSONObject contents = new JSONObject();
                                contents.put("type", "minecraft:player");
                                contents.put("id", senderUuid.toString());
                                JSONObject name = new JSONObject();
                                name.put("text", senderName);
                                contents.put("name", name);
                                hoverEvent.put("contents", contents);
                                player.put("hoverEvent", hoverEvent);
                                JSONArray extra = new JSONArray();
                                JSONObject empty = new JSONObject();
                                empty.put("text", "");
                                extra.put(empty);
                                extra.put(name);
                                extra.put(empty);
                                player.put("extra", extra);

                                switch (type) {
                                    case 3:
                                        json.put("translate", "chat.type.announcement");
                                        with.put(player);
                                        with.put(jsonMessage);
                                        break;
                                    case 4:
                                        json.put("translate", "commands.message.display.incoming");
                                        json.put("color", "gray");
                                        json.put("italic", true);
                                        with.put(player);
                                        with.put(jsonMessage);
                                        break;
                                    case 5:
                                        JSONObject team = new JSONObject();
                                        team.put("translate", "chat.square_brackets");
                                        JSONObject teamWith = new JSONObject();
                                        teamWith.put("text", "");
                                        teamWith.put("insertion", "Dummy");
                                        hoverEvent = new JSONObject();
                                        hoverEvent.put("action", "show_text");
                                        contents = new JSONObject();
                                        contents.put("text", "Dummy");
                                        hoverEvent.put("contents", contents);
                                        teamWith.put("hoverEvent", hoverEvent);
                                        team.put("with", Arrays.asList(teamWith));

                                        clickEvent = new JSONObject();
                                        clickEvent.put("action", "suggest_command");
                                        clickEvent.put("value", "/teammsg ");
                                        team.put("clickEvent", clickEvent);
                                        hoverEvent = new JSONObject();
                                        hoverEvent.put("action", "show_text");
                                        contents = new JSONObject();
                                        contents.put("translate", "chat.type.team.hover");
                                        hoverEvent.put("contents", contents);
                                        team.put("hoverEvent", hoverEvent);

                                        json.put("translate", "chat.type.team.sent");
                                        with.put(team);
                                        with.put(player);
                                        with.put(jsonMessage);
                                        break;
                                    case 6:
                                        json.put("translate", "chat.type.emote");
                                        with.put(player);
                                        with.put(jsonMessage);
                                        break;
                                }

                                json.put("with", with);
                                message = ChatSerializer.a(json.toString());
                                type = 1;
                            }

                            System.out.println(ChatSerializer.a(message));
                            msg = new ClientboundSystemChatPacket(message, type);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                super.write(ctx, msg, promise);
            }
        });
    }
}
