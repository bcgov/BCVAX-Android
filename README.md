# BC Vaccine Card Verifier-Android

#### BC Vaccine Card Verifier App is used to scan the QR generated from HealthGateway app and shows the vaccination status of a person. This is for Businesses from the province of British Columbia.

## Table of Contents

- [Project Resources](#markdown-header-project-resources)
- [Architecture](#markdown-header-architecture)
  - [Folder Structure](#markdown-header-folder-structure)
  - [Libraries](#markdown-header-libraries)
- [Configuration](#markdown-header-configuration)
  - [Build Variants](#markdown-header-build-variants)
  - [Environment Variables](#markdownpheader-environment-variables)
  - [APIs](#markdown-header-apis)
- [Deployment](#markdown-header-deployment)
  - [Versioning](#markdown-header-versioning)
  - [Internal](#markdown-header-internal)
  - [External](#markdown-header-external)
- [Contributors](#markdown-header-contributors)

## Project Resources
---

- [Google Play TBD](TBD)

## Architecture
---

Language: __Kotlin__

Architecture: __MVVM__

	-  Architecture used for app: MVVM
		- One activity with multiple fragments is used. Business logic is present in classes under utils package. View models associated with fragments seperate the UI from business logic. 
		- Does it differ from standard architecture? No.
		- Include UML diagram if needed. NA

Dependency Injection: __Hilt__

Concurrency: __Coroutines, Kotlin Flow__

### Folder Structure
barcodeanalyser, DI, model, UI, utils, viewModel are placed under project package. barcodeanalyser and utils contains the business logic.

### Libraries

- [GSON](https://github.com/google/gson): For serialization/deserialization, and converting Java Objects into JSON, and back
- [Material design](https://material.io/develop/android/docs/getting-started): Material Components for Android
- [Jetpack navigation](https://developer.android.com/guide/navigation): For screen navigation
- [Lifecycle-aware components](https://developer.android.com/jetpack/androidx/releases/lifecycle): For usage of Lifecycle-aware components
- [Hilt](https://developer.android.com/training/dependency-injection/hilt-android): For dependency injection
- [Google barcode scanner](https://developers.google.com/ml-kit/vision/barcode-scanning/android): For scanning QR
- [CameraX](https://developer.android.com/jetpack/androidx/releases/camera): For camera funtionalities
- [JWT](https://mvnrepository.com/artifact/io.jsonwebtoken): For verification of signature embedded in QR
- [BouncyCastle](https://mvnrepository.com/artifact/org.bouncycastle): For crptography
- [DataStore](https://developer.android.com/topic/libraries/architecture/datastore?gclid=CjwKCAjwhOyJBhA4EiwAEcJdcQu0nsVtVucNurmZ9Mr__luXN1zzVucwANlm07DqcpzHWqOL4T0SRRoCbbMQAvD_BwE&gclsrc=aw.ds): For caching key value pairs

## Configuration
---

### Build Variants

Build variants used in the app

- demoDebug
  - Uses dev jwks.json configuration
- demoRelease
  - Uses dev jwks.json configuration
- prodDebug
  - Uses PROD jwks.json configuration
- prodRelease
  - Uses PROD jwks.json configuration
- stageDebug
  - Uses STAGE jwks.json configuration
- stageRelease
  - Uses STAGE jwks.json configuration

### Environment Variables

- Not applicable

### APIs

- No external services were used

## Deployment
---

### Versioning

In version name 1.2, 1 stands for major change and 2 stands of minor change or patch fix.

### Internal

How is the app deployed and where?

Builds generated manually, distributed through Google Play. CI/CD implementation is in progress

### External

How is the app deployed and where?

Builds generated manually, distributed through Google Play.

## Contributors
---

List past and present contributors. Will S, Pinakin Kansara, Amit Metri