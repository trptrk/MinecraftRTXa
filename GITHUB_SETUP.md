# GitHub Repository Setup Guide

This guide will help you set up your RTX Mod repository on GitHub with all the necessary configurations for automated building, testing, and publishing.

## 🚀 Initial Repository Setup

### 1. Create GitHub Repository
1. Go to [GitHub](https://github.com) and create a new repository
2. Repository name: `minecraft-rtx-fabric-mod`
3. Description: `Hardware-accelerated RTX mod for Minecraft 1.20.4 using Fabric`
4. Set to Public or Private as desired
5. Initialize with README (or push existing code)

### 2. Configure Repository Settings
Go to your repository settings and configure:

**General Settings:**
- ✅ Enable Issues
- ✅ Enable Projects  
- ✅ Enable Wiki
- ✅ Enable Discussions (optional)

**Branches:**
- Set `main` as default branch
- Create `develop` branch for development
- Set up branch protection rules for `main`:
  - Require pull request reviews
  - Require status checks to pass
  - Require branches to be up to date

## 🔐 Secrets Configuration

Add the following secrets in **Settings > Secrets and variables > Actions**:

### Required Secrets
```
GITHUB_TOKEN          # Automatically provided by GitHub
```

### Optional Publishing Secrets
```
CURSEFORGE_TOKEN      # For CurseForge publishing
MODRINTH_TOKEN        # For Modrinth publishing  
DISCORD_WEBHOOK       # For Discord notifications
```

### How to Get Publishing Tokens

**CurseForge:**
1. Go to [CurseForge Core API](https://console.curseforge.com/)
2. Generate API token
3. Add as `CURSEFORGE_TOKEN` secret

**Modrinth:**
1. Go to [Modrinth Account Settings](https://modrinth.com/settings/account)
2. Create Personal Access Token
3. Add as `MODRINTH_TOKEN` secret

**Discord Webhook:**
1. Create Discord webhook in your server
2. Add webhook URL as `DISCORD_WEBHOOK` secret

## 📝 Update Configuration Files

### 1. Update build.gradle
Replace placeholders in `build.gradle`:
```gradle
pom {
    name = 'RTX Mod'
    description = 'Hardware-accelerated ray tracing mod for Minecraft using OpenGL compute shaders'
    url = 'https://github.com/YOUR_USERNAME/minecraft-rtx-fabric-mod' // Update this
    
    developers {
        developer {
            id = 'YOUR_USERNAME'        // Update this
            name = 'Your Name'          // Update this
            email = 'your.email@example.com' // Update this
        }
    }
    
    scm {
        connection = 'scm:git:git://github.com/YOUR_USERNAME/minecraft-rtx-fabric-mod.git' // Update this
        developerConnection = 'scm:git:ssh://github.com/YOUR_USERNAME/minecraft-rtx-fabric-mod.git' // Update this
        url = 'https://github.com/YOUR_USERNAME/minecraft-rtx-fabric-mod' // Update this
    }
}
```

### 2. Update publish.yml
Replace placeholders in `.github/workflows/publish.yml`:
```yaml
curseforge-id: YOUR_CURSEFORGE_PROJECT_ID  # Get from CurseForge project
modrinth-id: YOUR_MODRINTH_PROJECT_ID      # Get from Modrinth project
```

### 3. Update dependabot.yml
Replace placeholders in `.github/dependabot.yml`:
```yaml
reviewers:
  - "YOUR_USERNAME"  # Replace with your GitHub username
assignees:
  - "YOUR_USERNAME"  # Replace with your GitHub username
```

## 🏷️ Labels Configuration

Create the following labels in **Issues > Labels**:

| Label | Color | Description |
|-------|--------|-------------|
| `bug` | `#d73a49` | Something isn't working |
| `enhancement` | `#a2eeef` | New feature or request |
| `dependencies` | `#0366d6` | Pull requests that update a dependency |
| `gradle` | `#fbca04` | Related to Gradle build system |
| `github-actions` | `#28a745` | Related to CI/CD workflows |
| `rtx` | `#6f42c1` | RTX/Ray tracing related |
| `performance` | `#e99695` | Performance improvements |
| `shader` | `#f9d0c4` | Shader-related changes |
| `documentation` | `#0075ca` | Improvements or additions to documentation |
| `good first issue` | `#7057ff` | Good for newcomers |

## 🔄 Workflow Overview

### Build Workflow (`.github/workflows/build.yml`)
**Triggers:** Push to main/develop, PRs to main
**Features:**
- ✅ Multi-Java version testing (17, 21)
- ✅ Gradle wrapper validation
- ✅ Dependency caching
- ✅ Mod structure validation
- ✅ Automatic release publishing
- ✅ Code quality checks

### Development Workflow (`.github/workflows/dev-build.yml`)  
**Triggers:** Push to develop, PRs, manual dispatch
**Features:**
- ✅ Development build generation
- ✅ PR build artifacts
- ✅ Shader validation
- ✅ Cross-platform testing
- ✅ Automatic PR comments

### Publishing Workflow (`.github/workflows/publish.yml`)
**Triggers:** GitHub releases
**Features:**
- ✅ CurseForge publishing
- ✅ Modrinth publishing  
- ✅ GitHub Packages
- ✅ Discord notifications

### Dependabot (`.github/dependabot.yml`)
**Schedule:** Weekly on Mondays
**Features:**
- ✅ Automatic dependency updates
- ✅ Grouped updates for related libraries
- ✅ Automatic review assignment

## 📋 Release Process

### 1. Creating Releases
1. Update version in `gradle.properties`
2. Create release notes
3. Create git tag: `git tag v1.0.0`
4. Push tag: `git push origin v1.0.0`
5. Create GitHub release from tag

### 2. Automated Release Process
When you create a GitHub release:
1. ✅ Code is automatically built
2. ✅ JAR is attached to release
3. ✅ Published to CurseForge (if configured)
4. ✅ Published to Modrinth (if configured)
5. ✅ Discord notification sent (if configured)

## 🧪 Testing the Setup

### 1. Test Build Workflow
1. Make a small change and push to `develop`
2. Check if Actions workflow runs successfully
3. Verify artifacts are uploaded

### 2. Test PR Workflow
1. Create a branch and make changes
2. Open a PR to `main` or `develop`
3. Check if PR validation runs
4. Verify build artifact comment is posted

### 3. Test Release Process
1. Create a test release (mark as pre-release)
2. Verify all publishing workflows complete
3. Check if JAR is properly attached

## 🛠️ Development Workflow

### Recommended Branch Strategy
```
main     ←── stable releases
└── develop  ←── active development
    └── feature/rtx-improvements  ←── feature branches
    └── bugfix/shader-compile-error  ←── bug fixes
```

### Git Flow
1. Create feature branch from `develop`
2. Make changes and commit
3. Push branch and create PR to `develop`
4. After review and CI passes, merge to `develop`
5. Periodically merge `develop` to `main` for releases

## 📊 Monitoring

### GitHub Actions
- Monitor workflow runs in **Actions** tab
- Set up email notifications for failed builds
- Review artifact downloads and usage

### Dependencies  
- Monitor Dependabot PRs weekly
- Review security alerts
- Keep dependencies up to date

### Performance
- Track build times and optimize as needed
- Monitor artifact sizes
- Review test execution times

## 🔧 Troubleshooting

### Common Issues

**Build Failures:**
- Check Java version compatibility
- Verify Gradle wrapper permissions
- Review dependency conflicts

**Publishing Failures:**
- Verify API tokens are correct
- Check project IDs for CurseForge/Modrinth
- Ensure release format is correct

**Workflow Permissions:**
- Verify repository secrets are accessible
- Check workflow permissions in repo settings
- Ensure GITHUB_TOKEN has adequate permissions

## 🎯 Next Steps

After setup is complete:
1. ✅ Verify all workflows run successfully
2. ✅ Create first release to test publishing
3. ✅ Set up project documentation
4. ✅ Configure issue templates
5. ✅ Set up project boards for tracking
6. ✅ Create contributor guidelines

## 📞 Support

For issues with this setup:
1. Check GitHub Actions logs
2. Review workflow configurations
3. Consult GitHub Actions documentation
4. Open an issue with setup problems

---

🚀 **Your RTX mod is now ready for professional development with automated CI/CD!**
