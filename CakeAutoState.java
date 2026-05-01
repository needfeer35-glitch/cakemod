package com.cakeauto;

import net.minecraft.server.network.ServerPlayerEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CakeAutoState {
    private static final Map<UUID, CakeAutoState> STATES = new HashMap<>();

    private boolean autoEnabled = false;

    public static CakeAutoState get(ServerPlayerEntity player) {
        return STATES.computeIfAbsent(player.getUuid(), id -> new CakeAutoState());
    }

    public boolean isAutoEnabled() {
        return autoEnabled;
    }

    public void setAutoEnabled(boolean autoEnabled) {
        this.autoEnabled = autoEnabled;
    }
}
