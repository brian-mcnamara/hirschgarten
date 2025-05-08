load("@rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "fastbuild",
    srcs = glob(["*.kt"]),
    visibility = ["//visibility:public"],
    deps = [
        "//plugin-bazel/src/main/kotlin/org/jetbrains/bazel/flow",
        "//plugin-bazel/src/main/kotlin/org/jetbrains/bazel/jvm/run",
        "//rules_intellij/intellij_platform_sdk:plugin_api",
        "//sdkcompat",
        "@maven//:com_google_code_gson_gson",
    ],
)
