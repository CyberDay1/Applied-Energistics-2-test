plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.21.1"

stonecutter.registerChiseled(tasks.register("chiseledBuild", stonecutter.chiseled) {
    ofTask("build")
})

parameters {
    consts.put("MC", "1.21.1")
    consts.put("NEOFORGE", "21.1.xxx")
}

