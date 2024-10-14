# Developer User Guide

Welcome to the Developer User Guide for our open-source project! This guide provides all the necessary information to help you get started with development, contribute code, and follow our project standards.

## Table of Contents
1. [Getting Started](#getting-started)
2. [Development Workflow](#development-workflow)
3. [Running and Testing](#running-and-testing)
4. [Code Style](#code-style)
5. [Submitting Changes](#submitting-changes)
6. [Reporting Issues](#reporting-issues)
7. [Requesting Features](#requesting-features)
8. [Communication](#communication)
9. [Resources](#resources)

## Getting Started

### Prerequisites

Make sure you have the following software installed:
- Git
- Java Development Kit (JDK) 17 or higher
- Maven

### Fork the Repository

1. Go to the project repository on GitHub.
2. Click the "Fork" button to create a copy of the repository on your GitHub account.

### Clone Your Fork

```bash
git clone https://github.com/mycloudnexus/kraken.git
cd project-name
```

### Set Upstream Remote

```bash
git remote add upstream https://github.com/mycloudnexus/kraken.git
```

### Install Dependencies

Make sure you have the required dependencies installed. You can install them using:

```bash
mvn install
```

## Development Workflow

### Branching Model

We use the [Git Flow](https://nvie.com/posts/a-successful-git-branching-model/) branching model. Here are the primary branch types:

- `main`: The main branch containing production-ready code.
- `feature/*`: Feature branches for new features.
- `bugfix/*`: Bugfix branches for fixing issues.

### Creating a Feature Branch

```bash
git checkout main
git pull upstream main
git checkout -b feature/your-feature-name
```

### Making Changes

Make your changes in the feature branch. Ensure your code follows the project's coding standards and passes all tests.

## Running and Testing

### Running Tests

Run the tests to ensure your changes do not break existing functionality:

```bash
mvn test
```

### Package and Run the project

```bash
mvn package -DskipTests
```

There are three applications under kraken-app/

You can run the application via
```base
java -jar kraken-app/kraken-app-controller/target/*.jar
```

Then you can access the swagger-ui via
```
http://localhost:8000
```


## Code Style

We follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) for our code standards. Please ensure your code adheres to this style guide.

- Use meaningful variable and function names.
- Write clear and concise comments where necessary.

## Submitting Changes

### Creating a Pull Request

1. Go to your forked repository on GitHub.
2. Click the "Compare & pull request" button.
3. Provide a clear title and description for your pull request ([Title convention](https://github.com/amannn/action-semantic-pull-request)).
4. Submit the pull request.
5. Ensure all checks have passed before your pull request can be reviewed.

### Reviewing Pull Requests

Your pull request will be reviewed by project maintainers. Please be responsive to feedback and make any necessary changes.

## Reporting Issues

If you encounter any issues, please report them using the [GitHub Issues](https://github.com/mycloudnexus/kraken/issues) tracker. Provide as much detail as possible, including steps to reproduce the issue, your environment, and any relevant logs or screenshots.

## Requesting Features

To request a new feature, open a [GitHub Issue](https://github.com/mycloudnexus/kraken/issues) and provide a detailed description of the feature, including its purpose and how it should work.

## Communication

- For general questions and discussion, use the [GitHub Discussions](https://github.com/mycloudnexus/kraken/discussions) forum.

## Resources

- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Git Flow](https://nvie.com/posts/a-successful-git-branching-model/)

Thank you for contributing to our project! Your efforts help make this project better for everyone.
