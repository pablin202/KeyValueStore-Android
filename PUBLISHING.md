# Publishing to Maven Central - Complete Guide

This guide will help you publish the KeyValueStore library to Maven Central so developers can use it with:

```kotlin
dependencies {
    implementation("io.github.pablin202:kvstore:1.0.0")
}
```

## Prerequisites

- GitHub account
- GPG installed (for signing artifacts)
- Sonatype account

## Step 1: Install GPG (if not already installed)

### Windows

Download and install GPG4Win: https://www.gpg4win.org/

### Verify Installation

```bash
gpg --version
```

## Step 2: Generate GPG Key

```bash
# Generate a new GPG key
gpg --gen-key

# You'll be prompted for:
# - Name: Pablo Molina (or your real name)
# - Email: molinapablod@gmail.com (must match your GitHub email)
# - Password: Choose a strong password (you'll need this later)
```

### List your keys

```bash
gpg --list-secret-keys --keyid-format=long
```

Output will look like:
```
sec   rsa3072/ABCD1234EFGH5678 2024-01-15
      1234567890ABCDEFGHIJKLMNOPQRSTUV
uid                 Pablo Molina <molinapablod@gmail.com>
```

The key ID is `ABCD1234EFGH5678` (the last 8 characters after the slash).

### Export your public key to keyserver

```bash
# Replace ABCD1234EFGH5678 with your actual key ID
gpg --keyserver keys.openpgp.org --send-keys ABCD1234EFGH5678
gpg --keyserver keyserver.ubuntu.com --send-keys ABCD1234EFGH5678
```

### Export secret key ring (for Gradle)

```bash
# Windows
gpg --export-secret-keys -o C:\Users\Pablo Molina\.gnupg\secring.gpg

# Verify file exists
dir "C:\Users\Pablo Molina\.gnupg\secring.gpg"
```

## Step 3: Create Sonatype Account

1. Go to https://central.sonatype.com/
2. Click "Sign Up" or "Register"
3. Create account with GitHub (easiest option)
4. Verify your GitHub namespace `io.github.pablin202`
   - This is automatic if you sign in with GitHub
   - Your namespace will be `io.github.YOUR_GITHUB_USERNAME`

**Alternative (Legacy Portal):**
- https://s01.oss.sonatype.org/
- Create JIRA ticket to claim namespace
- Wait for approval (can take 2-48 hours)

## Step 4: Configure gradle.properties

1. Copy the template:
```bash
copy gradle.properties.template gradle.properties
```

2. Edit `gradle.properties` with your actual values:

```properties
# Library Metadata
PUBLISH_GROUP_ID=io.github.pablin202
PUBLISH_VERSION=1.0.0
PUBLISH_ARTIFACT_ID=kvstore

# Sonatype Credentials
ossrhUsername=your_sonatype_username
ossrhPassword=your_sonatype_password

# GPG Signing
signing.keyId=ABCD1234  # Last 8 chars of your GPG key
signing.password=your_gpg_password
signing.secretKeyRingFile=C:/Users/Pablo Molina/.gnupg/secring.gpg
```

⚠️ **NEVER commit gradle.properties to Git!** (already in .gitignore)

## Step 5: Test Local Build

```bash
# Clean and build
./gradlew clean :kvstore:build

# Test publishing to local Maven
./gradlew :kvstore:publishToMavenLocal

# Check if artifacts were created in:
# C:\Users\Pablo Molina\.m2\repository\io\github\pablin202\kvstore\1.0.0\
```

You should see:
- kvstore-1.0.0.aar (library)
- kvstore-1.0.0.aar.asc (signature)
- kvstore-1.0.0-sources.jar
- kvstore-1.0.0-javadoc.jar
- kvstore-1.0.0.pom

## Step 6: Publish to Maven Central

### First time setup verification

```bash
# Verify all configurations
./gradlew :kvstore:tasks --group publishing

# You should see:
# - publishReleasePublicationToSonatypeRepository
# - publishAllPublicationsToSonatypeRepository
```

### Publish!

```bash
# Publish to Sonatype staging
./gradlew :kvstore:publishReleasePublicationToSonatypeRepository
```

This will:
1. Build the library
2. Generate sources and javadoc JARs
3. Sign all artifacts with GPG
4. Upload to Sonatype staging repository

### What happens next?

#### New Portal (central.sonatype.com)
- Artifacts appear in staging repository
- Click "Publish" button
- Wait 15-30 minutes for sync to Maven Central
- Your library is live!

#### Legacy Portal (s01.oss.sonatype.org)
1. Login to https://s01.oss.sonatype.org/
2. Go to "Staging Repositories"
3. Find your repository (io.github.pablin202-XXXX)
4. Click "Close" (validates artifacts)
5. Wait for validation (5-10 minutes)
6. Click "Release" if validation passes
7. Wait 10-30 minutes for sync to Maven Central
8. Wait 2-4 hours for search.maven.org to index

## Step 7: Verify Publication

After 30 minutes, check:

1. **Maven Central Search**: https://search.maven.org/search?q=io.github.pablin202
2. **Direct artifact**: https://repo1.maven.org/maven2/io/github/pablin202/kvstore/1.0.0/
3. **Sonatype Search**: https://central.sonatype.com/artifact/io.github.pablin202/kvstore

## Step 8: Update README

Once published, update your README.md:

```markdown
## Installation

Add to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.pablin202:kvstore:1.0.0")
}
```

Or in `libs.versions.toml`:

```toml
[versions]
kvstore = "1.0.0"

[libraries]
kvstore = { module = "io.github.pablin202:kvstore", version.ref = "kvstore" }
```
```

## Publishing New Versions

### For patch/minor updates:

1. Update version in `gradle.properties`:
```properties
PUBLISH_VERSION=1.0.1
```

2. Update version in README.md

3. Commit changes:
```bash
git add .
git commit -m "chore: bump version to 1.0.1"
git tag v1.0.1
git push origin main --tags
```

4. Publish:
```bash
./gradlew clean :kvstore:publishReleasePublicationToSonatypeRepository
```

5. Release on Sonatype portal

6. Create GitHub release:
   - Go to https://github.com/pablin202/KeyValueStore-Android/releases
   - Click "Draft a new release"
   - Select tag v1.0.1
   - Describe what changed
   - Publish release

## Troubleshooting

### "Could not find signing key"

- Verify GPG key is exported: `gpg --list-secret-keys`
- Check `secring.gpg` exists in the path specified
- Ensure `signing.keyId` matches the last 8 characters

### "Unauthorized" when publishing

- Double-check Sonatype credentials in gradle.properties
- Try logging into https://s01.oss.sonatype.org/ manually
- Password might need to be in quotes if it has special characters

### "Package does not exist in registry"

- First time: You need to create the package on Sonatype
- Verify your namespace is approved (io.github.pablin202)

### Build fails with "Execution failed for task ':kvstore:signReleasePublication'"

- GPG password is wrong
- secring.gpg path is incorrect
- Try regenerating secring.gpg

## Security Best Practices

1. ✅ Never commit gradle.properties
2. ✅ Never commit .gpg files
3. ✅ Use strong GPG password
4. ✅ Backup your GPG keys securely
5. ✅ Use GitHub's OSSRH token (instead of password) for CI/CD
6. ✅ Enable 2FA on Sonatype account

## Reference Links

- Maven Central Portal: https://central.sonatype.com/
- Maven Central Requirements: https://central.sonatype.org/publish/requirements/
- GPG Guide: https://central.sonatype.org/publish/requirements/gpg/
- Publishing Plugin: https://docs.gradle.org/current/userguide/publishing_maven.html

## Support

If you encounter issues:
1. Check Sonatype status: https://status.maven.org/
2. Sonatype JIRA: https://issues.sonatype.org/
3. Stack Overflow: Tag `maven-central`

---

**Ready to publish?** Start with Step 1! 🚀
