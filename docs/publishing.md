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

Credentials and private signing keys must never be stored in the repository's `gradle.properties`
or committed to the repository.

## Local signing with GnuPG

When `signingInMemoryKey` is not supplied, all library modules use `signing { useGpgCmd() }`.
This uses an existing private key in your local GnuPG keyring, without exporting it. The
`signingInMemoryKey` configuration takes precedence so GitHub Actions keeps using its Secrets.

Install GnuPG (Gpg4win on Windows), then find your signing key:

```shell
gpg --list-secret-keys --keyid-format LONG
```

Put these properties in your **user-level** `~/.gradle/gradle.properties`
(`%USERPROFILE%\.gradle\gradle.properties` on Windows), not the repository file:

```properties
signing.gnupg.executable=gpg
signing.gnupg.useLegacyGpg=false
signing.gnupg.keyName=YOUR_SIGNING_KEY_FINGERPRINT
signing.gnupg.passphrase=YOUR_PRIVATE_KEY_PASSPHRASE

mavenCentralUsername=YOUR_CENTRAL_TOKEN_USERNAME
mavenCentralPassword=YOUR_CENTRAL_TOKEN_PASSWORD
```

Use `useLegacyGpg=false` with GnuPG 2.x. The `passphrase` unlocks the private key; it is not
your Maven Central password and does not replace the key itself. Restrict access to this local
file. If `passphrase` is omitted, GnuPG can ask for it through `gpg-agent` instead.

If you previously exported an in-memory key into this PowerShell session, clear it to select
GnuPG signing (also remove any `signingInMemoryKey` property from your user Gradle configuration):

```powershell
Remove-Item Env:ORG_GRADLE_PROJECT_signingInMemoryKey -ErrorAction SilentlyContinue
Remove-Item Env:ORG_GRADLE_PROJECT_signingInMemoryKeyId -ErrorAction SilentlyContinue
Remove-Item Env:ORG_GRADLE_PROJECT_signingInMemoryKeyPassword -ErrorAction SilentlyContinue
```

With JDK 17 selected, first verify signing without uploading anything:

```powershell
.\gradlew.bat :capsule-core:signMavenPublication "-PVERSION_NAME=0.1.0" --no-daemon
```

Then publish all eight libraries:

```powershell
.\gradlew.bat publishAndReleaseToMavenCentral "-PVERSION_NAME=0.1.0" --no-daemon
```

Do not reuse a compromised private key or an already published release version. See the
[Gradle signing documentation](https://docs.gradle.org/current/userguide/signing_plugin.html#sec:using_gpg_agent)
for GnuPG configuration details.

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
