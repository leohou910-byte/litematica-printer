package me.aleksilassila.litematica.printer.pwp.refactor.core;

import java.util.*;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.selection.AreaSelection;
import fi.dy.masa.litematica.selection.Box;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import me.aleksilassila.litematica.printer.printer.zxy.inventory.InventoryUtils;
import me.aleksilassila.litematica.printer.pwp.refactor.util.VersionIntegrator;
import me.aleksilassila.litematica.printer.pwp.refactor.core.action.ActionControler;
import me.aleksilassila.litematica.printer.printer.zxy.Utils.HighlightBlockRenderer;
import me.aleksilassila.litematica.printer.printer.zxy.Utils.ZxyUtils;
import me.aleksilassila.litematica.printer.printer.zxy.Utils.overwrite.MyBox;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.*;

//#if MC < 11900
//$$ import net.minecraft.network.chat.TranslatableComponent;
//#endif

import static me.aleksilassila.litematica.printer.LitematicaMixinMod.SCHEMATIC_CONTAINER_SYNC_TARGET_COLOR;
import static me.aleksilassila.litematica.printer.LitematicaMixinMod.SCHEMATIC_CONTAINER_SYNC_TIMEOUT;
import static me.aleksilassila.litematica.printer.LitematicaMixinMod.SCHEMATIC_CONTAINER_SYNC_MISSING_COLOR;
import static me.aleksilassila.litematica.printer.LitematicaMixinMod.SCHEMATIC_CONTAINER_SYNC_RATE;
import static me.aleksilassila.litematica.printer.LitematicaMixinMod.SCHEMATIC_CONTAINER_SYNC_ERROR_COLOR;

public class SchematicContainerSync {
    private static final Minecraft client = Minecraft.getInstance();

    // === 功能類 ===
    public record ContainerResult(boolean canOpen, String message) {
    }

    // 回傳選區內要填充的容器List
    public static LinkedList<BlockPos> getSelectionAreaContainerList() {
        LinkedList<BlockPos> containerPositions = new LinkedList<>();
        if (client.level == null) return containerPositions;

        // 取得所有選區
        AreaSelection areaSelection = DataManager.getSelectionManager().getCurrentSelection();
        if (areaSelection == null) return containerPositions;
        List<Box> boxes = areaSelection.getAllSubRegionBoxes();

        // 取得與藍圖相同容器方塊
        WorldSchematic schematicWorld = SchematicWorldHandler.getSchematicWorld();
        if (schematicWorld == null) return containerPositions;

        for (Box box : boxes) {
            if (box.getPos1() == null || box.getPos2() == null) continue;

            // 加入此選區中的箱子
            for (BlockPos pos : new MyBox(box)) {
                // 是否為箱子
                if (!InventoryUtils.isInventory(client.level, pos)) continue;

                // 是否與藍圖相同
                BlockState schematicBlockState = schematicWorld.getBlockState(pos);
                BlockState realblockState = client.level.getBlockState(pos);
                if (!schematicBlockState.is(realblockState.getBlock())) continue;

                // 加入該容器座標
                containerPositions.add(pos);
            }
        }

        return containerPositions;
    }

    // 是否能開啟容器
    public static ContainerResult canContainerOpen(BlockPos pos) {
        if (pos == null || client.level == null) {
            return new ContainerResult(false, "地圖尚未載入或座標無效");
        }

        BlockState blockState = client.level.getBlockState(pos);
        BlockEntity blockEntity = client.level.getBlockEntity(pos);
        boolean getMenuProvider = !(blockState.getMenuProvider(client.level, pos) == null);
        // 是否能獲得容器選單
        if (!getMenuProvider) {
            return new ContainerResult(false, "無法獲取容器選單");
        }

        // 界伏盒特殊處理
        if (blockEntity instanceof ShulkerBoxBlockEntity shulkerBoxBlockEntity) {
            Direction direction = blockState.getValue(ShulkerBoxBlock.FACING);

            // 檢查是否有實體或方塊阻擋蓋子彈出空間
            boolean isBlocked =
                    //#if MC >= 260100
                    !client.level.noCollision(Shulker.getProgressDeltaAabb(1.0F, direction, 0.0F, 0.5F, Vec3.atBottomCenterOf(pos)).deflate(1.0E-6));
                    //#elseif MC >= 12104
                    //$$ !client.level.noCollision(Shulker.getProgressDeltaAabb(1.0F, direction, 0.0F, 0.5F, pos.getBottomCenter()).deflate(1.0E-6));
                    //#elseif MC >= 12006
                    //$$ !client.level.noCollision(Shulker.getProgressDeltaAabb(1.0F, direction, 0.0F, 0.5F).move(pos).deflate(1.0E-6));
                    //#else
                    //$$ !client.level.noCollision(Shulker.getProgressDeltaAabb(direction, 0.0f, 0.5f).move(pos).deflate(1.0E-6));
                    //#endif

            // 如果蓋子已經是開的（例如正要關上），通常不需要判定為阻擋
            if (shulkerBoxBlockEntity.getAnimationStatus() == ShulkerBoxBlockEntity.AnimationStatus.CLOSED && isBlocked) {
                return new ContainerResult(false, "界伏盒開啟方向有阻擋");
            }
        }

        return new ContainerResult(true, "");
    }

    // 獲取藍圖容器物品，方便處理
    private static NonNullList<ItemStack> getContainerItemStacksFromSchematic(BlockPos worldPos) {
        WorldSchematic schematicWorld = SchematicWorldHandler.getSchematicWorld();
        if (schematicWorld == null) return null;

        BlockEntity blockEntity = schematicWorld.getBlockEntity(worldPos);
        if (blockEntity instanceof Container container) {
            int containerSize = container.getContainerSize();
            NonNullList<ItemStack> schematicContainerItemStacks = NonNullList.withSize(containerSize, ItemStack.EMPTY);
            for (int i = 0; i < containerSize; i++) {
                schematicContainerItemStacks.set(i, container.getItem(i).copy());
            }
            
            return schematicContainerItemStacks;
        }

        return NonNullList.create();
    }

    // 填充並回傳缺少物品
    public static Map<Item, Integer> fillContainerAndReturnMissing(BlockPos pos) {
        Map<Item, Integer> misItems = new HashMap<>();
        if (client.player == null || client.gameMode == null) return misItems;
        
        AbstractContainerMenu sc = client.player.containerMenu;

        NonNullList<Slot> playerSlots = sc.slots;
        NonNullList<ItemStack> schematicContainerItemStacks = getContainerItemStacksFromSchematic(pos);
        if (schematicContainerItemStacks.isEmpty()) return misItems;

        // 大箱子 index 偏移
        int indexOffSet = 0;
        BlockState blockState =  client.level.getBlockState(pos);
        if (blockState.hasProperty(ChestBlock.TYPE) && blockState.getValue(ChestBlock.TYPE) == ChestType.LEFT) indexOffSet = 27;

        // 填充
        int playerContainerSize = playerSlots.get(0).container.getContainerSize();
        int schematicContainerSize = schematicContainerItemStacks.size();
        for (int i = 0; i < schematicContainerSize; i++) {
            // 加入偏移
            int playerSlotIndex = i + indexOffSet;
            // 防止超過容器介面邊界
            if (playerSlotIndex >= playerContainerSize) break;

            // 容器和藍圖的物品
            ItemStack playerItemStack = playerSlots.get(playerSlotIndex).getItem();
            ItemStack schematicItemStack = schematicContainerItemStacks.get(i);
            // 容器和藍圖的堆疊數量
            int currNum = playerItemStack.getCount();
            int tarNum = schematicItemStack.getCount();

            // 丟出物品
            boolean same = ItemStack.isSameItemSameComponents(playerItemStack, schematicItemStack);
            if (!same) {
                // 不同直接扔出
                client.gameMode.handleContainerInput(sc.containerId, playerSlotIndex, 1, ContainerInput.THROW, client.player);
                currNum = 0;
            } else if (currNum != tarNum) {
                // 相同但有多
                while (currNum > tarNum) {
                    client.gameMode.handleContainerInput(sc.containerId, playerSlotIndex, 0, ContainerInput.THROW, client.player);
                    currNum--;
                }
            } else {
                // 物品和數量相同時 不和背包交互
                continue;
            }

            // 背包互動，放入物品
            for (int j = playerContainerSize; j < sc.slots.size(); j++) {
                // 補充完畢跳出
                if (currNum == tarNum) break;

                // 不符合條件跳過
                ItemStack playerItem = sc.slots.get(j).getItem();
                boolean same2 = ItemStack.isSameItemSameComponents(schematicItemStack, playerItem);
                if (playerItem.isEmpty() || !same2) continue;

                // 取德物品數量
                int playerItemCount = playerItem.getCount();

                // 拿取背包物品，不管如何都要先拿起
                client.gameMode.handleContainerInput(sc.containerId, j, 0, ContainerInput.PICKUP, client.player);

                if ((tarNum - currNum) >= playerItemCount) {
                    // 可直接全部移入
                    client.gameMode.handleContainerInput(sc.containerId, playerSlotIndex, 0, ContainerInput.PICKUP, client.player);
                    currNum += playerItemCount;
                } else {
                    // 需要一個一個移入
                    for (; currNum < tarNum && playerItemCount > 0; playerItemCount--) {
                        client.gameMode.handleContainerInput(sc.containerId, playerSlotIndex, 1, ContainerInput.PICKUP, client.player);
                        currNum++;
                    }
                }

                // 如果有多，放回背包
                if (!sc.getCarried().isEmpty()) {
                    client.gameMode.handleContainerInput(sc.containerId, j, 0, ContainerInput.PICKUP, client.player);
                }
            }

            if (tarNum > currNum) {
                Item type = schematicItemStack.getItem();
                misItems.put(type, misItems.getOrDefault(type, 0) + (tarNum - currNum));
            }
        }

        return misItems;
    }

    public static boolean haveAnyItemInInv(Map<Item, Integer> misItems) {
        if(client.player == null) return false;
        AbstractContainerMenu sc = client.player.containerMenu;
        if (!sc.equals(client.player.inventoryMenu)) return false;

        for (int i = 0; i < sc.slots.size(); i++) {
            ItemStack playerItem = sc.slots.get(i).getItem();
            if (!playerItem.isEmpty() && misItems.containsKey(playerItem.getItem())) {
                return true;
            }
        }

        return false;
    }

    // === 主要程式 ===
    public enum SchematicSyncState {
        IDLE,                       // 閒置中
        FIND_AND_CHECK_CONTAINER,   // 尋找最近的箱子 並檢查箱子
        FILLING_CONTAINER,          // 正在填充物品
        WAITING_ITEMS               // 等待背包中有相符物品
    }
    public static List<BlockPos> schematicSyncList = new ArrayList<>();
    public static Map<Item, Integer> missingItems = new HashMap<>();
    public static Set<BlockPos> highlightTargetPosList = new LinkedHashSet<>();
    public static Set<BlockPos> highlightMissingPosList = new LinkedHashSet<>();
    public static Set<BlockPos> highlightErrorPosList = new LinkedHashSet<>();
    public static SchematicSyncState schematicSyncState = SchematicSyncState.IDLE;
    public static int schematicSyncCooldown = 0;
    public static int openRetryTimer = 0; // 用於計算超時

    private static BlockPos blockPos = null;
    private static BlockPos tempBlockPos = null;

    // 顏色初始渲染
    private static void getReadyColor() {
        String schematicSyncTarget = "schematicSyncTarget";
        String schematicSyncMissing = "schematicSyncMissing";
        String schematicSyncError = "schematicSyncError";

        HighlightBlockRenderer.createHighlightBlockList(schematicSyncTarget, SCHEMATIC_CONTAINER_SYNC_TARGET_COLOR);
        HighlightBlockRenderer.createHighlightBlockList(schematicSyncMissing, SCHEMATIC_CONTAINER_SYNC_MISSING_COLOR);
        HighlightBlockRenderer.createHighlightBlockList(schematicSyncError, SCHEMATIC_CONTAINER_SYNC_ERROR_COLOR);

        highlightTargetPosList = HighlightBlockRenderer.getHighlightBlockPosList(schematicSyncTarget);
        highlightMissingPosList = HighlightBlockRenderer.getHighlightBlockPosList(schematicSyncMissing);
        highlightErrorPosList = HighlightBlockRenderer.getHighlightBlockPosList(schematicSyncError);


        highlightTargetPosList.clear();
        highlightMissingPosList.clear();
        highlightErrorPosList.clear();
    }

    // 開始單獨藍圖容器同步
    public static void startSingleSchematicContainerSync() {
        HitResult hitResult = client.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
            VersionIntegrator.overlayMessage(client, "未偵測到容器", false);
            return;
        }

        blockPos = ((BlockHitResult) hitResult).getBlockPos();
        if (!InventoryUtils.isInventory(client.level, blockPos)) {
            VersionIntegrator.overlayMessage(client, "這不是容器", false);
            return;
        }

        VersionIntegrator.overlayMessage(client, "單獨容器同步", false);
        getReadyColor();
        schematicSyncList.clear();
        schematicSyncList.add(blockPos);
        highlightTargetPosList.add(blockPos);
        schematicSyncState = SchematicSyncState.FIND_AND_CHECK_CONTAINER;
    }

    // 開始多藍圖容器同步
    public static void startMultipleSchematicContainerSync() {
        if (schematicSyncList.isEmpty()) {
            getReadyColor();
            VersionIntegrator.overlayMessage(client, "多重藍圖容器同步", false);

            schematicSyncList.clear();
            schematicSyncList.addAll(getSelectionAreaContainerList());
            highlightTargetPosList.addAll(schematicSyncList);
            schematicSyncState = SchematicSyncState.FIND_AND_CHECK_CONTAINER;
        } else {
            VersionIntegrator.overlayMessage(client, "藍圖容器同步取消", false);
            stopSync();
        }
    }

    // 藍圖容器同步
    public static void schematicSyncInv() {
        switch (schematicSyncState) {
            case FIND_AND_CHECK_CONTAINER -> {
                if (openRetryTimer > 0) {
                    VersionIntegrator.overlayMessage(client, "嘗試開啟容器...", false);
                    return;
                }

                // 已完成全部清單
                if (schematicSyncList.isEmpty() && tempBlockPos == null) {
                    schematicSyncState = SchematicSyncState.IDLE;
                    VersionIntegrator.overlayMessage(client, "藍圖容器同步完成", false);
                    return;
                }

                // 尋找目標容器
                if (tempBlockPos != null) {
                    // 優先使用沒確認的方塊座標
                    if (ZxyUtils.canInteracted(tempBlockPos)) {
                        blockPos = tempBlockPos;
                    }
                } else {
                    // 尋找第一個可觸碰的方塊
                    blockPos = null;
                    for (BlockPos pos : schematicSyncList) {
                        if (ZxyUtils.canInteracted(pos)) {
                            blockPos = pos;
                            break;
                        }
                    }
                }

                // 容器是否在範圍內
                if (blockPos == null) {
                    VersionIntegrator.overlayMessage(client, "距離過遠", false);
                    return;
                }

                // 檢查容器狀態是否能開啟
                ContainerResult result = canContainerOpen(blockPos);
                if (!result.canOpen) {
                    VersionIntegrator.overlayMessage(client, result.message(), false);

                    schematicSyncList.remove(blockPos);
                    highlightTargetPosList.remove(blockPos);
                    highlightErrorPosList.add(blockPos);
                    return;
                }

                // 開啟容器
                ActionControler.lookBlock(client.player, blockPos);
                ActionControler.rightClickBlock(client, blockPos);
                openRetryTimer = SCHEMATIC_CONTAINER_SYNC_TIMEOUT.getIntegerValue();
            }

            case FILLING_CONTAINER -> {
                if (client.player == null ) return;

                // 確保現在開著的確實是容器，不是玩家自己的背包
                if (client.player.containerMenu.equals(client.player.inventoryMenu)) {
                    schematicSyncState = SchematicSyncState.FIND_AND_CHECK_CONTAINER;
                    return;
                }

                // 重製計時器
                openRetryTimer = 0;

                // 執行填充邏輯
                missingItems = fillContainerAndReturnMissing(blockPos);

                // 關閉箱子
                client.player.closeContainer();

                // 根據不同情況，渲染箱子顏色，切換到不同狀態機
                schematicSyncList.remove(blockPos);
                highlightTargetPosList.remove(blockPos);

                if (missingItems.isEmpty()) { // 同步完成可以進入下一個
                    tempBlockPos = null;
                    schematicSyncState = SchematicSyncState.FIND_AND_CHECK_CONTAINER;
                } else { // 需要等待物品
                    tempBlockPos = blockPos;
                    highlightMissingPosList.add(blockPos);

                    schematicSyncState = SchematicSyncState.WAITING_ITEMS;
                }

                // 冷卻重設
                schematicSyncCooldown = SCHEMATIC_CONTAINER_SYNC_RATE.getIntegerValue();
            }

            case WAITING_ITEMS -> {
                if (haveAnyItemInInv(missingItems)) {
                    highlightMissingPosList.remove(tempBlockPos);
                    highlightTargetPosList.add(tempBlockPos);

                    schematicSyncState = SchematicSyncState.FIND_AND_CHECK_CONTAINER;
                    return;
                }

                List<String> names = missingItems.entrySet().stream()
                        .map(e -> {
                            //#if MC >= 11900
                            return Component.translatable(e.getKey().getDescriptionId()).getString() + " x" + e.getValue();
                            //#else
                            //$$ return new TranslatableComponent(e.getKey().getDescriptionId()).getString() + " x" + e.getValue();
                            //#endif
                        })
                    //#if MC >= 11900
                    .toList();
                    //#else
                    //$$ .collect(java.util.stream.Collectors.toList());
                    //#endif

                String display;
                if (names.size() > 3) {
                    display = String.join(", ", names.subList(0, 3)) + " 等 " + names.size() + " 種物品";
                } else {
                    display = String.join(", ", names);
                }
                VersionIntegrator.overlayMessage(client, "缺少物品: " + display, false);
            }

            default -> {}
        }
    }

    public static void tick() {
        // 閒置狀態直接跳出
        if (schematicSyncState == SchematicSyncState.IDLE) return;

        // 超時計算
        if (openRetryTimer > 0) {
            openRetryTimer--;
        }

        // 冷卻時間
        if (schematicSyncCooldown > 1) {
            schematicSyncCooldown--;
            return;
        }

        // 執行操作
        schematicSyncInv();
    }

    public static void stopSync() {
        schematicSyncState = SchematicSyncState.IDLE;

        highlightTargetPosList.clear();
        highlightMissingPosList.clear();
        highlightErrorPosList.clear();
        
        schematicSyncList.clear();
        tempBlockPos = null;
    }
}
