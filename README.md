# Antigravity Java SDK Playground

A playground project demonstrating how to use the [unofficial Antigravity SDK for Java](https://github.com/glaforge/antigravity-java-sdk).

This SDK allows you to build, configure, host, and execute AI agents in Java, bridging the gap for enterprise Java developers who want to harness the power of Antigravity with Gemini models.

---

## Prerequisites

- **Java**: Java 21 or newer (tested on GraalVM / OpenJDK 21+)
- **Gemini API Key**: Set your `GEMINI_API_KEY` environment variable:
  ```bash
  export GEMINI_API_KEY="your-gemini-api-key"
  ```
  *(You can obtain a key at [Google AI Studio](https://aistudio.google.com/app/api-keys))*.
- Alternatively, if using **Vertex AI**, configure Application Default Credentials (ADC):
  ```bash
  gcloud auth application-default login
  export GOOGLE_CLOUD_PROJECT="your-project-id"
  export GOOGLE_CLOUD_LOCATION="us-central1"
  ```

---

## Project Setup

The project uses Maven with the included Maven Wrapper (`./mvnw`).

### 1. Synchronize Native Harness Engine

The Antigravity SDK uses a native Go harness (`localharness`) to manage agent state, streaming, and tool dispatching. A helper script is provided to automatically fetch the matching binary for your platform from the upstream wheel:

```bash
./sync-harness.sh
```

Supported platform slices:
- macOS (Apple Silicon `osx-aarch64` and Intel `osx-x86_64`)
- Linux (`linux-x86_64` and `linux-aarch64`)
- Windows (`windows-x86_64` and `windows-aarch64`)

### 2. Build the Project

```bash
./mvnw clean compile
```

### 3. Run the Hello World Sample

```bash
./mvnw exec:java
```

Or execute directly by pointing to the main class:

```bash
./mvnw exec:java -Dexec.mainClass="io.github.glaforge.samples.HelloWorldAgent"
```

---

## Sample Code

Here is the simple Hello World agent sample in [`HelloWorldAgent.java`](src/main/java/io/github/glaforge/samples/HelloWorldAgent.java):

```java
package io.github.glaforge.samples;

import io.github.glaforge.antigravity.Agent;
import io.github.glaforge.antigravity.AgentConfig;
import io.github.glaforge.antigravity.AgentResponse;

import java.util.concurrent.TimeUnit;

public class HelloWorldAgent {

    public static void main(String[] args) {
        System.out.println("=== Antigravity Java SDK - Hello World ===");

        AgentConfig config = AgentConfig.builder()
                .instructions("You are a helpful and concise AI assistant.")
                .build();

        try (Agent agent = new Agent(config)) {
            String prompt = "Hello! Please introduce yourself in one or two sentences.";
            System.out.println("User: " + prompt);

            AgentResponse response = agent.chat(prompt).get(120, TimeUnit.SECONDS);

            System.out.println("\nAgent:\n" + response.text());
        } catch (Exception e) {
            System.err.println("Error running agent: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

---

## Bundled Agent Skill

This playground project includes the official [Agent Skill](skills/antigravity-sdk-java/SKILL.md) for the **Antigravity SDK for Java**:

- [`skills/antigravity-sdk-java/SKILL.md`](skills/antigravity-sdk-java/SKILL.md) — Main instructions and overview.
- [`skills/antigravity-sdk-java/references/api-reference.md`](skills/antigravity-sdk-java/references/api-reference.md) — API reference for `AgentConfig`, capabilities, tool annotations (`@Tool`), dynamic tools, multimodal input, and session state.
- [`skills/antigravity-sdk-java/references/security-and-hooks.md`](skills/antigravity-sdk-java/references/security-and-hooks.md) — Policies (`denyIf`, `askUser`, `allowTools`) and the 3-tier lifecycle hooks architecture.
- [`skills/antigravity-sdk-java/references/streaming-and-reactive.md`](skills/antigravity-sdk-java/references/streaming-and-reactive.md) — Streaming tokens and Reactive Streams integration (`Flow.Publisher`).

---

## Maven Dependency

The playground uses version `0.2.12` of the SDK:

```xml
<dependency>
    <groupId>io.github.glaforge.antigravity</groupId>
    <artifactId>antigravity-sdk-wrapper</artifactId>
    <version>0.2.12</version>
</dependency>
```
