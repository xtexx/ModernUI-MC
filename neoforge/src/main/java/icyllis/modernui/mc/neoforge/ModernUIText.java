/*
 * Modern UI.
 * Copyright (C) 2019-2024 BloCamLimb. All rights reserved.
 *
 * Modern UI is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Modern UI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Modern UI. If not, see <https://www.gnu.org/licenses/>.
 */

package icyllis.modernui.mc.neoforge;

import icyllis.modernui.mc.MuiModApi;
import icyllis.modernui.mc.text.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nonnull;

import static icyllis.modernui.mc.ModernUIMod.*;

/**
 * Modern UI Text for Minecraft can bootstrap independently.
 */
public final class ModernUIText {

    static {
        assert FMLEnvironment.dist.isClient();
    }

    private ModernUIText() {
    }

    public static void init(IEventBus modEventBus) {
        modEventBus.register(ModernUIText.class);
    }

    /*@SubscribeEvent
    static void registerResourceListener(@Nonnull RegisterClientReloadListenersEvent event) {
        // language may reload, cause TranslatableComponent changed, so clear layout cache
        event.registerReloadListener(TextLayoutEngine.getInstance()::reload);
        LOGGER.debug(MARKER, "Registered language reload listener");
    }*/

    @SubscribeEvent
    static void setupClient(@Nonnull FMLClientSetupEvent event) {
        // preload text engine, note that this event is fired after client config first load
        // so that the typeface config is valid
        //Minecraft.getInstance().execute(ModernUI::getSelectedTypeface);
        MuiModApi.addOnWindowResizeListener(TextLayoutEngine.getInstance());
        MuiModApi.addOnDebugDumpListener(TextLayoutEngine.getInstance());
        NeoForge.EVENT_BUS.register(EventHandler.class);
        LOGGER.info(MARKER, "Loaded modern text engine");
    }

    /*@SubscribeEvent
    static void onParallelDispatch(@Nonnull ParallelDispatchEvent event) {
        // since Forge EVENT_BUS is not started yet, we should manually maintain that
        // in case of some mods render texts before entering main menu
        event.enqueueWork(() -> TextLayoutEngine.getInstance().clear());
    }*/

    @SubscribeEvent
    static void registerShaders(@Nonnull RegisterShadersEvent event) {
        /*ResourceProvider provider = event.getResourceManager();
        try {
            event.registerShader(new ShaderInstance(provider, TextRenderType.SHADER_RL,
                    DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), TextRenderType::setShader);
            event.registerShader(new ShaderInstance(provider, TextRenderType.SHADER_SEE_THROUGH_RL,
                    DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), TextRenderType::setShaderSeeThrough);
        } catch (IOException e) {
            throw new RuntimeException("Bad shaders", e);
        }*/
        // these three are required shaders, let them preload
        event.registerShader(TextRenderType.SHADER_NORMAL);
        event.registerShader(TextRenderType.SHADER_SDF_FILL);
        event.registerShader(TextRenderType.SHADER_SDF_STROKE);
    }

    static class EventHandler {

        /*@SubscribeEvent
        static void onClientChat(@Nonnull ClientChatEvent event) {
            final String msg = event.getMessage();
            if (CONFIG.mEmojiShortcodes.get() && !msg.startsWith("/")) {
                final TextLayoutEngine engine = TextLayoutEngine.getInstance();
                final Matcher matcher = TextLayoutEngine.EMOJI_SHORTCODE_PATTERN.matcher(msg);

                StringBuilder builder = null;
                int lastEnd = 0;
                while (matcher.find()) {
                    if (builder == null) {
                        builder = new StringBuilder();
                    }
                    int st = matcher.start();
                    int en = matcher.end();
                    String emoji = null;
                    if (en - st > 2) {
                        emoji = engine.lookupEmojiShortcode(msg.substring(st + 1, en - 1));
                    }
                    if (emoji != null) {
                        builder.append(msg, lastEnd, st);
                        builder.append(emoji);
                    } else {
                        builder.append(msg, lastEnd, en);
                    }
                    lastEnd = en;
                }
                if (builder != null) {
                    builder.append(msg, lastEnd, msg.length());
                    //event.setMessage(builder.toString());
                }
            }
        }*/

        @SubscribeEvent
        static void onClientTick(@Nonnull ClientTickEvent.Post event) {
            TextLayoutEngine.getInstance().onEndClientTick();
        }

        @SubscribeEvent
        static void onRegisterClientCommands(@Nonnull RegisterClientCommandsEvent event) {
            MuiTextCommand.register(event.getDispatcher(), event.getBuildContext());
        }
    }
}
