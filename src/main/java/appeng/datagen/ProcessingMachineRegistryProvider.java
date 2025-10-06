package appeng.datagen;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import appeng.AE2Registries;

public final class ProcessingMachineRegistryProvider implements DataProvider {
    private final PackOutput output;

    public ProcessingMachineRegistryProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        var machines = new JsonArray();
        createEntries().forEach(entry -> machines.add(entry.toJson()));

        var root = new JsonObject();
        root.add("machines", machines);

        var pathProvider = this.output.createPathProvider(PackOutput.Target.DATA_PACK,
                AE2Registries.MODID + "/datamaps");

        return DataProvider.saveStable(cachedOutput, root, pathProvider.file("processing_machines.json"));
    }

    private static List<ProcessingMachineEntry> createEntries() {
        return List.of(
                new ProcessingMachineEntry("minecraft:furnace", "furnace", "Vanilla Furnace"),
                new ProcessingMachineEntry("minecraft:blast_furnace", "blast_furnace", "Blast Furnace"),
                new ProcessingMachineEntry("minecraft:brewing_stand", "brewing", "Brewing Stand"));
    }

    @Override
    public String getName() {
        return "AE2 Processing Machine Registry";
    }

    private record ProcessingMachineEntry(String id, String executor, String display) {
        private JsonObject toJson() {
            var obj = new JsonObject();
            obj.addProperty("id", id);
            obj.addProperty("executor", executor);
            obj.addProperty("display", display);
            return obj;
        }
    }
}
