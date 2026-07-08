// 链进度面板：展示所有活跃链的步骤树 + 已完成链列表
// 从客户端缓存读取 QuestData，获取 activeChains 和各链槽位信息
// 独立 Fragment，从主面板的 "View All Chains" 按钮打开
package com.alma.improved_original.quest.screen;

import com.alma.improved_original.quest.QuestData;
import com.alma.improved_original.quest.QuestData.StepInfo;
import com.alma.improved_original.quest.QuestData.StepStatus;
import com.alma.improved_original.quest.client.ClientQuestCache;
import com.alma.improved_original.quest.component.Rarity;
import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.annotation.Nullable;
import icyllis.modernui.core.Context;
import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.mc.ScreenCallback;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.view.*;
import icyllis.modernui.widget.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;

public class ChainProgressFragment extends Fragment implements ScreenCallback {

    private final List<QuestData.ChainProgress> completedChains;

    public ChainProgressFragment(List<QuestData.ChainProgress> completedChains) {
        this.completedChains = completedChains != null ? completedChains : List.of();
    }

    // Keep the no-arg constructor for backward compat
    public ChainProgressFragment() {
        this.completedChains = List.of();
    }

    private static final int BG_COLOR = ModernUIHelper.COLOR_BG;
    private static final int GOLD = ModernUIHelper.COLOR_GOLD;
    private static final int GREEN = ModernUIHelper.COLOR_GREEN;
    private static final int GRAY = ModernUIHelper.COLOR_GRAY;
    private static final int WHITE = ModernUIHelper.COLOR_WHITE;
    private static final int HEADER_COLOR = ModernUIHelper.COLOR_HEADER;

    private static final float PANEL_WIDTH_RATIO = 0.60f;
    private static final float PANEL_HEIGHT_RATIO = 0.75f;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable DataSet savedInstanceState) {
        Context ctx = requireContext();
        QuestData questData = ClientQuestCache.get();
        if (questData == null) questData = QuestData.createFresh();

        var window = Minecraft.getInstance().getWindow();
        int panelW = (int) (window.getWidth() * PANEL_WIDTH_RATIO);
        int panelH = (int) (window.getHeight() * PANEL_HEIGHT_RATIO);

        LinearLayout panel = new LinearLayout(ctx);
        panel.setOrientation(LinearLayout.VERTICAL);

        int padPx = dpPx(ctx, 16);
        panel.setPadding(padPx, padPx, padPx, padPx);

        ShapeDrawable panelBg = new ShapeDrawable();
        panelBg.setCornerRadius(dpPx(ctx, 8));
        panelBg.setColor(BG_COLOR);
        panel.setBackground(panelBg);

        panel.setLayoutParams(new FrameLayout.LayoutParams(panelW, panelH, Gravity.CENTER));

        // Header
        View header = buildHeader(ctx, panelW);
        panel.addView(header);

        // Scrollable content
        ScrollView scrollView = new ScrollView(ctx);
        LinearLayout content = buildContent(ctx, questData, panelW - padPx * 2);
        scrollView.addView(content);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        panel.addView(scrollView);

        // Footer with close button
        panel.addView(buildFooter(ctx, panelW));

        return panel;
    }

    private View buildHeader(Context ctx, int panelW) {
        TextView title = new TextView(ctx);
        title.setText(Component.translatable("screen.improved_original.chain_progress").getString());
        title.setTextSize(21);
        title.setTextColor(GOLD);
        title.setGravity(Gravity.CENTER);
        int pad = dpPx(ctx, 8);
        LinearLayout header = new LinearLayout(ctx);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(pad, pad, pad, pad);

        ShapeDrawable headerBg = new ShapeDrawable();
        headerBg.setCornerRadius(dpPx(ctx, 8));
        headerBg.setColor(HEADER_COLOR);
        header.setBackground(headerBg);

        header.addView(title, new LinearLayout.LayoutParams(panelW, ViewGroup.LayoutParams.WRAP_CONTENT));
        header.setLayoutParams(new LinearLayout.LayoutParams(panelW, dpPx(ctx, 50)));
        return header;
    }

    private LinearLayout buildContent(Context ctx, QuestData data, int contentW) {
        LinearLayout content = new LinearLayout(ctx);
        content.setOrientation(LinearLayout.VERTICAL);

        // Active chains section
        TextView activeTitle = new TextView(ctx);
        activeTitle.setText(Component.translatable("quest.improved_original.chains.active").getString());
        activeTitle.setTextSize(16);
        activeTitle.setTextColor(GOLD);
        content.addView(activeTitle);

        Map<String, QuestData.ChainProgress> chains = data.getActiveChains();
        if (chains.isEmpty()) {
            TextView empty = new TextView(ctx);
            empty.setText(Component.translatable("quest.improved_original.chains.empty").getString());
            empty.setTextSize(14);
            empty.setTextColor(GRAY);
            content.addView(empty);
        } else {
            for (var entry : chains.entrySet()) {
                QuestData.ChainProgress progress = entry.getValue();
                content.addView(buildChainHeader(ctx, progress, contentW));
                // 渲染步骤树
                List<StepInfo> steps = progress.steps();
                if (steps != null && !steps.isEmpty()) {
                    for (int i = 0; i < steps.size(); i++) {
                        content.addView(buildStepRow(ctx, steps.get(i), i == steps.size() - 1, contentW));
                    }
                }
            }
        }

        // Spacer
        View spacer = new View(ctx);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(contentW, dpPx(ctx, 16)));
        content.addView(spacer);

        // Completed chains section
        TextView completedTitle = new TextView(ctx);
        completedTitle.setText(Component.translatable("quest.improved_original.chains.completed").getString());
        completedTitle.setTextSize(16);
        completedTitle.setTextColor(GRAY);
        content.addView(completedTitle);

        if (completedChains.isEmpty()) {
            TextView completedEmpty = new TextView(ctx);
            completedEmpty.setText(Component.translatable("quest.improved_original.chains.no_completed").getString());
            completedEmpty.setTextSize(13);
            completedEmpty.setTextColor(GRAY);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    contentW, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(dpPx(ctx, 16), dpPx(ctx, 4), 0, 0);
            content.addView(completedEmpty, lp);
        } else {
            for (QuestData.ChainProgress cp : completedChains) {
                TextView item = new TextView(ctx);
                item.setText("\u2714 " + cp.chainId() + "  " + cp.totalStepCount() + "/" + cp.totalStepCount() + "  Done");
                item.setTextSize(13);
                item.setTextColor(ModernUIHelper.COLOR_GREEN);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        contentW, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMargins(dpPx(ctx, 16), dpPx(ctx, 4), 0, 0);
                content.addView(item, lp);
            }
        }

        return content;
    }

    private View buildChainHeader(Context ctx, QuestData.ChainProgress progress, int contentW) {
        StringBuilder sb = new StringBuilder();
        sb.append("\u25B6 "); // ▶
        sb.append(progress.chainId());
        sb.append("  ").append(progress.completedStepCount())
                .append("/").append(progress.totalStepCount());
        sb.append("  Active");

        TextView header = new TextView(ctx);
        header.setText(sb.toString());
        header.setTextSize(14);
        header.setTextColor(WHITE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                contentW, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dpPx(ctx, 16), dpPx(ctx, 4), 0, dpPx(ctx, 4));
        header.setLayoutParams(lp);
        return header;
    }

    private View buildStepRow(Context ctx, StepInfo step, boolean isLast, int contentW) {
        String icon;
        int color;
        switch (step.status()) {
            case DONE:
                icon = "\u2714 "; // ✅ checkmark
                color = ModernUIHelper.COLOR_GREEN;
                break;
            case ACTIVE:
                icon = "\u25B6 "; // ▶ play
                color = ModernUIHelper.COLOR_GOLD;
                break;
            default:
                icon = "\u23F3 "; // ⏳ hourglass
                color = ModernUIHelper.COLOR_GRAY;
                break;
        }

        String prefix = isLast ? "  \u2514 " : "  \u251C "; // └─ or ├─

        TextView row = new TextView(ctx);
        row.setText(prefix + icon + step.questId());
        row.setTextSize(12);
        row.setTextColor(color);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                contentW, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dpPx(ctx, 32), dpPx(ctx, 1), 0, dpPx(ctx, 1));
        row.setLayoutParams(lp);
        return row;
    }

    private View buildFooter(Context ctx, int panelW) {
        TextView closeBtn = new TextView(ctx);
        closeBtn.setText(Component.translatable("quest.improved_original.done").getString());
        closeBtn.setTextSize(15);
        closeBtn.setTextColor(0xFFCCCCCC);
        closeBtn.setGravity(Gravity.CENTER);
        closeBtn.setClickable(true);

        ShapeDrawable btnBg = new ShapeDrawable();
        btnBg.setCornerRadius(dpPx(ctx, 4));
        btnBg.setColor(0xFF555555);
        closeBtn.setBackground(btnBg);

        closeBtn.setOnClickListener(v -> Minecraft.getInstance().execute(() -> {
            var screen = Minecraft.getInstance().screen;
            if (screen != null) screen.onClose();
        }));

        LinearLayout footer = new LinearLayout(ctx);
        footer.setOrientation(LinearLayout.HORIZONTAL);
        footer.setGravity(Gravity.CENTER);
        int pad = dpPx(ctx, 8);
        footer.setPadding(pad, 0, pad, 0);

        ShapeDrawable footerBg = new ShapeDrawable();
        footerBg.setCornerRadius(dpPx(ctx, 8));
        footerBg.setColor(HEADER_COLOR);
        footer.setBackground(footerBg);

        footer.addView(closeBtn, new LinearLayout.LayoutParams(
                dpPx(ctx, 100), dpPx(ctx, 40)));
        footer.setLayoutParams(new LinearLayout.LayoutParams(panelW, dpPx(ctx, 60)));
        return footer;
    }

    private int dpPx(Context ctx, int dp) {
        return (int) (dp * ctx.getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public boolean isPauseScreen() { return false; }
    @Override
    public boolean shouldClose() { return true; }
    @Override
    public boolean hasDefaultBackground() { return false; }
}
