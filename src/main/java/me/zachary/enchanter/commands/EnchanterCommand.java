package me.zachary.enchanter.commands;

import me.zachary.enchanter.Enchanter;
import me.zachary.zachcore.commands.Command;
import me.zachary.zachcore.commands.CommandResult;
import me.zachary.zachcore.guis.ZMenu;
import me.zachary.zachcore.guis.buttons.ZButton;
import me.zachary.zachcore.utils.ChatPromptUtils;
import me.zachary.zachcore.utils.MessageUtils;
import me.zachary.zachcore.utils.PlayerInventoryUtils;
import me.zachary.zachcore.utils.items.ItemBuilder;
import me.zachary.zachcore.utils.xseries.XMaterial;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class EnchanterCommand extends Command {
    @Override
    public String getCommand() {
        return "enchanter";
    }

    @Override
    public CommandResult onPlayerExecute(Player player, String[] strings) {
        if(player.getInventory().getItemInMainHand().getType().isAir()){
            MessageUtils.sendMessage(player, "&cNo item found in your hand.");
            return CommandResult.COMPLETED;
        }
        //player.setLevel(player.getLevel() - 5);
        openEnchantInventory(player);
        return CommandResult.COMPLETED;
    }

    @Override
    public CommandResult onConsoleExecute(boolean b, String[] strings) {
        return CommandResult.COMPLETED;
    }

    private void getButtonBottom(Player player, ZMenu menu, ItemStack currentItemHand, int page){
        ZButton currentItemButton = new ZButton(currentItemHand);
        ZButton renameButton = new ZButton(new ItemBuilder(XMaterial.PAPER.parseMaterial())
                .name("&6&lRename item")
                .lore(
                        "&6Click on this button",
                        "&6to rename your item."
                )
                .build()
        ).withListener((InventoryClickEvent event) -> {
            player.closeInventory();
            ChatPromptUtils.showPrompt(JavaPlugin.getPlugin(Enchanter.class), player, "&6Please enter the new item name in chat:", new ChatPromptUtils.ChatConfirmHandler() {
                @Override
                public void onChat(ChatPromptUtils.ChatConfirmEvent chatConfirmEvent) {
                    PlayerInventoryUtils.SetInMainHand(player, new ItemBuilder(currentItemHand).name(chatConfirmEvent.getMessage()).build());
                    openInventory(player, menu);
                }
            });
        });
        ZButton repairButton = new ZButton(new ItemBuilder(XMaterial.ANVIL.parseMaterial())
                .name("&6&lRepair item")
                .lore(
                        "&6Click on this button",
                        "&6to repair your item."
                )
                .build()
        ).withListener((InventoryClickEvent event) -> {
            ItemMeta itemMeta = currentItemHand.getItemMeta();
            if(itemMeta instanceof Damageable){
                ((Damageable) itemMeta).setDamage(0);
                currentItemHand.setItemMeta(itemMeta);
            }
            openInventory(player, menu);
        });

        menu.setButton(page, 39, repairButton);
        menu.setButton(page, 40, currentItemButton);
        menu.setButton(page, 41, renameButton);
    }

    private void openInventory(Player player, ZMenu menu){
        Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(Enchanter.class), new Runnable() {
            @Override
            public void run() {
                openEnchantInventory(player);
            }
        });
    }

    private void openEnchantInventory(Player player){
        ZMenu enchanterMenu = Enchanter.getZachGUI().create("&c&lEnchanter menu", 5);
        ItemStack currentItemHand = player.getInventory().getItemInMainHand();
        int page = 0;
        int slot = 0;
        for (Enchantment e: Enchantment.values()) {
            if(slot == 0 && (page == 0 || page == 1 || page == 2))
                getButtonBottom(player, enchanterMenu, currentItemHand, page);
            if(!e.getItemTarget().includes(player.getInventory().getItemInMainHand()))
                continue;
            String status = null;
            if(currentItemHand.containsEnchantment(e)){
                status = "&cClick to remove this enchant";
            }else{
                status = "&aClick to add this enchant";
            }
            for (int i = e.getStartLevel(); i <= e.getMaxLevel(); i++){
                int finalI = i;
                int cost = 20 * i;
                ZButton zButton = new ZButton(new ItemBuilder(XMaterial.ENCHANTED_BOOK.parseMaterial())
                        .name("&8&n" + WordUtils.capitalize(e.getKey().getKey()))
                        .lore(
                                "&6Level: &e" + i,
                                "&6Cost: &e" + cost + " &6xp",
                                status
                        )
                        .enchant(Enchantment.getByKey(e.getKey()), i)
                        .flag(ItemFlag.HIDE_ENCHANTS)
                        .build()
                ).withListener((InventoryClickEvent event) -> {
                    if(player.getLevel() >= cost || player.getGameMode().equals(GameMode.CREATIVE)){
                        if(!player.getGameMode().equals(GameMode.CREATIVE))
                            player.setLevel(player.getLevel() - cost);
                    }else{
                        MessageUtils.sendMessage(player, "&cYou don't have enough xp to do that. You need &4" + cost + " &cXp.");
                        return;
                    }
                    if(currentItemHand.containsEnchantment(e)){
                        PlayerInventoryUtils.SetInMainHand(player, new ItemBuilder(currentItemHand).unenchant(e).build());
                    }else{
                        PlayerInventoryUtils.SetInMainHand(player, new ItemBuilder(currentItemHand).enchant(e, finalI).build());
                    }
                    player.closeInventory();
                    openInventory(player, enchanterMenu);
                });
                enchanterMenu.setButton(page, slot, zButton);
                slot++;
                if(i == e.getMaxLevel()){
                    slot += (9 - e.getMaxLevel());
                }
            }
            if(enchanterMenu.getButton(page, 27) != null){
                page++;
                slot = 0;
            }
        }
        if(enchanterMenu.getButton(0) == null){
            MessageUtils.sendMessage(player, "&cNo enchantment found with that item.");
            return;
        }
        player.openInventory(enchanterMenu.getInventory());
    }
}
