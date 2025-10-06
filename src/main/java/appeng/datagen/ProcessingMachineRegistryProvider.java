package appeng.datagen;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import appeng.AE2Registries;

public class ProcessingMachineRegistryProvider implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final PackOutput output;

    public ProcessingMachineRegistryProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        JsonObject root = new JsonObject();
        JsonArray machines = new JsonArray();

        machines.add(machine("minecraft:furnace", "furnace", "Vanilla Furnace"));
        machines.add(machine("minecraft:blast_furnace", "blast_furnace", "Blast Furnace"));
        machines.add(machine("minecraft:brewing_stand", "brewing", "Brewing Stand"));

        root.add("machines", machines);

        Path path = this.output.getOutputFolder()
                .resolve("data/" + AE2Registries.MODID + "/datamaps/processing_machines.json");

        return DataProvider.saveStable(cachedOutput, GSON.toJsonTree(root), path);
    }

    private static JsonObject machine(String id, String executor, String displayName) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", id);
        obj.addProperty("executor", executor);
        obj.addProperty("display", displayName);
        return obj;
    }

    @Override
    public String getName() {
        return "AE2 Processing Machine Registry";
    }
}
