package appeng.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import net.neoforged.neoforge.capabilities.Capability;

import appeng.api.AECapabilities;
import appeng.api.compat.CuriosCompat;
import appeng.api.compat.JeiCompat;
import appeng.api.compat.ReiCompat;
import appeng.api.networking.interop.GridApi;
import appeng.api.storage.StorageApi;
import appeng.capability.AE2Capabilities;

/**
 * Logs the status of optional integrations and exposed API entry points during startup.
 */
public final class AE2InteropValidator {
    private static final Set<String> REGISTERED_BRIDGES = ConcurrentHashMap.newKeySet();

    private AE2InteropValidator() {
    }

    public static void markBridgeInitialized(String name) {
        REGISTERED_BRIDGES.add(name);
        AELog.info("[Interop] Bridge initialized: %s", name);
    }

    public static void logStatus() {
        AELog.info("[Interop] Optional mod detection: %s", discoverOptionalMods());
        AELog.info("[Interop] API bridges registered: %s", registeredBridges());
        AELog.info("[Interop] Capability tokens: %s", listCapabilityTokens());
        AELog.info("[Interop] Block capabilities: %s", listBlockCapabilities());

        // Touch helpers to ensure they are not stripped and to hint at availability in logs.
        AELog.info("[Interop] Grid API helper ready: %s", GridApi.INSTANCE.getClass().getSimpleName());
        AELog.info("[Interop] Storage API helper ready: %s", StorageApi.INSTANCE.getClass().getSimpleName());
    }

    private static String discoverOptionalMods() {
        Map<String, Boolean> detection = new LinkedHashMap<>();
        detection.put("JEI", JeiCompat.isLoaded());
        detection.put("REI", ReiCompat.isLoaded());
        detection.put("Curios", CuriosCompat.isLoaded());
        return detection.entrySet().stream()
                .map(entry -> entry.getKey() + '=' + (entry.getValue() ? "present" : "absent"))
                .collect(Collectors.joining(", "));
    }

    private static String registeredBridges() {
        if (REGISTERED_BRIDGES.isEmpty()) {
            return "none";
        }
        return REGISTERED_BRIDGES.stream().sorted().collect(Collectors.joining(", "));
    }

    private static String listCapabilityTokens() {
        List<String> tokens = new ArrayList<>();
        for (Field field : AE2Capabilities.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (!Capability.class.isAssignableFrom(field.getType())) {
                continue;
            }
            tokens.add(field.getName());
        }

        Collections.sort(tokens);
        return String.join(", ", tokens);
    }

    private static String listBlockCapabilities() {
        List<String> tokens = new ArrayList<>();
        for (Field field : AECapabilities.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (!field.getType().getSimpleName().contains("Capability")) {
                continue;
            }
            tokens.add(field.getName());
        }
        Collections.sort(tokens);
        return String.join(", ", tokens);
    }
}
