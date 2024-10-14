# Release Process

  This document outlines the release process for the project. It covers the steps to prepare, create, and publish a release, following [Semantic Versioning](https://semver.org/). Additionally, it includes a section on how to backport changes to previous versions. Our development follows a **chunk-based** process, which emphasizes incremental and manageable changes.


## 1. Preparation

### 1.1. Review Issues and Pull Requests
- Ensure all relevant issues are closed and pull requests are merged.
- Review the status of any pending issues or PRs that might affect the release.

### 1.2. Update Documentation
- Ensure that all user-facing documentation, including `README.md`, `CHANGELOG.md`, and any API documentation, is up-to-date.
- Add or update the `CHANGELOG.md` with the latest changes, grouped under the appropriate categories: Added, Changed, Deprecated, Removed, Fixed, and Security.

### 1.3. Version Bumping
- Determine the new version number based on the nature of the changes:
  - **MAJOR** version when making incompatible API changes,
  - **MINOR** version when adding functionality in a backward-compatible manner, and
  - **PATCH** version when making backward-compatible bug fixes.

## 2. Creating the Release

### 2.1. Final Checks
- Run all tests and ensure the build is passing.
- Verify that all dependencies are up-to-date and compatible.
- Conduct a final review of the codebase for any pending changes or clean-up tasks.

### 2.2. Update Version in `pom.xml`
    ```xml
    <version>1.2.0</version> <!-- Example for a minor release -->
    ```

### 2.3. Commit Version Update
    ```bash
    git add pom.xml
    git commit -m "Bump version to 1.2.0"
    ```

### 2.4. Tagging the Release
- Tag the release with the new version number:
  ```bash
  git tag -a v1.2.0 -m "Release v1.2.0"
  ```

- Push the tag to the repository:
  ```bash
  git push origin v1.2.0
  ```

### 2.5. Creating a GitHub Release
  - Navigate to the [GitHub releases page](https://github.com/mycloudnexus/kraken/releases) of your repository.
  - Click on "Draft a new release".
  - Select the tag that you just pushed (e.g., `v1.2.0`).
  - Fill in the release title (e.g., `v1.2.0`) and description. The description should include:
    - A summary of the changes.
    - A link to the `CHANGELOG.md` entry for the release.
    - Acknowledge contributors, if any.

  - Click "Publish release".

## 3. Backporting Changes

  Backporting involves applying important bug fixes or features to an older version of the project. Here's how to do it:

### 3.1. Identify the Commits to Backport
  Determine which commits need to be backported from the main branch.

### 3.2. Check Out the Target Branch
    ```bash
    git checkout release/1.1
    ```

### 3.3. Cherry-Pick the Commits

    ```bash
    git cherry-pick <commit-hash>
    ```

    For multiple commits:
    ```bash
    git cherry-pick <commit-hash1> <commit-hash2>
    ```

### 3.4. Resolve Conflicts (if any)

 If conflicts arise during cherry-picking, manually resolve them. Then continue:

    ```bash
    git add <resolved-file>
    git cherry-pick --continue
    ```

    To abort the process:
    ```bash
    git cherry-pick --abort
    ```

### 3.5. Test the Changes
 Ensure that the backported changes work correctly in the older version.

### 3.6. Commit and Push
    ```bash
    git push origin release/1.1
    ```

### 3.7. Version Bumping

  Based on semantic versioning, for this case, need to increase the PATCH version in `MAJOR.MINOR.PATCH`
  
  1. **Update Version in `pom.xml`**:
      ```xml
      <version>1.1.1</version> <!-- Example for a patch release -->
      ```

  3. **Commit Version Update**:
      ```bash
      git add pom.xml
      git commit -m "Bump version to 1.1.1"
      ```

### 3.7. Tag the Backported Release
    ```bash
    git tag -a v1.1.1 -m "Backport changes to 1.1.1"
    git push origin v1.1.1
    ```

### 3.8. Create a Backported Release on GitHub
    - Follow the same steps as in the 2.5. Creating a GitHub Release section.


## 4. Handling Hotfixes

### 4.1. Creating a Hotfix Branch
- For urgent fixes, create a hotfix branch from the last stable release tag:
  ```bash
  git checkout -b hotfix-v1.2.1 v1.2.0
  ```

### 4.2. Applying and Releasing the Hotfix
- Apply the necessary fix, update tests, and bump the patch version.
- Follow the standard release process, ensuring the hotfix is merged into the relevant branches.
