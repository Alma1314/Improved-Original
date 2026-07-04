// ModernUI Fragment 实现的每日任务面板
// 替代原手绘 QuestScreen，使用 ModernUI 的 ViewGroup/TextView 体系
package com.alma.improved_original.quest.screen;

import com.alma.improved_original.Config;
import com.alma.improved_original.quest.QuestData;
import com.alma.improved_original.quest.QuestDefinition;
import com.alma.improved_original.quest.QuestSlotData;
import com.alma.improved_original.quest.client.ClientQuestCache;
import com.alma.improved_original.quest.network.C2SQuestLockPayload;
import com.alma.improved_original.quest.network.C2SQuestRefreshPayload;
import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.annotation.Nullable;
import icyllis.modernui.core.Context;
import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.mc.MuiModApi;
import icyllis.modernui.mc.ScreenCallback;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.view.*;
import icyllis.modernui.widget.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class QuestFragment extends Fragment implements ScreenCallback {

    // 引用 ModernUIHelper 中的统一颜色常量
    private static final int BG_COLOR = ModernUIHelper.COLOR_BG;
    private static final int HEADER_COLOR = ModernUIHelper.COLOR_HEADER;
    private static final int GOLD = ModernUIHelper.COLOR_GOLD;
    private static final int ACCENT = ModernUIHelper.COLOR_ACCENT;
    private static final int GREEN = ModernUIHelper.COLOR_GREEN;
    private static final int GRAY = ModernUIHelper.COLOR_GRAY;
    private static final int WHITE = ModernUIHelper.COLOR_WHITE;

    private QuestData questData;
    private TextView countdownText;

    public QuestFragment() {
        QuestData latest = ClientQuestCache.get();
        this.questData = latest != null ? latest : QuestData.createFresh();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable DataSet savedInstanceState) {
        QuestData latest = ClientQuestCache.get();
        if (latest != null) this.questData = latest;

        Context ctx = requireContext();

        // 面板容器
        LinearLayout panel = new LinearLayout(ctx);
        panel.setOrientation(LinearLayout.VERTICAL);

        // 面板圆角背景
        ShapeDrawable panelBg = new ShapeDrawable();
        panelBg.setCornerRadius(dp(panel, 8));
        panelBg.setColor(BG_COLOR);
        panel.setBackground(panelBg);

        int panelW = dp(panel, 280);
        panel.setLayoutParams(new FrameLayout.LayoutParams(panelW, dp(panel, 220), Gravity.CENTER));

        // ---- 标题栏 ----
        panel.addView(buildHeader(ctx, panel));

        // ---- 3个任务槽位 ----
        panel.addView(buildSlots(ctx, panel));

        // ---- 底部按钮 ----
        panel.addView(buildFooter(ctx, panel));

        return panel;
    }

    private View buildHeader(Context ctx, View ref) {
        LinearLayout header = new LinearLayout(ctx);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setGravity(Gravity.CENTER);

        ShapeDrawable headerBg = new ShapeDrawable();
        headerBg.setCornerRadius(dp(ref, 8));
        headerBg.setColor(HEADER_COLOR);
        header.setBackground(headerBg);

        int headerW = dp(ref, 280);
        int headerH = dp(ref, 40);

        TextView title = new TextView(ctx);
        title.setText(Component.translatable("screen.improved_original.quest").getString());
        title.setTextSize(14);
        title.setTextColor(GOLD);
        title.setGravity(Gravity.CENTER);
        header.addView(title, new LinearLayout.LayoutParams(headerW, dp(ref, 18)));

        countdownText = new TextView(ctx);
        countdownText.setTextSize(11);
        countdownText.setTextColor(GRAY);
        countdownText.setGravity(Gravity.CENTER);
        updateCountdown(ref);
        header.addView(countdownText, new LinearLayout.LayoutParams(headerW, dp(ref, 14)));

        header.setLayoutParams(new LinearLayout.LayoutParams(headerW, headerH));
        return header;
    }

    private View buildSlots(Context ctx, View ref) {
        LinearLayout slotsLayout = new LinearLayout(ctx);
        slotsLayout.setOrientation(LinearLayout.VERTICAL);
        int panelW = dp(ref, 280);
        int slotH = dp(ref, 46);

        for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
            if (i > 0) {
                View sep = new View(ctx);
                sep.setLayoutParams(new LinearLayout.LayoutParams(panelW, dp(ref, 1)));
                ShapeDrawable sepBg = new ShapeDrawable();
                sepBg.setColor(0xFF444444);
                sep.setBackground(sepBg);
                slotsLayout.addView(sep);
            }
            slotsLayout.addView(buildSlot(ctx, ref, i, panelW, slotH));
        }

        slotsLayout.setLayoutParams(new LinearLayout.LayoutParams(panelW, dp(ref, 140)));
        return slotsLayout;
    }

    private View buildSlot(Context ctx, View ref, int slotIndex, int panelW, int slotH) {
        var qo = questData.getQuest(slotIndex);
        boolean hasQuest = qo.isPresent();
        boolean done = hasQuest && questData.getSlot(slotIndex).isComplete();

        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(ref, 4), dp(ref, 2), dp(ref, 2), dp(ref, 2));

        int contentW = panelW * 3 / 4;

        if (hasQuest) {
            QuestDefinition q = qo.get();
            QuestSlotData sl = questData.getSlot(slotIndex);
            List<Integer> pt = sl.perTargetProgress();

            LinearLayout content = new LinearLayout(ctx);
            content.setOrientation(LinearLayout.VERTICAL);

            // 任务名 + 锁定标记
            String nameStr = "[" + (slotIndex + 1) + "] ";
            if (q.name() != null && !q.name().isEmpty()) {
                nameStr += Component.translatable(q.name()).getString();
            }
            if (questData.isSlotLocked(slotIndex)) {
                nameStr += " " + Component.translatable("quest.improved_original.locked").getString();
            }
            TextView name = new TextView(ctx);
            name.setText(nameStr);
            name.setTextSize(10);
            name.setTextColor(done ? GREEN : questData.isSlotLocked(slotIndex) ? 0xFFFF5555 : WHITE);
            content.addView(name, new LinearLayout.LayoutParams(contentW, ViewGroup.LayoutParams.WRAP_CONTENT));

            // 进度条 (每个target一个)
            int barW = dp(ref, 50);
            for (int t = 0; t < q.targets().size(); t++) {
                QuestDefinition.QuestTarget tg = q.targets().get(t);
                int prog = t < pt.size() ? pt.get(t) : 0;
                int max = tg.count();
                boolean tc = prog >= max;

                LinearLayout barRow = new LinearLayout(ctx);
                barRow.setOrientation(LinearLayout.HORIZONTAL);
                barRow.setGravity(Gravity.CENTER_VERTICAL);

                TextView targetLabel = new TextView(ctx);
                targetLabel.setText(q.getTargetDisplayName(tg.type(), tg.item()).getString());
                targetLabel.setTextSize(8);
                targetLabel.setTextColor(0xFFCCCCCC);
                barRow.addView(targetLabel, new LinearLayout.LayoutParams(dp(ref, 56), ViewGroup.LayoutParams.WRAP_CONTENT));

                // 进度条
                FrameLayout barFrame = new FrameLayout(ctx);
                barFrame.setLayoutParams(new LinearLayout.LayoutParams(barW, dp(ref, 5)));

                View barBg = new View(ctx);
                ShapeDrawable sd1 = new ShapeDrawable();
                sd1.setColor(0xFF333333);
                barBg.setBackground(sd1);
                barFrame.addView(barBg, new FrameLayout.LayoutParams(barW, dp(ref, 5)));

                if (max > 0) {
                    int fillW = (int) ((float) prog / max * barW);
                    if (fillW > 0) {
                        View barFg = new View(ctx);
                        ShapeDrawable sd2 = new ShapeDrawable();
                        sd2.setColor(tc ? GREEN : ACCENT);
                        barFg.setBackground(sd2);
                        barFrame.addView(barFg, new FrameLayout.LayoutParams(fillW, dp(ref, 5)));
                    }
                }
                barRow.addView(barFrame);

                TextView count = new TextView(ctx);
                count.setText(prog + "/" + max);
                count.setTextSize(7);
                count.setTextColor(GRAY);
                barRow.addView(count, new LinearLayout.LayoutParams(dp(ref, 24), ViewGroup.LayoutParams.WRAP_CONTENT));

                content.addView(barRow, new LinearLayout.LayoutParams(contentW, ViewGroup.LayoutParams.WRAP_CONTENT));
            }

            // 奖励列表
            if (!q.rewards().isEmpty()) {
                var rn = q.getRewardDisplayNames();
                LinearLayout rewardRow = new LinearLayout(ctx);
                rewardRow.setOrientation(LinearLayout.HORIZONTAL);
                StringBuilder sb = new StringBuilder();
                for (int r = 0; r < q.rewards().size(); r++) {
                    if (r > 0) sb.append(", ");
                    sb.append(Component.translatable("quest.improved_original.reward", q.rewards().get(r).count(), rn.get(r)).getString());
                }
                TextView reward = new TextView(ctx);
                reward.setText(sb.toString());
                reward.setTextSize(8);
                reward.setTextColor(done ? GREEN : GOLD);
                content.addView(reward, new LinearLayout.LayoutParams(contentW, ViewGroup.LayoutParams.WRAP_CONTENT));
            }

            row.addView(content, new LinearLayout.LayoutParams(contentW, ViewGroup.LayoutParams.WRAP_CONTENT));
        } else {
            TextView empty = new TextView(ctx);
            empty.setText(Component.translatable("quest.improved_original.empty_slot").getString());
            empty.setTextSize(10);
            empty.setTextColor(0xFF666666);
            row.addView(empty, new LinearLayout.LayoutParams(contentW, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        // 右1/4: 锁定按钮
        if (hasQuest && !done) {
            int btnW = panelW / 4 - dp(ref, 12);
            String label = questData.isSlotLocked(slotIndex)
                    ? Component.translatable("quest.improved_original.locked").getString()
                    : Component.translatable("quest.improved_original.lock_button").getString();
            TextView lockBtn = new TextView(ctx);
            lockBtn.setText(label);
            lockBtn.setTextSize(9);
            lockBtn.setTextColor(GRAY);
            lockBtn.setGravity(Gravity.CENTER);
            lockBtn.setClickable(true);
            int si = slotIndex;
            lockBtn.setOnClickListener(v -> PacketDistributor.sendToServer(new C2SQuestLockPayload(si)));

            ShapeDrawable lockBg = new ShapeDrawable();
            lockBg.setCornerRadius(dp(ref, 4));
            lockBg.setColor(0x66333333);
            lockBtn.setBackground(lockBg);

            row.addView(lockBtn, new LinearLayout.LayoutParams(btnW, dp(ref, 18)));
        }

        row.setLayoutParams(new LinearLayout.LayoutParams(panelW, slotH));
        return row;
    }

    private View buildFooter(Context ctx, View ref) {
        LinearLayout footer = new LinearLayout(ctx);
        footer.setOrientation(LinearLayout.HORIZONTAL);
        footer.setGravity(Gravity.CENTER);

        ShapeDrawable footerBg = new ShapeDrawable();
        footerBg.setCornerRadius(dp(ref, 8));
        footerBg.setColor(HEADER_COLOR);
        footer.setBackground(footerBg);

        int footerH = dp(ref, 40);
        int panelW = dp(ref, 280);

        // 刷新按钮
        int refreshCost = Config.EMERALD_REFRESH_COST.getAsInt();
        TextView refreshBtn = buildButton(ctx, ref,
                Component.translatable("quest.improved_original.refresh_button", refreshCost).getString(),
                () -> PacketDistributor.sendToServer(new C2SQuestRefreshPayload()));

        // 关闭按钮 — 必须在 Minecraft Render 线程上关闭 Screen
        // ModernUI 的 onClick 回调运行在 UI thread，需要线程切换
        TextView closeBtn = buildButton(ctx, ref,
                Component.translatable("quest.improved_original.done").getString(),
                () -> Minecraft.getInstance().execute(() -> {
                    var screen = Minecraft.getInstance().screen;
                    if (screen != null) screen.onClose();
                }));

        footer.addView(refreshBtn, new LinearLayout.LayoutParams(dp(ref, 110), dp(ref, 22)));
        footer.addView(closeBtn, new LinearLayout.LayoutParams(dp(ref, 50), dp(ref, 22)));

        footer.setLayoutParams(new LinearLayout.LayoutParams(panelW, footerH));
        return footer;
    }

    private TextView buildButton(Context ctx, View ref, String text, Runnable onClick) {
        TextView btn = new TextView(ctx);
        btn.setText(text);
        btn.setTextSize(11);
        btn.setTextColor(0xFFCCCCCC);
        btn.setGravity(Gravity.CENTER);
        btn.setClickable(true);

        ShapeDrawable btnBg = new ShapeDrawable();
        btnBg.setCornerRadius(dp(ref, 4));
        btnBg.setColor(0xFF555555);
        btn.setBackground(btnBg);

        btn.setOnClickListener(v -> onClick.run());
        return btn;
    }

    private void updateCountdown(View ref) {
        if (countdownText == null || Minecraft.getInstance().level == null) return;
        long tick = Minecraft.getInstance().level.getGameTime();
        long iv = 20L * 60 * Config.QUEST_REFRESH_INTERVAL_MINUTES.getAsInt();
        if (iv <= 0) iv = 1;
        long sec = (iv - tick % iv) / 20;
        countdownText.setText(
                Component.translatable("quest.improved_original.countdown", sec / 60, sec % 60).getString());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable DataSet savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.postDelayed(this::tickCountdown, 1000);
    }

    private void tickCountdown() {
        View view = getView();
        if (view != null && isAdded()) {
            updateCountdown(view);
            view.postDelayed(this::tickCountdown, 1000);
        }
    }

    // 工具：用 View 获取 dp 值
    private static int dp(View ref, int dp) {
        return ref.dp(dp);
    }

    // ---- ScreenCallback ----
    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public boolean shouldClose() { return true; }

    @Override
    public boolean hasDefaultBackground() { return false; }
}
