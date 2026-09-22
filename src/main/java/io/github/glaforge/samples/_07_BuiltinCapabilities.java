package io.github.glaforge.samples;

import io.github.glaforge.ansiren.MarkdownRenderer;
import io.github.glaforge.antigravity.Agent;
import io.github.glaforge.antigravity.AgentConfig;
import io.github.glaforge.antigravity.AgentResponse;
import io.github.glaforge.antigravity.CapabilitiesConfig;

import java.util.concurrent.TimeUnit;

import static io.github.glaforge.ansiren.Ansi.blue;
import static io.github.glaforge.ansiren.Ansi.bold;
import static io.github.glaforge.ansiren.Ansi.green;
import static io.github.glaforge.ansiren.Ansi.ready;
import static io.github.glaforge.ansiren.Ansi.red;

/**
 * Sample 07: Demonstrates native built-in capabilities provided by the Go harness.
 * Enables zero-boilerplate tools such as workspace file inspection (listDir, viewFile)
 * and Google web search directly via CapabilitiesConfig.
 */
public class _07_BuiltinCapabilities {

    public static void main(String[] args) {
        System.out.println(ready()
                .bold().brightCyan()
                .append("\n=== Antigravity Java SDK - 07 Built-in Capabilities ===\n")
                .reset());

        // Configure native harness capabilities
        CapabilitiesConfig capabilities = CapabilitiesConfig.builder()
                .enableListDir(true)
                .enableViewFile(true)
                .enableWebSearch(true)
                .build();

        AgentConfig config = AgentConfig.builder()
                .instructions("You are a helpful assistant with native capabilities. "
                        + "Use list_dir and view_file to inspect the project workspace when asked.")
                .capabilities(capabilities)
                .build();

        try (Agent agent = new Agent(config)) {
            String prompt = "Please inspect the current workspace directory and summarize what files exist and what this project is.";
            System.out.println(bold(blue("User: ")) + prompt);
            System.out.println(ready().faint().italic().append("Waiting for agent to inspect workspace...\n").reset());

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
