## Tune Kit Integration

### The Tune kit is no longer supported

The Tune kit for the mParticle iOS SDK has been deprecated and is no longer supported.

-----

**Deprecated**

This repository contains the [Tune](https://www.tune.com) integration for the [mParticle Android SDK](https://github.com/mParticle/mparticle-android-sdk).

### Adding the integration

1. Add the kit dependency to your app's build.gradle:

    ```groovy
    dependencies {
        implementation 'com.mparticle:android-tune-kit:5+'
    }
    ```
2. Follow the mParticle Android SDK [quick-start](https://github.com/mParticle/mparticle-android-sdk), then rebuild and launch your app, and verify that you see `"Tune detected"` in the output of `adb logcat`.
3. Reference mParticle's integration docs below to enable the integration.

### Documentation

[Tune integration](http://docs.mparticle.com/?java#tune)

### License

[Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)
