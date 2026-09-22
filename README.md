# Antigravity Java SDK Playground

A playground project demonstrating how to use the [unofficial Antigravity SDK for Java](https://github.com/glaforge/antigravity-java-sdk).

This SDK allows you to build, configure, host, and execute AI agents in Java, bridging the gap for enterprise Java developers who want to harness the power of Antigravity with Gemini models.

The SDK features an on-demand native harness downloader: on first run, it automatically fetches and caches the appropriate native Go harness binary (`localharness`) for your platform into `~/.antigravity/bin/<slice>/`. Subsequent runs reuse the cached binary.

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

### 2. Run the Samples

Run Sample 01 (Hello World):
```bash
./mvnw exec:java -Dexec.mainClass="io.github.glaforge.samples._01_HelloWorld"
```

Run Sample 02 (Local Weather Tool):
```bash
./mvnw exec:java -Dexec.mainClass="io.github.glaforge.samples._02_WeatherTool"
```

Run Sample 03 (Streaming Responses):
```bash
./mvnw exec:java -Dexec.mainClass="io.github.glaforge.samples._03_Streaming"
```

Run Sample 04 (Security Policies & Guardrails):
```bash
./mvnw exec:java -Dexec.mainClass="io.github.glaforge.samples._04_SecurityPolicies"
```

Run Sample 05 (Agent Skills):
```bash
./mvnw exec:java -Dexec.mainClass="io.github.glaforge.samples._05_AgentSkills"
```

Run Sample 06 (Multi-Turn Chat & ToolContext):
```bash
./mvnw exec:java -Dexec.mainClass="io.github.glaforge.samples._06_MultiTurnChat"
```

Run Sample 07 (Built-in Capabilities):
```bash
./mvnw exec:java -Dexec.mainClass="io.github.glaforge.samples._07_BuiltinCapabilities"
```

Run Sample 08 (GitHub PR Comparison):
```bash
./mvnw exec:java -Dexec.mainClass="io.github.glaforge.samples._08_GitHubPRComparison"
```

---

## Available Samples

1. **[`_01_HelloWorld`](src/main/java/io/github/glaforge/samples/_01_HelloWorld.java)**: Basic agent configuration and execution turn, formatted with ANSI styling.
2. **[`_02_WeatherTool`](src/main/java/io/github/glaforge/samples/_02_WeatherTool.java)**: Registering local Java tools via `@Tool` and `@Param`, and generating strongly-typed Structured Output conforming to a Java record (`WeatherAdvisory`) via `finishToolSchema(...)`.
3. **[`_03_Streaming`](src/main/java/io/github/glaforge/samples/_03_Streaming.java)**: Real-time token-by-token streaming using `chatStream()`, separating model thinking from text deltas.
4. **[`_04_SecurityPolicies`](src/main/java/io/github/glaforge/samples/_04_SecurityPolicies.java)**: Deny-by-default security policies with `Policies.denyIf(...)`, `allowTool(...)`, and `denyAll()`, blocking dangerous tools before execution.
5. **[`_05_AgentSkills`](src/main/java/io/github/glaforge/samples/_05_AgentSkills.java)**: Loading file-based Agent Skills (`.addSkillPath(...)`) conforming to the open Agent Skills specification.
6. **[`_06_MultiTurnChat`](src/main/java/io/github/glaforge/samples/_06_MultiTurnChat.java)**: Multi-turn conversational memory and `ToolContext` parameter injection for managing session-scoped state.
7. **[`_07_BuiltinCapabilities`](src/main/java/io/github/glaforge/samples/_07_BuiltinCapabilities.java)**: Enabling native Go harness capabilities (`CapabilitiesConfig`) such as workspace file inspection and web search.
8. **[`_08_GitHubPRComparison`](src/main/java/io/github/glaforge/samples/_08_GitHubPRComparison.java)**: Comparing two competing GitHub Pull Requests using domain-specific Agent Skills (`skills/github-pr-review`), subagents, custom GitHub tools, and security policies.

---

## Sample Code

### 01: Hello World Agent (`_01_HelloWorld.java`)

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

### 02: Local Weather Tool & Structured Output (`_02_WeatherTool.java`)

```java
public class _02_WeatherTool {

    // Tool return data
    public record WeatherReport(String city, String condition, int temperatureCelsius, int humidityPercent) {}

    // Agent structured output schema
    public record WeatherAdvisory(
            String city,
            int temperatureCelsius,
            String condition,
            String clothingRecommendation,
            boolean umbrellaNeeded,
            List<String> suggestedActivities
    ) {}

    public static class WeatherTools {
        @Tool(name = "get_weather", description = "Get current weather conditions and temperature for a given city.")
        public WeatherReport getWeather(
                @Param(name = "city", description = "The name of the city, e.g. Paris, Tokyo, London") String city
        ) {
            System.out.println(bold(yellow("⚡ [Local Tool Invoked] ")) + yellow("get_weather(city=\"" + city + "\")"));
            return new WeatherReport("Paris", "Sunny with mild breeze", 22, 55);
        }
    }

    public static void main(String[] args) {
        AgentConfig config = AgentConfig.builder()
                .instructions("You are a helpful weather assistant. Always use get_weather before advising.")
                .addTool(new WeatherTools())
                .finishToolSchema(WeatherAdvisory.class) // <-- Derives JSON Schema from Java record
                .build();

        try (Agent agent = new Agent(config)) {
            AgentResponse response = agent.chat("What is the current weather in Paris? Give me clothing advice and things to do.")
                    .get(120, TimeUnit.SECONDS);

            // Deserialize directly into strongly-typed Java record
            WeatherAdvisory advisory = response.getStructuredOutput(WeatherAdvisory.class);

            System.out.println("City: " + advisory.city());
            System.out.println("Temp: " + advisory.temperatureCelsius() + "°C");
            System.out.println("Clothing: " + advisory.clothingRecommendation());
            System.out.println("Umbrella needed? " + (advisory.umbrellaNeeded() ? "Yes ☂️" : "No ☀️"));
            System.out.println("Activities: " + advisory.suggestedActivities());
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

The playground uses version `0.2.15` of the SDK and version `0.1.1` of Ansiren:

```xml
<dependency>
    <groupId>io.github.glaforge.antigravity</groupId>
    <artifactId>antigravity-sdk-wrapper</artifactId>
    <version>0.2.15</version>
</dependency>

<dependency>
    <groupId>io.github.glaforge</groupId>
    <artifactId>ansiren</artifactId>
    <version>0.1.1</version>
</dependency>
```
