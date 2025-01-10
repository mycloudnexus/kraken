Please all the pull request owner to self check your own PR to meet the following criterias before asking others to review:

- [ ] Ensure that you are not pushing configuration files containing sensitive information such as testing keys.
- [ ] Avoid large PRs,  Consider splitting if there are more than 500 line changes, In addition to line count, also consider the number of files being affected, An ideal amount is less than 10, 25-30 is generally too large with the exception of library upgrades, refactors, etc.
- [ ] If the PR is inevitably large, organize a short PR review meeting or discuss with reviewers.
- [ ] If the PR is to resolve a blocker, do not make unrelated changes in the same PR e.g. formatting changes, to minimize review time.
- [ ] Keep PRs focused, creating one PR per Acceptance Criteria entry
- [ ] Give context in the PR description, Include a clear description of the changes
- [ ] Link to the related github issues, Include screenshots if this will make the context clearer for the reviewer, for example for BPMN changes
- [ ] No Attempting to get more updates into a PR - and increasing its size - will lead to a delay in review timing
- [ ] The branch is up to date with the latest main branch (e.g. pull and merge develop in, rebase, etc..)
- [ ] No merge conflicts
- [ ] Sonacloud quality gate passed, and unit testing passed
- [ ] No "work in progress" or "hold" labels
- [ ] adherence to style guidelines
- [ ] dynamic feature flags for new features if needed
- [ ] follow secure development practices
- [ ] no secrets or sensitive data have been committed or logged
- [ ] no hardcoded values that may change depending on environment (these should be configured dynamically e.g. through environment variables, from the db etc)
- [ ] Review the Sonar Cloud scan results and flag any bugs, security issues or code smells that may still need to be addressed
