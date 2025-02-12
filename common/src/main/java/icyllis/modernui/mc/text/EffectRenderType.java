/*
 * Modern UI.
 * Copyright (C) 2019-2023 BloCamLimb. All rights reserved.
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

package icyllis.modernui.mc.text;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import icyllis.arc3d.core.ColorInfo;
import icyllis.arc3d.core.RefCnt;
import icyllis.arc3d.engine.*;
import icyllis.arc3d.opengl.GLDevice;
import icyllis.arc3d.opengl.GLTexture;
import icyllis.modernui.annotation.RenderThread;
import icyllis.modernui.core.Core;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.Objects;

public class EffectRenderType extends RenderType {

    private static GLTexture WHITE;

    private static final EffectRenderType TYPE;
    private static final EffectRenderType SEE_THROUGH_TYPE;
    private static final EffectRenderType POLYGON_OFFSET_TYPE;

    static {
        TYPE = new EffectRenderType("modern_text_effect", 256, () -> {
            TextRenderType.VANILLA_STATES.forEach(RenderStateShard::setupRenderState);
            RenderSystem.setShaderTexture(0, WHITE.getHandle());
        }, () -> TextRenderType.VANILLA_STATES.forEach(RenderStateShard::clearRenderState));
        SEE_THROUGH_TYPE = new EffectRenderType("modern_text_effect_see_through", 256, () -> {
            TextRenderType.SEE_THROUGH_STATES.forEach(RenderStateShard::setupRenderState);
            RenderSystem.setShaderTexture(0, WHITE.getHandle());
        }, () -> TextRenderType.SEE_THROUGH_STATES.forEach(RenderStateShard::clearRenderState));
        POLYGON_OFFSET_TYPE = new EffectRenderType("modern_text_effect_polygon_offset", 256, () -> {
            TextRenderType.POLYGON_OFFSET_STATES.forEach(RenderStateShard::setupRenderState);
            RenderSystem.setShaderTexture(0, WHITE.getHandle());
        }, () -> TextRenderType.POLYGON_OFFSET_STATES.forEach(RenderStateShard::clearRenderState));
    }

    private EffectRenderType(String name, int bufferSize, Runnable setupState, Runnable clearState) {
        super(name, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS,
                bufferSize, false, true, setupState, clearState);
    }

    @RenderThread
    @Nonnull
    public static EffectRenderType getRenderType(boolean seeThrough, boolean polygonOffset) {
        if (WHITE == null)
            makeWhiteTexture();
        return polygonOffset ? POLYGON_OFFSET_TYPE : seeThrough ? SEE_THROUGH_TYPE : TYPE;
    }

    @RenderThread
    @Nonnull
    public static EffectRenderType getRenderType(Font.DisplayMode mode) {
        return switch (mode) {
            case SEE_THROUGH -> SEE_THROUGH_TYPE;
            case POLYGON_OFFSET -> POLYGON_OFFSET_TYPE;
            default -> TYPE;
        };
    }

    public static void clear() {
        WHITE = RefCnt.move(WHITE);
    }

    private static void makeWhiteTexture() {
        ImmediateContext context = Core.requireImmediateContext();
        final int width = 8, height = 8;
        final int colorType = ColorInfo.CT_RGBA_8888;
        ImageDesc desc = context.getCaps().getDefaultColorImageDesc(
                Engine.ImageType.k2D,
                colorType,
                width, height,
                1,
                ISurface.FLAG_SAMPLED_IMAGE
        );
        Objects.requireNonNull(desc); // RGBA8 is always supported
        WHITE = (GLTexture) context
                .getResourceProvider()
                .findOrCreateImage(
                        desc,
                        /*budgeted*/ false,
                        "WhiteTexture"
                );
        Objects.requireNonNull(WHITE);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int bpp = ColorInfo.bytesPerPixel(colorType);
            ByteBuffer pixels = stack.malloc(width * height * bpp);
            MemoryUtil.memSet(pixels, 0xff);
            boolean res = ((GLDevice) context.getDevice()).writePixels(
                    WHITE, 0, 0, width, height,
                    colorType, colorType,
                    width * bpp,
                    MemoryUtil.memAddress(pixels)
            );
            assert res;

            int boundTexture = GL33C.glGetInteger(GL33C.GL_TEXTURE_BINDING_2D);
            GL33C.glBindTexture(GL33C.GL_TEXTURE_2D, WHITE.getHandle());

            GL33C.glTexParameteri(GL33C.GL_TEXTURE_2D, GL33C.GL_TEXTURE_MAG_FILTER, GL33C.GL_NEAREST);
            GL33C.glTexParameteri(GL33C.GL_TEXTURE_2D, GL33C.GL_TEXTURE_MIN_FILTER, GL33C.GL_NEAREST);

            GL33C.glBindTexture(GL33C.GL_TEXTURE_2D, boundTexture);
        }
    }
}
