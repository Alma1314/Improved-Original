// ModernUI Fragment 实现的每日任务面板
// 面板尺寸按窗口固定比例，头部和底部固定，任务区域可滚动
// 增量刷新：数据变化时直接更新对应 View（文字、进度条宽度、颜色），不再重建整个 Fragment
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

import java.util.ArrayList;
import java.util.List;

public class QuestFragment extends Fragment implements ScreenCallback {

    private static final int BG_COLOR = ModernUIHelper.COLOR_BG;
    private static final int HEADER_COLOR = ModernUIHelper.COLOR_HEADER;
    private static final int GOLD = ModernUIHelper.COLOR_GOLD;
    private static final int ACCENT = ModernUIHelper.COLOR_ACCENT;
    private static final int GREEN = ModernUIHelper.COLOR_GREEN;
    private static final int GRAY = ModernUIHelper.COLOR_GRAY;
    private static final int WHITE = ModernUIHelper.COLOR_WHITE;

    // 面板占窗口比例
    private static final float PANEL_WIDTH_RATIO = 0.70f;
    private static final float PANEL_HEIGHT_RATIO = 0.80f;

    // 间距
    private static final int PANEL_PADDING_DP = 16;
    private static final int CONTENT_GAP_DP = 8;
    private static final int SECTION_GAP_DP = 12;

    private QuestData questData;
    private long lastKnownVersion; // 用于检测数据变化

    // 缓存的 View 引用，用于增量刷新
    private TextView countdownText;
    private final TextView[] slotNameTexts = new TextView[QuestData.SLOT_COUNT];
    // 每个槽位每个目标的 {barFg View, count TextView}
    private final View[][] slotBarFgs = new View[QuestData.SLOT_COUNT][];
    private final TextView[][] slotBarCounts = new TextView[QuestData.SLOT_COUNT][];
    private final TextView[] slotRewardTexts = new TextView[QuestData.SLOT_COUNT];
    private final TextView[] slotLockButtons = new TextView[QuestData.SLOT_COUNT];
    private final View[] slotLockBtnContainers = new View[QuestData.SLOT_COUNT]; // lockBtn 的父容器（整个slot row），用于显示/隐藏

    // 缓存的计算值
    private int barW; // 进度条像素宽度

    // 防抖：pending refresh 标记
    private boolean refreshPending;
    private final Runnable doRefresh = this::refreshUI;

    public QuestFragment() {
        QuestData latest = ClientQuestCache.get();
        this.questData = latest != null ? latest : QuestData.createFresh();
        this.lastKnownVersion = ClientQuestCache.getVersion();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable DataSet savedInstanceState) {
        QuestData latest = ClientQuestCache.get();
        if (latest != null) {
            this.questData = latest;
            this.lastKnownVersion = ClientQuestCache.getVersion();
        }

        Context ctx = requireContext();
        this.barW = dpPx(ctx, 120); // 进度条宽度从 100dp 提升到 120dp

        LinearLayout panel = new LinearLayout(ctx);
        panel.setOrientation(LinearLayout.VERTICAL);

        // 用窗口实际像素 × 比例 = 面板物理像素
        var window = Minecraft.getInstance().getWindow();
        int panelW = (int) (window.getWidth() * PANEL_WIDTH_RATIO);
        int panelH = (int) (window.getHeight() * PANEL_HEIGHT_RATIO);
        int padPx = dpPx(ctx, PANEL_PADDING_DP);

        panel.setPadding(padPx, padPx, padPx, padPx);

        ShapeDrawable panelBg = new ShapeDrawable();
        panelBg.setCornerRadius(dpPx(ctx, 8));
        panelBg.setColor(BG_COLOR);
        panel.setBackground(panelBg);

        panel.setLayoutParams(new FrameLayout.LayoutParams(panelW, panelH, Gravity.CENTER));

        // 头部（固定）
        View header = buildHeader(ctx, panelW);
        panel.addView(header);

        // 分隔线
        panel.addView(buildSeparator(ctx, panelW));

        // 任务区域（填充剩余空间，超出滚动）
        View slotsContent = buildSlots(ctx, panelW, panelW - padPx * 2);
        ScrollView scrollView = new ScrollView(ctx);
        scrollView.addView(slotsContent);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        panel.addView(scrollView);

        // 分隔线
        panel.addView(buildSeparator(ctx, panelW));

        // 底部（固定）
        panel.addView(buildFooter(ctx, panelW));

        return panel;
    }

    // dp 辅助（需要 Context 的版本，onCreateView 期间使用）
    private int dpPx(Context ctx, int dp) {
        return (int) (dp * ctx.getResources().getDisplayMetrics().density + 0.5f);
    }

    private View buildHeader(Context ctx, int panelW) {
        int headerH = dpPx(ctx, 68);

        LinearLayout header = new LinearLayout(ctx);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setGravity(Gravity.CENTER);
        int pad = dpPx(ctx, CONTENT_GAP_DP);
        header.setPadding(pad, pad, pad, pad);

        ShapeDrawable headerBg = new ShapeDrawable();
        headerBg.setCornerRadius(dpPx(ctx, 8));
        headerBg.setColor(HEADER_COLOR);
        header.setBackground(headerBg);

        int contentW = panelW - dpPx(ctx, PANEL_PADDING_DP) * 2;

        TextView title = new TextView(ctx);
        title.setText(Component.translatable("screen.improved_original.quest").getString());
        title.setTextSize(21);
        title.setTextColor(GOLD);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(contentW, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, dpPx(ctx, 4));
        header.addView(title, titleParams);

        countdownText = new TextView(ctx);
        countdownText.setTextSize(15);
        countdownText.setTextColor(GRAY);
        countdownText.setGravity(Gravity.CENTER);
        updateCountdownText();
        header.addView(countdownText, new LinearLayout.LayoutParams(contentW, ViewGroup.LayoutParams.WRAP_CONTENT));

        header.setLayoutParams(new LinearLayout.LayoutParams(panelW, headerH));
        return header;
    }

    private View buildSeparator(Context ctx, int width) {
        View sep = new View(ctx);
        sep.setLayoutParams(new LinearLayout.LayoutParams(width, 1));
        ShapeDrawable sepBg = new ShapeDrawable();
        sepBg.setColor(0xFF444444);
        sep.setBackground(sepBg);
        return sep;
    }

    private View buildSlots(Context ctx, int panelW, int contentW) {
        LinearLayout slotsLayout = new LinearLayout(ctx);
        slotsLayout.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
            if (i > 0) {
                slotsLayout.addView(buildSeparator(ctx, contentW));
            }
            slotsLayout.addView(buildSlot(ctx, i, contentW));
        }

        slotsLayout.setLayoutParams(new LinearLayout.LayoutParams(contentW, ViewGroup.LayoutParams.WRAP_CONTENT));
        return slotsLayout;
    }

    private LinearLayout buildSlot(Context ctx, int slotIndex, int panelW) {
        var qo = questData.getQuest(slotIndex);
        boolean hasQuest = qo.isPresent();
        boolean done = hasQuest && questData.getSlot(slotIndex).isComplete();

        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int gap = dpPx(ctx, CONTENT_GAP_DP);
        row.setPadding(gap, dpPx(ctx, 8), gap, dpPx(ctx, 8));

        int contentW = panelW * 3 / 4;

        if (hasQuest) {
            row.addView(buildQuestContent(ctx, qo.get(), slotIndex, done, contentW),
                    new LinearLayout.LayoutParams(contentW, ViewGroup.LayoutParams.WRAP_CONTENT));
        } else {
            row.addView(buildEmptySlot(ctx),
                    new LinearLayout.LayoutParams(contentW, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        // lock button area
        if (hasQuest && !done) {
            row.addView(buildLockButton(ctx, slotIndex, panelW));
        } else {
            // 占位：保存 null 不会取用
            slotLockBtnContainers[slotIndex] = null;
        }

        row.setLayoutParams(new LinearLayout.LayoutParams(panelW, ViewGroup.LayoutParams.WRAP_CONTENT));
        return row;
    }

    private View buildQuestContent(Context ctx, QuestDefinition q, int slotIndex, boolean done, int contentW) {
        LinearLayout content = new LinearLayout(ctx);
        content.setOrientation(LinearLayout.VERTICAL);

        // 任务名称
        TextView nameView = buildQuestName(ctx, q, slotIndex, done);
        slotNameTexts[slotIndex] = nameView;
        content.addView(nameView, new LinearLayout.LayoutParams(contentW, ViewGroup.LayoutParams.WRAP_CONTENT));

        // 进度条
        QuestSlotData sl = questData.getSlot(slotIndex);
        int targetCount = q.targets().size();
        slotBarFgs[slotIndex] = new View[targetCount];
        slotBarCounts[slotIndex] = new TextView[targetCount];

        for (int t = 0; t < targetCount; t++) {
            int prog = t < sl.perTargetProgress().size() ? sl.perTargetProgress().get(t) : 0;
            View barRow = buildProgressBar(ctx, q, t, prog, slotIndex);
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(contentW, ViewGroup.LayoutParams.WRAP_CONTENT);
            barParams.setMargins(0, dpPx(ctx, 3), 0, dpPx(ctx, 3));
            content.addView(barRow, barParams);
        }

        // 奖励文字
        if (!q.rewards().isEmpty()) {
            TextView rewardView = buildRewardText(ctx, q, done);
            slotRewardTexts[slotIndex] = rewardView;
            LinearLayout.LayoutParams rewardParams = new LinearLayout.LayoutParams(contentW, ViewGroup.LayoutParams.WRAP_CONTENT);
            rewardParams.setMargins(0, dpPx(ctx, CONTENT_GAP_DP), 0, 0);
            content.addView(rewardView, rewardParams);
        } else {
            slotRewardTexts[slotIndex] = null;
        }

        return content;
    }

    private TextView buildQuestName(Context ctx, QuestDefinition q, int slotIndex, boolean done) {
        boolean locked = questData.isSlotLocked(slotIndex);
        int color;
        if (done) {
            color = GREEN;
        } else if (locked) {
            color = 0xFFFF5555;
        } else {
            color = WHITE;
        }

        StringBuilder sb = new StringBuilder("[");
        sb.append(slotIndex + 1).append("] ");
        if (q.name() != null && !q.name().isEmpty()) {
            sb.append(Component.translatable(q.name()).getString());
        }
        if (locked) {
            sb.append(" ").append(Component.translatable("quest.improved_original.locked").getString());
        }

        TextView name = new TextView(ctx);
        name.setText(sb.toString());
        name.setTextSize(15);
        name.setTextColor(color);
        return name;
    }

    /**
     * 构建单条进度条行。
     * 返回的 View 是 barRow (LinearLayout，HORIZONTAL)。
     * 内部 fill View 和 count TextView 的引用缓存在 slotBarFgs[slotIndex][targetIndex] 和 slotBarCounts[slotIndex][targetIndex] 中。
     */
    private View buildProgressBar(Context ctx, QuestDefinition q, int targetIndex, int prog, int slotIndex) {
        QuestDefinition.QuestTarget tg = q.targets().get(targetIndex);
        int max = tg.count();
        int gap = dpPx(ctx, 4);
        int barH = dpPx(ctx, 12); // 进度条高度从 10dp 提升到 12dp

        LinearLayout barRow = new LinearLayout(ctx);
        barRow.setOrientation(LinearLayout.HORIZONTAL);
        barRow.setGravity(Gravity.CENTER_VERTICAL);

        // 目标名称标签 — 固定宽度，左对齐
        TextView targetLabel = new TextView(ctx);
        targetLabel.setText(q.getTargetDisplayName(tg.type(), tg.item()).getString());
        targetLabel.setTextSize(12);
        targetLabel.setTextColor(0xFFCCCCCC);
        barRow.addView(targetLabel, new LinearLayout.LayoutParams(dpPx(ctx, 140), ViewGroup.LayoutParams.WRAP_CONTENT));

        // 进度条容器
        FrameLayout barFrame = new FrameLayout(ctx);
        barFrame.setLayoutParams(new LinearLayout.LayoutParams(barW, barH));

        View barBg = new View(ctx);
        ShapeDrawable sd1 = new ShapeDrawable();
        sd1.setColor(0xFF333333);
        sd1.setCornerRadius(dpPx(ctx, 2));
        barBg.setBackground(sd1);
        barFrame.addView(barBg, new FrameLayout.LayoutParams(barW, barH));

        // 填充条
        View barFg = null;
        if (max > 0) {
            int fillW = (int) ((float) prog / max * barW);
            barFg = new View(ctx);
            ShapeDrawable sd2 = new ShapeDrawable();
            sd2.setColor(prog >= max ? GREEN : ACCENT);
            sd2.setCornerRadius(dpPx(ctx, 2));
            barFg.setBackground(sd2);
            barFrame.addView(barFg, new FrameLayout.LayoutParams(Math.max(fillW, 1), barH));
        }

        // 计数文字
        TextView count = new TextView(ctx);
        count.setText(prog + "/" + max);
        count.setTextSize(11);
        count.setTextColor(GRAY);
        count.setLayoutParams(new LinearLayout.LayoutParams(dpPx(ctx, 56), ViewGroup.LayoutParams.WRAP_CONTENT));

        // 缓存引用
        if (slotBarFgs[slotIndex] != null && targetIndex < slotBarFgs[slotIndex].length) {
            slotBarFgs[slotIndex][targetIndex] = barFg;
        }
        if (slotBarCounts[slotIndex] != null && targetIndex < slotBarCounts[slotIndex].length) {
            slotBarCounts[slotIndex][targetIndex] = count;
        }

        barRow.addView(barFrame);
        View spacer = new View(ctx);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(dpPx(ctx, gap * 2), ViewGroup.LayoutParams.WRAP_CONTENT));
        barRow.addView(spacer);
        barRow.addView(count);

        return barRow;
    }

    private TextView buildRewardText(Context ctx, QuestDefinition q, boolean done) {
        var rn = q.getRewardDisplayNames();
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < q.rewards().size(); r++) {
            if (r > 0) sb.append(", ");
            sb.append(Component.translatable("quest.improved_original.reward",
                    q.rewards().get(r).count(), rn.get(r)).getString());
        }
        TextView reward = new TextView(ctx);
        reward.setText(sb.toString());
        reward.setTextSize(12);
        reward.setTextColor(done ? GREEN : GOLD);
        return reward;
    }

    private TextView buildEmptySlot(Context ctx) {
        TextView empty = new TextView(ctx);
        empty.setText(Component.translatable("quest.improved_original.empty_slot").getString());
        empty.setTextSize(15);
        empty.setTextColor(0xFF666666);
        return empty;
    }

    private View buildLockButton(Context ctx, int slotIndex, int panelW) {
        boolean locked = questData.isSlotLocked(slotIndex);
        String label = locked
                ? Component.translatable("quest.improved_original.locked").getString()
                : Component.translatable("quest.improved_original.lock_button").getString();
        int btnW = panelW / 4 - dpPx(ctx, 24);

        TextView lockBtn = buildClickableButton(ctx, label, 13, 0x66333333, panelW,
                v -> PacketDistributor.sendToServer(new C2SQuestLockPayload(slotIndex)));

        slotLockButtons[slotIndex] = lockBtn;
        lockBtn.setLayoutParams(new LinearLayout.LayoutParams(btnW, dpPx(ctx, 36)));
        return lockBtn;
    }

    private View buildFooter(Context ctx, int panelW) {
        int contentW = panelW - dpPx(ctx, PANEL_PADDING_DP) * 2;
        int footerH = dpPx(ctx, 60);

        LinearLayout footer = new LinearLayout(ctx);
        footer.setOrientation(LinearLayout.HORIZONTAL);
        footer.setGravity(Gravity.CENTER);
        int pad = dpPx(ctx, CONTENT_GAP_DP);
        footer.setPadding(pad, 0, pad, 0);

        ShapeDrawable footerBg = new ShapeDrawable();
        footerBg.setCornerRadius(dpPx(ctx, 8));
        footerBg.setColor(HEADER_COLOR);
        footer.setBackground(footerBg);

        TextView refreshBtn = buildClickableButton(ctx,
                Component.translatable("quest.improved_original.refresh_button", Config.REFRESH_COST.getAsInt()).getString(),
                15, 0xFF555555, panelW,
                v -> PacketDistributor.sendToServer(new C2SQuestRefreshPayload()));

        TextView closeBtn = buildClickableButton(ctx,
                Component.translatable("quest.improved_original.done").getString(),
                15, 0xFF555555, panelW,
                v -> Minecraft.getInstance().execute(() -> {
                    var screen = Minecraft.getInstance().screen;
                    if (screen != null) screen.onClose();
                }));

        LinearLayout.LayoutParams refreshParams = new LinearLayout.LayoutParams(dpPx(ctx, 260), dpPx(ctx, 40));
        refreshParams.setMargins(0, 0, dpPx(ctx, SECTION_GAP_DP), 0);
        footer.addView(refreshBtn, refreshParams);
        LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(dpPx(ctx, 100), dpPx(ctx, 40));
        closeParams.setMargins(dpPx(ctx, SECTION_GAP_DP), 0, 0, 0);
        footer.addView(closeBtn, closeParams);

        footer.setLayoutParams(new LinearLayout.LayoutParams(contentW, footerH));
        return footer;
    }

    private TextView buildClickableButton(Context ctx, String text, int textSize, int bgColor, int panelW, View.OnClickListener onClick) {
        TextView btn = new TextView(ctx);
        btn.setText(text);
        btn.setTextSize(textSize);
        btn.setTextColor(0xFFCCCCCC);
        btn.setGravity(Gravity.CENTER);
        btn.setClickable(true);

        ShapeDrawable normalBg = new ShapeDrawable();
        normalBg.setCornerRadius(dpPx(ctx, 4));
        normalBg.setColor(bgColor);
        btn.setBackground(normalBg);

        btn.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setAlpha(0.7f);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.setAlpha(1.0f);
                    break;
            }
            return false;
        });

        btn.setOnClickListener(onClick);
        return btn;
    }

    // ======================== 刷新逻辑 ========================

    /**
     * 更新倒计时文字（不触发数据刷新）。
     */
    private void updateCountdownText() {
        if (countdownText == null || Minecraft.getInstance().level == null) return;
        long tick = Minecraft.getInstance().level.getGameTime();
        long iv = 20L * 60 * Config.QUEST_REFRESH_INTERVAL_MINUTES.getAsInt();
        if (iv <= 0) iv = 1;
        long sec = (iv - tick % iv) / 20;
        countdownText.setText(
                Component.translatable("quest.improved_original.countdown", sec / 60, sec % 60).getString());
    }

    /**
     * 无感增量刷新：比较版本号，仅更新变化的 View，不重建 Fragment。
     * 通过防抖标记避免同一帧内的重复刷新。
     */
    private void refreshUI() {
        refreshPending = false;
        View view = getView();
        if (view == null || !isAdded()) return;

        QuestData latest = ClientQuestCache.get();
        if (latest == null) return;
        questData = latest;

        for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
            refreshSlotUI(i);
        }
    }

    /**
     * 增量更新单个槽位的 UI：任务名、进度条、奖励文字、锁定按钮。
     */
    private void refreshSlotUI(int slotIndex) {
        QuestSlotData sl = questData.getSlot(slotIndex);
        var qo = questData.getQuest(slotIndex);
        boolean done = sl.isComplete();

        // 更新任务名文字和颜色
        if (slotNameTexts[slotIndex] != null && qo.isPresent()) {
            QuestDefinition q = qo.get();
            refreshQuestName(slotIndex, q, done);
        }

        // 更新进度条
        if (qo.isPresent()) {
            QuestDefinition q = qo.get();
            List<Integer> progress = sl.perTargetProgress();
            View[] barFgs = slotBarFgs[slotIndex];
            TextView[] barCounts = slotBarCounts[slotIndex];
            if (barFgs != null) {
                for (int t = 0; t < q.targets().size() && t < barFgs.length; t++) {
                    QuestDefinition.QuestTarget tg = q.targets().get(t);
                    int max = tg.count();
                    int prog = t < progress.size() ? progress.get(t) : 0;
                    int fillW = max > 0 ? Math.max((int) ((float) prog / max * barW), 1) : 0;

                    // 更新进度条填充宽度
                    View barFg = barFgs[t];
                    if (barFg != null) {
                        ViewGroup.LayoutParams lp = barFg.getLayoutParams();
                        lp.width = fillW;
                        barFg.setLayoutParams(lp);

                        // 更新填充颜色
                        ShapeDrawable sd = (ShapeDrawable) barFg.getBackground();
                        if (sd != null) {
                            sd.setColor(prog >= max ? GREEN : ACCENT);
                        }
                    }

                    // 更新计数文字
                    TextView countTv = barCounts != null ? barCounts[t] : null;
                    if (countTv != null) {
                        countTv.setText(prog + "/" + max);
                    }
                }
            }
        }

        // 更新奖励文字
        TextView rewardTv = slotRewardTexts[slotIndex];
        if (rewardTv != null && qo.isPresent()) {
            QuestDefinition q = qo.get();
            var rn = q.getRewardDisplayNames();
            StringBuilder sb = new StringBuilder();
            for (int r = 0; r < q.rewards().size(); r++) {
                if (r > 0) sb.append(", ");
                sb.append(Component.translatable("quest.improved_original.reward",
                        q.rewards().get(r).count(), rn.get(r)).getString());
            }
            rewardTv.setText(sb.toString());
            rewardTv.setTextColor(done ? GREEN : GOLD);
        }

        // 更新锁定按钮文字
        TextView lockBtn = slotLockButtons[slotIndex];
        if (lockBtn != null) {
            boolean locked = questData.isSlotLocked(slotIndex);
            lockBtn.setText(locked
                    ? Component.translatable("quest.improved_original.locked").getString()
                    : Component.translatable("quest.improved_original.lock_button").getString());
        }
    }

    /**
     * 刷新单个任务名文字和颜色。
     */
    private void refreshQuestName(int slotIndex, QuestDefinition q, boolean done) {
        TextView nameView = slotNameTexts[slotIndex];
        if (nameView == null) return;

        boolean locked = questData.isSlotLocked(slotIndex);
        int color;
        if (done) {
            color = GREEN;
        } else if (locked) {
            color = 0xFFFF5555;
        } else {
            color = WHITE;
        }

        StringBuilder sb = new StringBuilder("[");
        sb.append(slotIndex + 1).append("] ");
        if (q.name() != null && !q.name().isEmpty()) {
            sb.append(Component.translatable(q.name()).getString());
        }
        if (locked) {
            sb.append(" ").append(Component.translatable("quest.improved_original.locked").getString());
        }

        nameView.setText(sb.toString());
        nameView.setTextColor(color);
    }

    /**
     * 安排一次增量刷新（带防抖，同一帧内多次调用只执行一次）。
     */
    private void scheduleRefresh() {
        if (refreshPending) return;
        refreshPending = true;
        View view = getView();
        if (view != null) {
            view.post(doRefresh);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable DataSet savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 注册数据变化监听：服务端同步数据到达时，增量刷新（不再重建 Fragment）
        ClientQuestCache.setListener(() -> Minecraft.getInstance().execute(() -> {
            if (isAdded()) {
                long currentVersion = ClientQuestCache.getVersion();
                if (currentVersion != lastKnownVersion) {
                    lastKnownVersion = currentVersion;
                    scheduleRefresh();
                }
            }
        }));
        // 启动倒计时定时器
        view.postDelayed(this::tickCountdown, 1000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ClientQuestCache.clearListener();
        // 清空缓存引用，避免内存泄漏
        clearCachedRefs();
        refreshPending = false;
    }

    private void clearCachedRefs() {
        countdownText = null;
        for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
            slotNameTexts[i] = null;
            slotBarFgs[i] = null;
            slotBarCounts[i] = null;
            slotRewardTexts[i] = null;
            slotLockButtons[i] = null;
            slotLockBtnContainers[i] = null;
        }
    }

    /**
     * 每秒 tick：更新倒计时，检测数据版本变化。
     * 数据变化时通过版本号比较，增量刷新（不再 openScreen）。
     */
    private void tickCountdown() {
        View view = getView();
        if (view != null && isAdded()) {
            updateCountdownText();

            long currentVersion = ClientQuestCache.getVersion();
            if (currentVersion != lastKnownVersion) {
                lastKnownVersion = currentVersion;
                scheduleRefresh();
            }

            view.postDelayed(this::tickCountdown, 1000);
        }
    }

    // ---- ScreenCallback ----
    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public boolean shouldClose() { return true; }

    @Override
    public boolean hasDefaultBackground() { return false; }
}
