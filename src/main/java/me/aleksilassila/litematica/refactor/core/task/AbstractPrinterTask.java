package me.aleksilassila.litematica.refactor.core.task;

import java.util.Iterator;

import me.aleksilassila.litematica.refactor.util.BlockPosIterable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * 抽象印表機任務基類。
 * <p>
 * 負責維護玩家周圍方塊區域的迭代器，每次 tick 尋找可執行的目標位置進行處理。
 */
public abstract class AbstractPrinterTask {

    private Iterator<BlockPos> posIterator;
    private BlockPos currentItrEyePos;
    protected Minecraft mc;

    public AbstractPrinterTask() {
    }

    /**
     * 執行單一遊戲 tick 的任務邏輯。
     * <p>
     * 在單一 tick 內最多嘗試執行 {@code executionPertick} 次有效的操作。
     * 若範圍內已無滿足條件的方塊，將提前結束本 tick 的執行。
     *
     * @param minecraft        Minecraft 客戶端實例
     * @param printDistance    印表機的可作用距離
     * @param executionPertick 每 tick 允許執行的最大操作數量
     */
    public final void tick(Minecraft minecraft, int printDistance, int executionPertick) {
        if (printDistance <= 0 || executionPertick <= 0) {
            return;
        }

        this.mc = minecraft;

        LocalPlayer player = this.mc.player;
        if (player == null) {
            return;
        }

        Vec3 eyeVec3 = player.getEyePosition();

        for (int i = 0; i < executionPertick; i++) {
            BlockPos pos = getPos(eyeVec3, printDistance);
            if (pos == null) {
                return;
            }

            execute(pos);
        }
    }

    /**
     * 搜尋下一個位於有效作用距離內且滿足 {@link #canExecute(BlockPos)} 條件的方塊位置。
     * <p>
     * 當迭代器尚未建立或玩家眼部位置發生變更時，會自動建立新的 {@link BlockPosIterable} 迭代器。
     * 若走訪完當前區域內所有方塊仍未尋得合適位置，會重置迭代器與眼部位置快取並回傳 {@code null}。
     *
     * @param eyeVec3       玩家眼部的精確向量座標，用於計算實體距離
     * @param reachDistance 允許的最大操作距離
     * @return 下一個可執行操作的 {@link BlockPos}；若當前範圍內已無可執行的方塊則傳回 {@code null}
     */
    protected BlockPos getPos(Vec3 eyeVec3, double reachDistance) {
        if (eyeVec3 == null || reachDistance <= 0) {
            return null;
        }

        BlockPos eyePos = BlockPos.containing(eyeVec3);

        // 當迭代器為空或者玩家眼睛位置改變時，重新建立走訪器
        if (this.posIterator == null || !eyePos.equals(this.currentItrEyePos)) {
            this.currentItrEyePos = eyePos;
            int distanceInteger = (int) reachDistance + 2;
            this.posIterator = new BlockPosIterable(this.currentItrEyePos, distanceInteger).iterator();
        }

        double reachDistanceSqr = reachDistance * reachDistance;

        while (this.posIterator.hasNext()) {
            BlockPos nextPos = this.posIterator.next();

            double posDistanceSqr = eyeVec3.distanceToSqr(Vec3.atCenterOf(nextPos));
            if (posDistanceSqr > reachDistanceSqr) {
                continue;
            }

            if (!canExecute(nextPos)) {
                continue;
            }

            return nextPos;
        }

        // 當前區域已無滿足條件的方塊，重置狀態以供下一次搜尋時重新建立迭代器
        this.posIterator = null;
        this.currentItrEyePos = null;
        return null;
    }

    /**
     * 檢查指定位置的方塊是否符合當前任務的執行條件。
     *
     * @param pos 待檢查的方塊位置
     * @return 若可執行則傳回 {@code true}，否則傳回 {@code false}
     */
    protected abstract boolean canExecute(BlockPos pos);

    /**
     * 對指定位置的方塊執行任務操作（如放置或打破方塊）。
     *
     * @param pos 目標方塊位置
     */
    protected abstract void execute(BlockPos pos);
}