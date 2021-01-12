package me.zachary.enchanter;


import me.zachary.enchanter.commands.EnchanterCommand;
import me.zachary.zachcore.ZachCorePlugin;
import me.zachary.zachcore.guis.ZachGUI;

public final class Enchanter extends ZachCorePlugin {
    public static ZachGUI zachGUI;

    @Override
    public void onEnable() {
        // Plugin startup logic
        preEnable();

        // Load commands
        new EnchanterCommand();

        zachGUI = new ZachGUI(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static ZachGUI getZachGUI() {
        return zachGUI;
    }
}
