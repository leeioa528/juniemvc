# Jetbrains AI Prompts

Inspect the requirements document. Analyze for accuracy and completeness. Make recommendations for 
how we can improve this document. Implement the improvements in a revised version.

------------------

For a React project using Shadcn, inspect the technologies used. Are there any missing dependencies?

--------------------
What information is needed for the vite configuration?

--------------------
suggest further improvements for the guide?

--------------------
Can the Guide outline be improved?

instuctors query to Jetsbrains AI
------------------------------------
Inspect the requirements.md file. Generate a prompt to create an implementation plan from this file.

My query to Jetsbrains AI
------------------------------------
Inspect the requirementsLee.md file. Generate a prompt to create an implementation plan from this file.
# Junie Prompts
Inspect the file `prompts/requirements-prompt-draft.md`. Use this file to create a developer guide to
implement a React front end for this project. Update and improve this developer guide using the context 
of this project. The guide should be organized into clear actionable steps. 

Write the improved guide to `prompts/requirements.md`.

-------------------------

**THIS DID NOT WORK**
Analyze the `prompts/requirements.md` file and create a detailed plan for the improvements of this project.
Write the plan to `prompts/plan.md` file.

-------------------------------------
**Improved Prompt via Gemini mine**
Based on the `requirementsLee.md` file, here is a generated prompt to create an implementation plan:


Based on the file `prompts/requirementsLee.md`, generate a detailed implementation plan for creating the React frontend for the Spring Boot project. The plan should be structured as a series of epics, with each epic broken down into smaller, actionable user stories or tasks. The goal is to create a clear roadmap for a development team to follow.

The implementation plan should cover the following key areas, as detailed in the requirements document:

1.  **Project Foundation & Setup:**
    *   Create the initial React project with Vite and TypeScript in the `src/main/frontend` directory.
    *   Install all necessary dependencies for the core application, UI libraries (Tailwind CSS, shadcn, Radix), tooling, and testing.
    *   Initialize and configure Tailwind CSS and shadcn UI.

2.  **Build Integration & Configuration:**
    *   Configure Vite for the development environment, including setting up the proxy to the Spring Boot backend.
    *   Configure Vite for production builds, ensuring output is directed to `src/main/resources/static`.
    *   Integrate the frontend build into the Maven lifecycle using the `frontend-maven-plugin` for dependency installation and building.
    *   Configure the `maven-clean-plugin` to handle the cleanup of generated frontend assets.
    *   Set up `package.json` scripts for development, building, testing, and API client generation.

3.  **API Integration & Data Layer:**
    *   Generate the TypeScript API client and types from the OpenAPI specification (`openapi/openapi/openapi.yaml`).
    *   Implement a reusable Axios instance with global interceptors for API calls and error handling.
    *   Create a service layer with modules for each resource (Beers, Customers, Beer Orders), encapsulating data-fetching logic.

4.  **Application Architecture & UI Shell:**
    *   Implement the main application routing using React Router, including a nested layout structure.
    *   Develop a shared `AppLayout` component for consistent navigation and structure across pages.
    *   Create custom hooks (e.g., `useBeers`) to manage state, loading, and errors for data fetching.

5.  **Feature Implementation (CRUD per Resource):**
    *   For **Beers**, create components and pages for:
        *   Listing all beers with pagination.
        *   Viewing beer details.
        *   Creating, updating, and deleting beers using forms and dialogs.
    *   For **Customers**, create components and pages for:
        *   Listing all customers.
        *   Viewing customer details.
        *   Creating, updating, and deleting customers.
    *   For **Beer Orders**, create components and pages for:
        *   Listing all orders.
        *   Viewing order details.
        *   Creating and updating orders.

6.  **Quality & Testing:**
    *   Set up the testing environment using Vitest and React Testing Library.
    *   Configure ESLint and Prettier to enforce code quality and consistent style.
    *   Write unit and integration tests for key components, hooks, and services.

7.  **Documentation & Workflow:**
    *   Document the frontend development workflow in `.junie/guidelines.md`.
    *   Verify and document the local development process (concurrently running backend and frontend) and the production build process.

Please ensure the plan is logical, sequential, and provides enough detail for developers to understand the scope of each task.
Write the plan to `prompts/plan.md` file.











-------------------------------------
**Improved Prompt via Gemini instuctors **

Based on the file`prompts/requirements.md`, generate a detailed implementation plan for creating the React frontend for the Spring Boot Beer Service. `requirements.md`
The plan should be structured as a series of epics, with each epic broken down into smaller, actionable user stories or tasks. The goal is to create a clear roadmap for a development team to follow.
The implementation plan should cover the following key areas, as detailed in the requirements document:
1. **Project Setup & Configuration:**
    - Creating the initial React project with Vite and TypeScript.
    - Configuring Vite for development and production builds.
    - Setting up environment variables.
    - Installing and configuring all UI and utility libraries (Tailwind CSS, Shadcn, Radix, etc.).
    - Establishing code quality standards with ESLint and Prettier.

2. **API Integration:**
    - Generating TypeScript types and API client code from the OpenAPI specification.
    - Creating a reusable API service layer with Axios, including global error handling.
    - Implementing service modules for each resource (Beers, Customers, Beer Orders).

3. **Build Process Integration:**
    - Configuring the `frontend-maven-plugin` in the `pom.xml` to integrate the frontend build process with the main Maven lifecycle (installing dependencies, building, and testing).
    - Configuring the `maven-clean-plugin` to handle cleanup of generated frontend assets.

4. **Component Development & Routing:**
    - Setting up the main application routing using React Router.
    - Developing a set of reusable UI components based on Shadcn for forms, tables, dialogs, and navigation.
    - Creating pages/views for each primary feature.

5. **Feature Implementation (per resource):**
    - For **Beers**, create components for:
        - Listing all beers with filtering and pagination.
        - Viewing beer details.
        - Creating, updating, and deleting beers.

    - For **Customers**, create components for:
        - Listing all customers.
        - Viewing customer details.
        - Creating, updating, and deleting customers.

    - For **Beer Orders**, create components for:
        - Listing all orders.
        - Viewing order details.
        - Creating and updating orders.
        - Managing order shipments.

6. **Testing:**
    - Setting up the testing environment with Jest and React Testing Library.
    - Writing unit and integration tests for components and services.

Please ensure the plan is logical, sequential, and provides enough detail for developers to understand the scope of each task.

Write the plan to `prompts/plan.md` file.

--------------------------------------

Create a detailed enumerated task list according to the suggested enhancements plan in
`prompts/plan.md` Task items should have a placeholder [ ] for marking as done [x] upon task completion.
Write the task list to `prompts/tasks.md` file.

-------------------------------------

Complete the task list `prompts/tasks.md`. Inspect the `prompts/requirements.md` and `prompts/plan.md` and task.md (task list).
Implement the tasks in the task list. Focus on completing the tasks in order. Mark the task complete as it is done
using [x]. As each step is completed, it is very important to update the task list mark and the task as done [x]. 