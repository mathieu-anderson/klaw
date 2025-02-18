# 🪸 Klaw's new frontend app

- Please be aware of our [Code of Conduct](../CODE_OF_CONDUCT.md) ❤️

## Table of content

* [About](#about)
* [Installation and usage](#installation-and-usage)
  + [Usage: How to run Coral in development](#usage-how-to-run-coral-in-development)
    - [Scripts used and what they execute](#scripts-used-and-what-they-execute)
  + [Usage: How to run the Coral in build](#usage-how-to-run-the-frontend-app-in-development)
* [Tech stack](#tech-stack)
  + [App development](#app-development)
  + [Testing](#testing)
  + [Linting and code formatting](#linting-and-code-formatting)
* [Styling](#styling)
* [Documentation](#documentation)
  + [Tip](#tip)
* [More detailed documentation](#more-detailed-documentation)

## About

`/coral` contains a React app. It's the rewrite of the existing Klaw frontend.

## Installation and usage

ℹ️ Coral uses `pnpm run` as a package manager. Read their official documentation [how to install](https://pnpm.io/installation) it.

- navigate to this directory
- run `pnpm install`
- run `pnpm dev` to start the frontend app in development mode _with remote API_
- run `pnpm dev-without-api` to start the frontend app in development mode without api

ℹ️ **Using a remote API**
We recommend doing coral development with a remote API.
Please see our documentation: [Development with remote API](docs/development-with-remote-api.md).

ℹ️ **Developing without remote API**
If you want to run Coral without an API, you can do that, too.
Please see our documentation: [Mocking an API for development](docs/mock-api-for-development.md)

### Usage: How to run Coral in development

ℹ️ You can see all our scripts in the [`package.json`](package.json).
You can also run `pnpm` in your console to get a list of all available scripts.

#### Scripts used and what they execute
- `build`: builds the frontend app for production
- `dev`: starts the frontend app for development
- `lint`: runs a code format check and if no error is found, lints the code.
  - the linting script does not mutate your code. See [Linting and code formatting](#linting-and-code-formatting) for more info.
- `preview`: builds a preview production build _locally_
- `reformat`: runs the code formatter (prettier) and reformat all code
- `test-dev`: runs all test tests related to changed files in a watch mode
- `test`: runs all tests one time
- `tsc`: runs the TypeScript compiler

### Usage: How to run Coral inside the Klaw application

1. in `/coral`, run `make enable-coral-in-springboot` (see our [Makefile](Makefile)). This will enable Coral in Klaw. It also add the current Coral build files in the right place. 
2. go to the root directory and follow the [instructions on how to run Klaw](../README.md#Install)
3. Klaw will run in `http://localhost:9097` (note: it's `http` instead of `https`!)

## Tech stack

### App development
- TypeScript - 📃 [documentation](https://www.typescriptlang.org/) | 🐙 [repository](https://github.com/microsoft/TypeScript)
- React - 📃 [documentation](https://reactjs.org/docs/getting-started.html) | 🐙 [repository](https://github.com/facebook/react/)
- Vite - 📃 [documentation](https://vitejs.dev/guide/) | 🐙 [repository](https://github.com/vitejs/vite)

### Testing
- Jest - 📃 [documentation](https://jestjs.io/docs/getting-started) | 🐙 [repository](https://github.com/facebook/jest)
- React Testing Library - 📃 [documentation](https://testing-library.com/docs/react-testing-library/intro/) | 🐙 [repository](https://github.com/testing-library/react-testing-library)

📃 You can find more detailed information about testing in our docs for [Frontend Testing](docs/frontend-testing.md).

### Linting and code formatting
How we keep our app's codebase looking consistent and nice 💅🏼

- [Prettier](https://prettier.io/) for code formatting
- [ESlint](https://eslint.org/) and various plugins for linting

The script `lint` runs a prettier check and eslint after. It does not mutate your code in any way. If you want to format your code or let eslint fix it for you, you can run:

1. First: `pnpm reformat` (prettier formatting)
2. After that: `pnpm eslint --fix` (eslint in fix mode)

ℹ️ It's convenient to let prettier and eslint auto-format your code "on save" by your IDE or editor.

## Styling

We use the component library of Aiven's design system:
- 📃 [documentation](https://aiven-ds.netlify.app/)
- the repository is open source, but `private` at the moment

As a rule, please don't use css classes from the design system. All styles should be created by using the existing components and their properties.

__🔄 Work in progress related to styles__
- We plan to add css variables based on the design system's tokens.
- We plan to have a custom theme for Klaw. This will be used instead of the Aiven theme.

## Documentation

We've a more detailed document about our thinking about [Docmentation](docs/documentation.md).

### Tip
You can use `alex` ( 📃 [documentation]( https://alexjs.com/) | 🐙 [repository](https://github.com/get-alex/alex)) as an **optional** linting tool. It checks text documents and highlights language that potentially could be insensitive or inconsiderate.

- run it with `pnpm --silent dlx alex` to lint text files.


## More detailed documentation

We provide more documentation on:

- Our commitment to [Accessibility](docs/accessibility.md)
- Detailed overview of the [Directory Structure](docs/directory-structure.md)
- Our thinking about [Docmentation](docs/documentation.md)
- More information about [Frontend Testing](docs/frontend-testing.md)
- [Development with remote API](docs/development-with-remote-api.md)

