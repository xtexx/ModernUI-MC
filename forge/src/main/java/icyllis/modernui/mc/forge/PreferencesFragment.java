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

package icyllis.modernui.mc.forge;

import icyllis.modernui.R;
import icyllis.modernui.animation.*;
import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.annotation.Nullable;
import icyllis.modernui.core.Context;
import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.graphics.Color;
import icyllis.modernui.graphics.MathUtil;
import icyllis.modernui.mc.forge.ui.*;
import icyllis.modernui.text.InputFilter;
import icyllis.modernui.text.method.DigitsInputFilter;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.view.*;
import icyllis.modernui.viewpager.widget.*;
import icyllis.modernui.widget.*;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static icyllis.modernui.view.ViewGroup.LayoutParams.*;

public class PreferencesFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable DataSet savedInstanceState) {
        var pager = new ViewPager(getContext());

        pager.setAdapter(new TheAdapter());
        pager.setFocusableInTouchMode(true);
        pager.setKeyboardNavigationCluster(true);

        pager.setEdgeEffectColor(ThemeControl.THEME_COLOR);

        {
            var indicator = new LinearPagerIndicator(getContext());
            indicator.setPager(pager);
            indicator.setLineWidth(pager.dp(4));
            indicator.setLineColor(ThemeControl.THEME_COLOR);
            var lp = new ViewPager.LayoutParams();
            lp.height = pager.dp(30);
            lp.isDecor = true;
            lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
            pager.addView(indicator, lp);
        }

        var lp = new FrameLayout.LayoutParams(pager.dp(720), ViewGroup.LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.CENTER;
        pager.setLayoutParams(lp);

        return pager;
    }

    private static class TheAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return 2;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            var context = container.getContext();
            var sv = new ScrollView(context);
            if (position == 1) {
                sv.addView(createSecondPage(context), MATCH_PARENT, WRAP_CONTENT);
            } else {
                sv.addView(createFirstPage(context), MATCH_PARENT, WRAP_CONTENT);

                var animator = ObjectAnimator.ofFloat(sv,
                        View.ROTATION_Y, container.isLayoutRtl() ? -45 : 45, 0);
                animator.setInterpolator(TimeInterpolator.DECELERATE);
                sv.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
                                               int oldTop, int oldRight, int oldBottom) {
                        animator.start();
                        v.removeOnLayoutChangeListener(this);
                    }
                });
            }
            sv.setEdgeEffectColor(ThemeControl.THEME_COLOR);

            var params = new LinearLayout.LayoutParams(0, MATCH_PARENT, 1);
            var dp6 = sv.dp(6);
            params.setMargins(dp6, dp6, dp6, dp6);
            container.addView(sv, params);

            return sv;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }
    }

    public static LinearLayout createSecondPage(Context context) {
        var content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);

        {
            var list = createCategoryList(context, "modernui.center.category.system");

            list.addView(createBooleanOption(context, "modernui.center.system.forceRtlLayout",
                    Config.CLIENT.mForceRtl, Config.CLIENT::saveAndReloadAsync));

            list.addView(createFloatOption(context, "modernui.center.system.globalFontScale",
                    Config.Client.FONT_SCALE_MIN, Config.Client.FONT_SCALE_MAX,
                    4, Config.CLIENT.mFontScale, Config.CLIENT::saveAndReloadAsync));

            {
                var option = createInputBox(context, "modernui.center.system.globalAnimationScale");
                var input = option.<EditText>requireViewById(R.id.input);
                input.setText(Float.toString(ValueAnimator.sDurationScale));
                input.setFilters(DigitsInputFilter.getInstance(input.getTextLocale(), false, true),
                        new InputFilter.LengthFilter(4));
                input.setOnFocusChangeListener((view, hasFocus) -> {
                    if (!hasFocus) {
                        EditText v = (EditText) view;
                        double scale = Math.max(Math.min(Double.parseDouble(v.getText().toString()), 10), 0.1);
                        v.setText(Double.toString(scale));
                        if (scale != ValueAnimator.sDurationScale) {
                            ValueAnimator.sDurationScale = (float) scale;
                        }
                    }
                });
                list.addView(option);
            }

            content.addView(list);
        }

        {
            var list = createCategoryList(context, "modernui.center.category.font");

            {
                var option = new LinearLayout(context);
                option.setOrientation(LinearLayout.HORIZONTAL);
                option.setHorizontalGravity(Gravity.START);

                final int dp3 = content.dp(3);
                {
                    var title = new TextView(context);
                    title.setText(I18n.get("modernui.center.font.fontFamily"));
                    title.setTooltipText(I18n.get("modernui.center.font.fontFamily_desc"));
                    title.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                    title.setTextSize(14);
                    title.setMinWidth(content.dp(60));

                    var params = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 2);
                    params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
                    option.addView(title, params);
                }
                {
                    var input = new EditText(context);
                    input.setId(R.id.input);
                    input.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                    input.setTextSize(14);
                    input.setPadding(dp3, 0, dp3, 0);

                    input.setText(String.join("\n", Config.CLIENT.mFontFamily.get()));
                    input.setOnFocusChangeListener((view, hasFocus) -> {
                        if (!hasFocus) {
                            EditText v = (EditText) view;
                            ArrayList<String> result = new ArrayList<>();
                            for (String s : v.getText().toString().split("\n")) {
                                if (!s.isBlank()) {
                                    String strip = s.strip();
                                    if (!strip.isEmpty() && !result.contains(strip)) {
                                        result.add(strip);
                                    }
                                }
                            }
                            v.setText(String.join("\n", result));
                            if (!Config.CLIENT.mFontFamily.get().equals(result)) {
                                Config.CLIENT.mFontFamily.set(result);
                                Config.CLIENT.saveAsync();
                                Toast.makeText(v.getContext(),
                                                I18n.get("gui.modernui.restart_to_work"),
                                                Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                    });

                    ThemeControl.addBackground(input);

                    var params = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 5);
                    params.gravity = Gravity.CENTER_VERTICAL;
                    option.addView(input, params);
                }

                var params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
                params.gravity = Gravity.CENTER;
                params.setMargins(content.dp(6), 0, content.dp(6), 0);
                option.setLayoutParams(params);

                list.addView(option);
            }

            /*list.addView(createBooleanOption(context, "modernui.center.font.vanillaFont",
                    ModernUIText.CONFIG.mUseVanillaFont,
                    ModernUIText.CONFIG::saveAndReloadAsync));*/

            list.addView(createBooleanOption(context, "modernui.center.font.antiAliasing",
                    Config.CLIENT.mAntiAliasing, Config.CLIENT::saveAndReloadAsync));

            list.addView(createBooleanOption(context, "modernui.center.font.autoHinting",
                    Config.CLIENT.mAutoHinting, Config.CLIENT::saveAndReloadAsync));

            content.addView(list);
        }

        content.setDividerDrawable(new DividerDrawable(content));
        content.setDividerPadding(content.dp(8));
        content.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);

        return content;
    }

    public static LinearLayout createFirstPage(Context context) {
        var content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        var transition = new LayoutTransition();
        transition.enableTransitionType(LayoutTransition.CHANGING);
        content.setLayoutTransition(transition);

        Runnable saveFn = Config.CLIENT::saveAndReloadAsync;

        // Screen
        {
            var list = createCategoryList(context, "modernui.center.category.screen");

            list.addView(createColorOpacityOption(context, "modernui.center.screen.backgroundOpacity",
                    Config.CLIENT.mBackgroundColor, saveFn));

            list.addView(createIntegerOption(context, "modernui.center.screen.backgroundDuration",
                    Config.Client.ANIM_DURATION_MIN, Config.Client.ANIM_DURATION_MAX,
                    3, 50, Config.CLIENT.mBackgroundDuration, saveFn));
            {
                var option = createBooleanOption(context, "modernui.center.screen.blurEffect",
                        Config.CLIENT.mBlurEffect, saveFn);
                option.setTooltipText(I18n.get("modernui.center.screen.blurEffect_desc"));
                list.addView(option);
            }

            list.addView(createIntegerOption(context, "modernui.center.screen.blurRadius",
                    Config.Client.BLUR_RADIUS_MIN, Config.Client.BLUR_RADIUS_MAX,
                    2, 1, Config.CLIENT.mBlurRadius, saveFn));

            list.addView(createSpinnerOption(context, "modernui.center.screen.windowMode",
                    Config.Client.WindowMode.values(), Config.CLIENT.mWindowMode, saveFn));

            {
                var option = createBooleanOption(context, "modernui.center.screen.inventoryPause",
                        Config.CLIENT.mInventoryPause, saveFn);
                option.setTooltipText(I18n.get("modernui.center.screen.inventoryPause_desc"));
                list.addView(option);
            }

            content.addView(list);
        }

        {
            var list = createCategoryList(context, "modernui.center.category.extension");

            {
                var option = createBooleanOption(context, "modernui.center.extension.ding",
                        Config.CLIENT.mDing, saveFn);
                option.setTooltipText(I18n.get("modernui.center.extension.ding_desc"));
                list.addView(option);
            }

            {
                var option = createSwitchLayout(context, "modernui.center.extension.smoothScrolling");
                option.setTooltipText(I18n.get("modernui.center.extension.smoothScrolling_desc"));
                var button = option.<SwitchButton>requireViewById(R.id.button1);
                button.setChecked(!Boolean.parseBoolean(
                        ModernUIForge.getBootstrapProperty(ModernUIForge.BOOTSTRAP_DISABLE_SMOOTH_SCROLLING)
                ));
                button.setOnCheckedChangeListener((__, checked) -> {
                    ModernUIForge.setBootstrapProperty(
                            ModernUIForge.BOOTSTRAP_DISABLE_SMOOTH_SCROLLING,
                            Boolean.toString(!checked)
                    );
                    Toast.makeText(__.getContext(),
                                    I18n.get("gui.modernui.restart_to_work"),
                                    Toast.LENGTH_SHORT)
                            .show();
                });
                list.addView(option);
            }

            list.addView(createBooleanOption(context, "modernui.center.extension.tooltip",
                    Config.CLIENT.mTooltip, saveFn));

            /*list.addView(createIntegerOption(context, "modernui.center.extension.tooltipDuration",
                    Config.Client.ANIM_DURATION_MIN, Config.Client.ANIM_DURATION_MAX,
                    3, 50, Config.CLIENT.mTooltipDuration, saveFn));*/

            list.addView(createColorOpacityOption(context, "modernui.center.extension.tooltipBgOpacity",
                    Config.CLIENT.mTooltipFill, saveFn));

            {
                var layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);

                final int dp6 = layout.dp(6);
                final Button title;
                {
                    title = new Button(context);
                    title.setText(I18n.get("modernui.center.extension.tooltipBorderColor"));
                    title.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                    title.setTextSize(14);

                    var params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
                    layout.addView(title, params);
                }

                title.setOnClickListener(new TooltipBorderCollapsed(layout, saveFn));

                ThemeControl.addBackground(title);

                var params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
                params.gravity = Gravity.CENTER;
                params.setMargins(dp6, layout.dp(3), dp6, 0);
                list.addView(layout, params);
            }

            content.addView(list);
        }

        if (ModernUIForge.enablesTextEngine()) {
            var category = createCategoryList(context, "modernui.center.category.text");

            saveFn = ModernUIText.CONFIG::saveAndReloadAsync;

            {
                var option = createBooleanOption(context, "modernui.center.text.textShadersInWorld",
                        ModernUIText.CONFIG.mUseTextShadersInWorld, saveFn);
                option.setTooltipText(I18n.get("modernui.center.text.textShadersInWorld_desc"));
                category.addView(option);
            }

            {
                var option = createSpinnerOption(context, "modernui.center.text.defaultFontBehavior",
                        ModernUIText.Config.DefaultFontBehavior.values(),
                        ModernUIText.CONFIG.mDefaultFontBehavior, saveFn);
                option.getChildAt(0)
                        .setTooltipText(I18n.get("modernui.center.text.defaultFontBehavior_desc"));
                category.addView(option);
            }

            {
                var option = createBooleanOption(context, "modernui.center.text.colorEmoji",
                        ModernUIText.CONFIG.mUseColorEmoji, saveFn);
                option.setTooltipText(I18n.get("modernui.center.text.colorEmoji_desc"));
                category.addView(option);
            }

            {
                var option = createBooleanOption(context, "modernui.center.text.emojiShortcodes",
                        ModernUIText.CONFIG.mEmojiShortcodes, saveFn);
                option.setTooltipText(I18n.get("modernui.center.text.emojiShortcodes_desc"));
                category.addView(option);
            }

            category.addView(createSpinnerOption(context, "modernui.center.text.bidiHeuristicAlgo",
                    ModernUIText.Config.TextDirection.values(),
                    ModernUIText.CONFIG.mTextDirection,
                    saveFn));

            category.addView(createBooleanOption(context, "modernui.center.text.allowShadow",
                    ModernUIText.CONFIG.mAllowShadow, saveFn));

            category.addView(createFloatOption(context, "modernui.center.text.shadowOffset",
                    ModernUIText.Config.SHADOW_OFFSET_MIN, ModernUIText.Config.SHADOW_OFFSET_MAX,
                    5, ModernUIText.CONFIG.mShadowOffset, saveFn));

            category.addView(createBooleanOption(context, "modernui.center.text.allowAsyncLayout",
                    ModernUIText.CONFIG.mAllowAsyncLayout, saveFn));

            {
                var option = createSpinnerOption(context, "modernui.center.text.lineBreakStyle",
                        ModernUIText.Config.LineBreakStyle.values(),
                        ModernUIText.CONFIG.mLineBreakStyle, saveFn);
                option.getChildAt(0)
                        .setTooltipText(I18n.get("modernui.center.text.lineBreakStyle_desc"));
                category.addView(option);
            }

            category.addView(createSpinnerOption(context, "modernui.center.text.lineBreakWordStyle",
                    ModernUIText.Config.LineBreakWordStyle.values(),
                    ModernUIText.CONFIG.mLineBreakWordStyle, saveFn));

            {
                var option = createBooleanOption(context, "modernui.center.text.useComponentCache",
                        ModernUIText.CONFIG.mUseComponentCache, saveFn);
                option.setTooltipText(I18n.get("modernui.center.text.useComponentCache_desc"));
                category.addView(option);
            }

            category.addView(createBooleanOption(context, "modernui.center.text.fixedResolution",
                    ModernUIText.CONFIG.mFixedResolution, saveFn));

            category.addView(createFloatOption(context, "modernui.center.text.baseFontSize",
                    ModernUIText.Config.BASE_FONT_SIZE_MIN, ModernUIText.Config.BASE_FONT_SIZE_MAX,
                    5, ModernUIText.CONFIG.mBaseFontSize, saveFn));

            category.addView(createFloatOption(context, "modernui.center.text.baselineShift",
                    ModernUIText.Config.BASELINE_MIN, ModernUIText.Config.BASELINE_MAX,
                    5, ModernUIText.CONFIG.mBaselineShift, saveFn));

            category.addView(createFloatOption(context, "modernui.center.text.outlineOffset",
                    ModernUIText.Config.OUTLINE_OFFSET_MIN, ModernUIText.Config.OUTLINE_OFFSET_MAX,
                    5, ModernUIText.CONFIG.mOutlineOffset, saveFn));

            content.addView(category);
        }

        content.setDividerDrawable(new DividerDrawable(content));
        content.setDividerPadding(content.dp(8));
        content.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);

        return content;
    }

    @NonNull
    public static LinearLayout createCategoryList(Context context,
                                                  String name) {
        var layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final int dp6 = layout.dp(6);
        final int dp12 = layout.dp(12);
        final int dp18 = layout.dp(18);
        {
            var title = new TextView(context);
            title.setId(R.id.title);
            title.setText(I18n.get(name));
            title.setTextSize(16);
            title.setTextColor(ThemeControl.THEME_COLOR);

            var params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            params.gravity = Gravity.START;
            params.setMargins(dp6, dp6, dp6, dp6);
            layout.addView(title, params);
        }

        var params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        params.setMargins(dp12, dp12, dp12, dp18);
        layout.setLayoutParams(params);

        return layout;
    }

    public static LinearLayout createSwitchLayout(Context context, String name) {
        var layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setHorizontalGravity(Gravity.START);

        final int dp3 = layout.dp(3);
        final int dp6 = layout.dp(6);
        {
            var title = new TextView(context);
            title.setText(I18n.get(name));
            title.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            title.setTextSize(14);

            var params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, 1);
            params.gravity = Gravity.CENTER_VERTICAL;
            layout.addView(title, params);
        }
        {
            var button = new SwitchButton(context);
            button.setId(R.id.button1);
            button.setCheckedColor(ThemeControl.THEME_COLOR);

            var params = new LinearLayout.LayoutParams(layout.dp(36), layout.dp(16));
            params.gravity = Gravity.CENTER_VERTICAL;
            params.setMargins(0, dp3, 0, dp3);
            layout.addView(button, params);
        }

        var params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        params.setMargins(dp6, 0, dp6, 0);
        layout.setLayoutParams(params);

        return layout;
    }

    public static LinearLayout createBooleanOption(
            Context context,
            String name,
            ForgeConfigSpec.BooleanValue config,
            Runnable saveFn) {
        var layout = createSwitchLayout(context, name);
        var button = layout.<SwitchButton>requireViewById(R.id.button1);
        button.setChecked(config.get());
        button.setOnCheckedChangeListener((__, checked) -> {
            config.set(checked);
            saveFn.run();
        });
        return layout;
    }

    public static <E extends Enum<E>> LinearLayout createSpinnerOption(
            Context context,
            String name,
            E[] values,
            ForgeConfigSpec.EnumValue<E> config,
            Runnable saveFn) {
        var option = new LinearLayout(context);
        option.setOrientation(LinearLayout.HORIZONTAL);
        option.setHorizontalGravity(Gravity.START);

        final int dp6 = option.dp(6);
        {
            var title = new TextView(context);
            title.setText(I18n.get(name));
            title.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            title.setTextSize(14);

            var params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, 1);
            params.gravity = Gravity.CENTER_VERTICAL;
            option.addView(title, params);
        }
        {
            var spinner = new Spinner(context);
            spinner.setGravity(Gravity.END);
            spinner.setAdapter(new ArrayAdapter<>(context, values));
            spinner.setSelection(config.get().ordinal());
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    E newValue = values[position];
                    if (config.get() != newValue) {
                        config.set(newValue);
                        saveFn.run();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            var params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            params.gravity = Gravity.CENTER_VERTICAL;
            option.addView(spinner, params);
        }

        var params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        params.setMargins(dp6, 0, dp6, 0);
        option.setLayoutParams(params);

        return option;
    }

    @Nonnull
    public static LinearLayout createInputBox(Context context, String name) {
        var layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setHorizontalGravity(Gravity.START);

        final int dp3 = layout.dp(3);
        final int dp6 = layout.dp(6);
        {
            var title = new TextView(context);
            title.setText(I18n.get(name));
            title.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            title.setTextSize(14);

            var params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, 1);
            params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
            layout.addView(title, params);
        }
        {
            var input = new EditText(context);
            input.setId(R.id.input);
            input.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
            input.setTextSize(14);
            input.setPadding(dp3, 0, dp3, 0);

            ThemeControl.addBackground(input);

            var params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            params.gravity = Gravity.CENTER_VERTICAL;
            layout.addView(input, params);
        }

        var params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        params.setMargins(dp6, 0, dp6, 0);
        layout.setLayoutParams(params);

        return layout;
    }

    public static LinearLayout createInputBoxWithSlider(Context context, String name) {
        var layout = createInputBox(context, name);
        var slider = new SeekBar(context);
        slider.setId(R.id.button2);
        slider.setClickable(true);
        var params = new LinearLayout.LayoutParams(slider.dp(200), WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        layout.addView(slider, 1, params);
        return layout;
    }

    public static LinearLayout createIntegerOption(Context context, String name,
                                                   int minValue, int maxValue, int maxLength, int stepSize,
                                                   ForgeConfigSpec.IntValue config,
                                                   Runnable saveFn) {
        return createIntegerOption(context, name,
                minValue, maxValue, maxLength, stepSize,
                config, config::set, saveFn);
    }

    public static LinearLayout createIntegerOption(Context context, String name,
                                                   int minValue, int maxValue, int maxLength, int stepSize,
                                                   Supplier<Integer> getter, Consumer<Integer> setter,
                                                   Runnable saveFn) {
        var layout = createInputBoxWithSlider(context, name);
        var slider = layout.<SeekBar>requireViewById(R.id.button2);
        var input = layout.<EditText>requireViewById(R.id.input);
        input.setFilters(DigitsInputFilter.getInstance(input.getTextLocale()),
                new InputFilter.LengthFilter(maxLength));
        int curValue = getter.get();
        input.setText(Integer.toString(curValue));
        input.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                EditText v = (EditText) view;
                int newValue = MathUtil.clamp(Integer.parseInt(v.getText().toString()),
                        minValue, maxValue);
                v.setText(Integer.toString(newValue));
                if (newValue != getter.get()) {
                    setter.accept(newValue);
                    int curProgress = (newValue - minValue) / stepSize;
                    slider.setProgress(curProgress, true);
                    saveFn.run();
                }
            }
        });
        input.setMinWidth(slider.dp(50));
        int steps = (maxValue - minValue) / stepSize;
        slider.setMax(steps);
        slider.setProgress((curValue - minValue) / stepSize);
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int newValue = seekBar.getProgress() * stepSize + minValue;
                input.setText(Integer.toString(newValue));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int newValue = seekBar.getProgress() * stepSize + minValue;
                if (newValue != getter.get()) {
                    setter.accept(newValue);
                    input.setText(Integer.toString(newValue));
                    saveFn.run();
                }
            }
        });
        return layout;
    }

    public static LinearLayout createColorOpacityOption(
            Context context, String name,
            ForgeConfigSpec.ConfigValue<List<? extends String>> config,
            Runnable saveFn) {
        Supplier<Double> getter = () -> {
            List<? extends String> colors = config.get();
            if (colors != null && !colors.isEmpty()) {
                try {
                    return (double) ((Color.parseColor(colors.get(0)) >>> 24) / 255.0f);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
            return 1.0;
        };
        Consumer<Double> setter = (d) -> {
            int v = (int) (d * 255.0 + 0.5);
            var newList = new ArrayList<String>(config.get());
            if (newList.isEmpty()) {
                newList.add("#FF000000");
            }
            for (var it = newList.listIterator();
                 it.hasNext();
            ) {
                int color = Color.parseColor(it.next());
                color = color & 0xFFFFFF | (v << 24);
                if (v != 0) {
                    it.set(
                            '#' + Integer.toHexString(color)
                                    .toUpperCase(Locale.ROOT)
                    );
                } else {
                    it.set(
                            '#' + Integer.toHexString(0x1000000 | color).substring(1)
                                    .toUpperCase(Locale.ROOT)
                    );
                }
            }
            config.set(newList);
        };
        return createFloatOption(context, name, 0, 1, 4,
                getter, setter, 100, saveFn);
    }

    public static LinearLayout createFloatOption(Context context, String name,
                                                 float minValue, float maxValue, int maxLength,
                                                 ForgeConfigSpec.DoubleValue config,
                                                 Runnable saveFn) {
        return createFloatOption(context, name, minValue, maxValue, maxLength,
                config, config::set, 10, saveFn);
    }

    public static LinearLayout createFloatOption(Context context, String name,
                                                 float minValue, float maxValue, int maxLength,
                                                 Supplier<Double> getter, Consumer<Double> setter,
                                                 float denominator, // 10 means step=0.1, 100 means step=0.01
                                                 Runnable saveFn) {
        var layout = createInputBoxWithSlider(context, name);
        var slider = layout.<SeekBar>requireViewById(R.id.button2);
        var input = layout.<EditText>requireViewById(R.id.input);
        input.setFilters(DigitsInputFilter.getInstance(input.getTextLocale(), minValue < 0, true),
                new InputFilter.LengthFilter(maxLength));
        float curValue = getter.get().floatValue();
        input.setText(Float.toString(curValue));
        input.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                EditText v = (EditText) view;
                float newValue = MathUtil.clamp(Float.parseFloat(v.getText().toString()),
                        minValue, maxValue);
                v.setText(Float.toString(newValue));
                if (newValue != getter.get()) {
                    setter.accept((double) newValue);
                    int curProgress = (int) Math.round((newValue - minValue) * denominator);
                    slider.setProgress(curProgress, true);
                    saveFn.run();
                }
            }
        });
        input.setMinWidth(slider.dp(50));
        int steps = (int) Math.round((maxValue - minValue) * denominator);
        slider.setMax(steps);
        int curProgress = (int) Math.round((curValue - minValue) * denominator);
        slider.setProgress(curProgress);
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double newValue = seekBar.getProgress() / denominator + minValue;
                input.setText(Float.toString((float) newValue));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                double newValue = seekBar.getProgress() / denominator + minValue;
                if (newValue != getter.get()) {
                    setter.accept((double) (float) newValue);
                    input.setText(Float.toString((float) newValue));
                    saveFn.run();
                }
            }
        });
        return layout;
    }

    public static class TooltipBorderCollapsed implements View.OnClickListener {

        public static final String[][] PRESET_COLORS = {
                {"#F0AADCF0", "#F0FFC3F7", "#F0BFF2B2", "#F0D27F3D"},
                {"#F0AADCF0", "#F0DAD0F4", "#F0FFC3F7", "#F0DAD0F4"},
                {"#F028007F", "#F028007F", "#F014003F", "#F014003F"},
                {"#F0606060", "#F0101010", "#F0FFFFFF", "#F0B0B0B0"}
        };

        final ViewGroup mParent;
        final Runnable mSaveFn;

        // lazy-init
        LinearLayout mContent;
        FourColorPicker mColorPicker;

        // this callback is registered on a child view of 'parent'
        // so no weak ref
        public TooltipBorderCollapsed(ViewGroup parent, Runnable saveFn) {
            mParent = parent;
            mSaveFn = saveFn;
        }

        @Override
        public void onClick(View v) {
            if (mContent != null) {
                // toggle
                mContent.setVisibility(mContent.getVisibility() == View.GONE
                        ? View.VISIBLE
                        : View.GONE);
                return;
            }
            mContent = new LinearLayout(mParent.getContext());
            mContent.setOrientation(LinearLayout.VERTICAL);
            {
                var option = createIntegerOption(mParent.getContext(), "modernui.center.extension.tooltipBorderCycle",
                        Config.Client.TOOLTIP_BORDER_COLOR_ANIM_MIN, Config.Client.TOOLTIP_BORDER_COLOR_ANIM_MAX,
                        4, 100, Config.CLIENT.mTooltipCycle, mSaveFn);
                option.setTooltipText(I18n.get("modernui.center.extension.tooltipBorderCycle_desc"));
                mContent.addView(option);
            }
            var params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            params.setMargins(0, mContent.dp(6), 0, 0);
            {
                var buttonGroup = new LinearLayout(mParent.getContext());
                buttonGroup.setOrientation(LinearLayout.HORIZONTAL);
                for (int i = 0; i < 4; i++) {
                    var button = new Button(mParent.getContext());
                    button.setText(I18n.get("gui.modernui.preset_s", (i + 1)));
                    final int idx = i;
                    button.setOnClickListener((__) -> mColorPicker.setColors(
                            PRESET_COLORS[idx])
                    );
                    var p = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, 1);
                    buttonGroup.addView(button, p);
                }
                mContent.addView(buttonGroup, new LinearLayout.LayoutParams(params));
            }
            mContent.addView(mColorPicker = new FourColorPicker(mParent.getContext(),
                    Config.CLIENT.mTooltipStroke,
                    mSaveFn), new LinearLayout.LayoutParams(params));
            mParent.addView(mContent, params);
        }
    }
}
