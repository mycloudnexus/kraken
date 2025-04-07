Every development related task in Kraken project will be tracked by using a Github issue.

**1. Key Properties**

There are 3 key properties that contribute to effective GitHub issue management :

* **Labels:**
    * **Category:** A single-select label classifying the issue type:
        * Bug
        * New Feature
        * Improvement
        * Documentation
        * Technical Design
    * **Release Branch Candidate:** (Optional) A label indicating the specific release branch (e.g., "1.1.0-candidate", "1.2.0-candidate") if the issue will be also targeted for a particular release branch beside main branch.
    * **"Merge Back to Main"**: This label is applied to issues fixed within a release branch first to ensure the fix is eventually incorporated into the main codebase later.

* **Projects:** indicates if the issue has been scheduled into a sprint or iteration.

* **Milestone:** Specifies the target release version for the issue (e.g., "2.0.0-snaphot.2", "1.0.0") and it also indicates which branch it is going to be fixed.
    * If a issue fixed in release/1.0 branch first with 1.1.0 as milestone, then later we must clone it into another new issue with a milestone from main branch.

**2. Workflow**

* **New Issue:**
    * Reporter creates a new issue.
    * Reporter selects the appropriate **Category** label.
    * Reporter optionally selects a **Project** and **Milestone** if known.

* **PO Review:**
    * Product Owner (PO) reviews the issue.
    * PO verifies and corrects the **Category** label, **Project**, and **Milestone** as needed.
    * PO adds the **Release Branch Candidate** label if applicable.
    * PO assigns the issue to a developer

* **Development:**
    * Developer moves the issue to In Progress once he/she starts to work on it.
    * Developer creates a Pull Request (PR) and links the issue to the PR.
    * Developer move the issue the In Review once the PR is ready for review.
    * Github project Workflow(if works) will move the issue to Merged once the PR is merged, if not, developer has to Move the status by themselves.
    * Developer(BE) updates API detail in the issues for later FE integration.

* **QA:**
    * QA creates a docker image with github Actions if there are some issues in the merged queue and deploy it into testing env.
    * QA moves those merged issues to "Ready for Testing" once the deployment is done.
    * QA tests the changes in testing env.
    * QA adds comments to the issue with test result.
    * QA moves the issue to a "Ready for Release" status by using project status upon successful testing. If failed, moves it back to "In progress".

