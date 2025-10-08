plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.21.1"

stonecutter.registerChiseled(tasks.register("chiseledBuild", stonecutter.chiseled) {
    ofTask("build")
})

parameters {
    consts["MC"] = stonecutter.current.version
    consts["NEOFORGE"] = stonecutter.current.data["NEOFORGE"]?.toString() ?: ""
}

