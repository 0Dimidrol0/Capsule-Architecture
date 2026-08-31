# Publishing

Capsule uses the same release model as DeviceMonitor: the Vanniktech Maven Publish plugin,
in-memory signing in CI, a tag-derived version, Maven Central release, and a generated GitHub
release.

## Published artifacts

All artifacts use the `io.github.0dimidrol0` group:

- `capsule-core`
- `capsule-base-viewmodel`
- `capsule-base-fragment-xml`
- `capsule-debug`
- `capsule-middleware`
- `capsule-network`
- `capsule-navigation-compose`
- `capsule-navigation-xml`

Sample applications are never published.

## Versioning

Local and branch builds use `VERSION_NAME` from `gradle.properties`, currently
`0.1.0-SNAPSHOT`. A GitHub tag overrides it: tag `v0.1.0` publishes version `0.1.0`.

Accepted release tag examples:

- `v0.1.0`
- `v0.1.0-rc.1`

## GitHub secrets

Configure these repository Actions secrets before creating a release tag:

- `MAVEN_CENTRAL_USERNAME`: Maven Central publishing token username
- `MAVEN_CENTRAL_PASSWORD`: Maven Central publishing token password
- `SIGNING_IN_MEMORY_KEY`: ASCII-armored private GPG key
- `SIGNING_IN_MEMORY_KEY_PASSWORD`: private key passphrase

Credentials and private signing keys must never be stored in `gradle.properties` or committed to
the repository.

## Release flow

1. Ensure the `master` branch CI is green.
2. Create and push a version tag, for example `git tag v0.1.0` and `git push origin v0.1.0`.
3. The `Publish Release` workflow verifies the project.
4. All eight artifacts are signed, uploaded, and released to Maven Central.
5. A GitHub Release is generated from the tag.

Manual `workflow_dispatch` runs verification only. Publishing is intentionally restricted to
version tags.

## Local verification

Use JDK 17 and run:

```shell
./gradlew verifyKotlinFileLayout test lint assembleRelease --no-daemon
```

To inspect a publication without uploading it:

```shell
./gradlew :capsule-core:generatePomFileForMavenPublication
```

The generated POM is under `capsule-core/build/publications/maven/`.
