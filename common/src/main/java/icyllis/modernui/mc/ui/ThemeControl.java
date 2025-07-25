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

package icyllis.modernui.mc.ui;

import icyllis.modernui.R;
import icyllis.modernui.graphics.drawable.Drawable;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.graphics.drawable.StateListDrawable;
import icyllis.modernui.graphics.text.CharSequenceBuilder;
import icyllis.modernui.resources.TypedValue;
import icyllis.modernui.util.StateSet;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringDecomposer;

import javax.annotation.Nonnull;

public class ThemeControl {

    public static final int BACKGROUND_COLOR = 0xc0292a2c;
    public static final int THEME_COLOR = 0xffcda398;
    public static final int THEME_COLOR_2 = 0xffcd98a3;

    private static Drawable.ConstantState sBackgroundState;
    private static int sBackgroundDensity;

    public static synchronized void addBackground(@Nonnull View view) {
        createBackground(view);
        if (sBackgroundState != null) {
            view.setBackground(sBackgroundState.newDrawable());
        }
    }

    private static synchronized void createBackground(@Nonnull View view) {
        int density = view.getContext().getResources().getDisplayMetrics().densityDpi;
        if (sBackgroundState == null || density != sBackgroundDensity) {
            sBackgroundDensity = density;
            StateListDrawable background = new StateListDrawable();
            ShapeDrawable drawable = new ShapeDrawable();
            drawable.setShape(ShapeDrawable.RECTANGLE);
            drawable.setColor(0x60A0A0A0);
            drawable.setCornerRadius(view.dp(3));
            int dp1 = view.dp(1);
            drawable.setStroke(dp1, 0xFFE6E6E6);
            background.addState(StateSet.get(StateSet.VIEW_STATE_HOVERED), drawable);
            background.setEnterFadeDuration(200);
            background.setExitFadeDuration(200);
            sBackgroundState = background.getConstantState();
        }
    }

    public static Drawable makeDivider(@Nonnull View view) {
        return makeDivider(view, false);
    }

    public static Drawable makeDivider(@Nonnull View view, boolean vertical) {
        ShapeDrawable drawable = new ShapeDrawable();
        drawable.setShape(vertical ? ShapeDrawable.VLINE : ShapeDrawable.HLINE);
        TypedValue value = new TypedValue();
        view.getContext().getTheme().resolveAttribute(R.ns, R.attr.colorOutlineVariant, value, true);
        drawable.setColor(value.data);
        drawable.setSize(view.dp(1), view.dp(1));
        return drawable;
    }

    @Nonnull
    public static String stripFormattingCodes(@Nonnull String str) {
        if (str.indexOf(167) >= 0) {
            var csb = new CharSequenceBuilder();
            boolean res = StringDecomposer.iterateFormatted(str, Style.EMPTY, (index, style, codePoint) -> {
                csb.addCodePoint(codePoint);
                return true;
            });
            assert res;
            return csb.toString();
        }
        return str;
    }

    public static void setViewAndChildrenEnabled(View view, boolean enabled) {
        if (view != null) {
            view.setEnabled(enabled);
        }
        if (view instanceof ViewGroup vg) {
            int cc = vg.getChildCount();
            for (int i = 0; i < cc; i++) {
                View v = vg.getChildAt(i);
                setViewAndChildrenEnabled(v, enabled);
            }
        }
    }
}
