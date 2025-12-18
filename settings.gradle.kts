pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://jitpack.io")

        //SE SDK仓库地址
        maven ( url ="https://maven-android.solar-engine.com/repository/se_sdk_for_android/" )
        maven ( url ="https://developer.huawei.com/repo/" )
        maven ( url ="https://developer.hihonor.com/repo/" )

        maven (url = "https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea")
        maven (url = "https://artifact.bytedance.com/repository/pangle/")
        maven (url = "https://android-sdk.is.com/")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")

        //SE SDK仓库地址
        maven ( url ="https://maven-android.solar-engine.com/repository/se_sdk_for_android/" )
        maven ( url ="https://developer.huawei.com/repo/" )
        maven ( url ="https://developer.hihonor.com/repo/" )

        maven (url = "https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea")
        maven (url = "https://artifact.bytedance.com/repository/pangle/")
        maven (url = "https://android-sdk.is.com/")
    }
}

rootProject.name = "MoonStory"
include(":app")
