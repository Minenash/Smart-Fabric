package com.minenash.pocketwatch;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Hook;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.RangeConstraint;

import java.util.List;

@Modmenu(modId = "pocketwatch")
@Config(name = "pocketwatch", wrapperName = "PocketwatchConfig")
public class PocketwatchConfigModel {

    public boolean showDetails = false;

    @RangeConstraint(min = 1, max = 6)
    public int slotLimit = 3;

    @Hook
    public List<String> whitelist = List.of("minecraft:clock", "minecraft:compass", "minecraft:recovery_compass");

}
