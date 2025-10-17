https://youtu.be/ZbrmXZrdtGE (demo, C.9)
# App README

# Build Pipeline

Esta pipeline GitHub Actions é executada sempre que há um push para a branch principal (main).
Ela realiza todas as etapas necessárias para compilar, gerar e disponibilizar o arquivo .jar do projeto.

Passos do Workflow:

1. Checkout do repositório: garante que o código mais recente esteja disponível.
2. Configuração do Java 21+: usando Temurin para compatibilidade máxima.
3. Compilação Maven: mvn clean package gera o arquivo .jar dentro da pasta target.
4. Copiar .jar para raiz: permite download direto pela interface web do GitHub.
5. Publicação do artefacto: garante que o .jar fique disponível como artefacto no workflow.
6. Verificação final: lista os arquivos gerados para conferência.

Excerto do Workflow:
```
name: Java Build Premium
on:
  push:
    branches:
      - main
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: 21
          distribution: temurin
      - run: mvn clean package
      - run: cp target/*.jar .
      - uses: actions/upload-artifact@v3
        with:
          name: my-app-jar
          path: "*.jar"
      - run: ls -lh target/ *.jar
```

## Project Structure

The sources of your App have the following structure:

```
src
├── main/frontend
│   └── themes
│       └── default
│           ├── styles.css
│           └── theme.json
├── main/java
│   └── [application package]
│       ├── base
│       │   └── ui
│       │       ├── component
│       │       │   └── ViewToolbar.java
│       │       ├── MainErrorHandler.java
│       │       └── MainLayout.java
│       ├── examplefeature
│       │   ├── ui
│       │   │   └── TaskListView.java
│       │   ├── Task.java
│       │   ├── TaskRepository.java
│       │   └── TaskService.java                
│       └── Application.java       
└── test/java
    └── [application package]
        └── examplefeature
           └── TaskServiceTest.java                 
```

The main entry point into the application is `Application.java`. This class contains the `main()` method that start up 
the Spring Boot application.

The skeleton follows a *feature-based package structure*, organizing code by *functional units* rather than traditional 
architectural layers. It includes two feature packages: `base` and `examplefeature`.

* The `base` package contains classes meant for reuse across different features, either through composition or 
  inheritance. You can use them as-is, tweak them to your needs, or remove them.
* The `examplefeature` package is an example feature package that demonstrates the structure. It represents a 
  *self-contained unit of functionality*, including UI components, business logic, data access, and an integration test.
  Once you create your own features, *you'll remove this package*.

The `src/main/frontend` directory contains an empty theme called `default`, based on the Lumo theme. It is activated in
the `Application` class, using the `@Theme` annotation.

## Starting in Development Mode

To start the application in development mode, import it into your IDE and run the `Application` class. 
You can also start the application from the command line by running: 

```bash
./mvnw
```

## Building for Production

To build the application in production mode, run:

```bash
./mvnw -Pproduction package
```

To build a Docker image, run:

```bash
docker build -t my-application:latest .
```

If you use commercial components, pass the license key as a build secret:

```bash
docker build --secret id=proKey,src=$HOME/.vaadin/proKey .
```

## Getting Started

The [Getting Started](https://vaadin.com/docs/latest/getting-started) guide will quickly familiarize you with your new
App implementation. You'll learn how to set up your development environment, understand the project 
structure, and find resources to help you add muscles to your skeleton — transforming it into a fully-featured 
application.
