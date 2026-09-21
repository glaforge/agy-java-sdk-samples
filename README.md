# Antigravity Java SDK Playground

A playground project demonstrating how to use the [unofficial Antigravity SDK for Java](https://github.com/glaforge/antigravity-java-sdk).

This SDK allows you to build, configure, host, and execute AI agents in Java, bridging the gap for enterprise Java developers who want to harness the power of Antigravity with Gemini models.

The native Go harness engine is automatically bundled directly within the SDK JAR dependency (`antigravity-sdk-wrapper`) across all major platforms (macOS ARM/Intel, Linux ARM/Intel, Windows x86/ARM).

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

## Building and Running

The project uses Maven with the included Maven Wrapper (`./mvnw`).

### 1. Build the Project

```bash
./mvnw clean compile
```

### 2. Run the Hello World Sample

```bash
./mvnw exec:java
```

Or execute directly by pointing to the main class:

```bash
./mvnw exec:java -Dexec.mainClass="io.github.glaforge.samples._01_HelloWorld"
```

---

## Sample Code

Here is the simple Hello World agent sample in [`_01_HelloWorld.java`](src/main/java/io/github/glaforge/samples/_01_HelloWorld.java):

```java
package io.github.glaforge.samples;

import io.github.glaforge.ansiren.MarkdownRenderer;
import io.github.glaforge.antigravity.Agent;
import io.github.glaforge.antigravity.AgentConfig;
import io.github.glaforge.antigravity.AgentResponse;

import java.util.concurrent.TimeUnit;

import static io.github.glaforge.ansiren.Ansi.blue;
import static io.github.glaforge.ansiren.Ansi.bold;
import static io.github.glaforge.ansiren.Ansi.green;
import static io.github.glaforge.ansiren.Ansi.ready;
import static io.github.glaforge.ansiren.Ansi.red;

public class _01_HelloWorld {

    public static void main(String[] args) {
        System.out.println(ready()
                .bold().brightCyan()
                .append("\n=== Antigravity Java SDK - 01 Hello World ===\n")
                .reset());

        AgentConfig config = AgentConfig.builder()
                .instructions("You are a helpful and concise AI assistant.")
                .build();

        try (Agent agent = new Agent(config)) {
            String prompt = "Hello! Please introduce yourself in two short bullet points.";
            System.out.println(bold(blue("User: ")) + prompt);
            System.out.println(ready().faint().italic().append("Waiting for agent response...\n").reset());

            AgentResponse response = agent.chat(prompt).get(120, TimeUnit.SECONDS);

            System.out.println(bold(green("Agent:")));
            MarkdownRenderer markdownRenderer = new MarkdownRenderer();
            System.out.println(markdownRenderer.render(response.text()));
        } catch (Exception e) {
            System.err.println(red("Error running agent: " + e.getMessage()));
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

## Maven Dependencies

The playground uses version `0.2.13` of the SDK and version `0.1.0` of Ansiren:

```xml
<dependency>
    <groupId>io.github.glaforge.antigravity</groupId>
    <artifactId>antigravity-sdk-wrapper</artifactId>
    <version>0.2.13</version>
</dependency>

<dependency>
    <groupId>io.github.glaforge</groupId>
    <artifactId>ansiren</artifactId>
    <version>0.1.0</version>
</dependency>
```
