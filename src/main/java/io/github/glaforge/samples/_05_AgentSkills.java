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
 * Sample 05: Demonstrates how to load open-specification Agent Skills.
 * The agent dynamically activates the bundled 'antigravity-sdk-java' skill
 * to answer domain-specific questions about configuring Java agents.
 */
public class _05_AgentSkills {

    public static void main(String[] args) {
        System.out.println(ready()
                .bold().brightCyan()
                .append("\n=== Antigravity Java SDK - 05 Agent Skills ===\n")
                .reset());

        String skillPath = SkillResolver.resolveSkillPath("skills/antigravity-sdk-java");
        System.out.println(ready().faint().append("Registering Agent Skill path: " + skillPath + "\n").reset());

        // Configure agent with file-based skill path and file viewing capability so it can read SKILL.md
        CapabilitiesConfig capabilities = CapabilitiesConfig.builder()
                .enableViewFile(true)
                .build();

        AgentConfig config = AgentConfig.builder()
                .instructions("""
                        You are a helpful expert software engineer specializing in the Antigravity Java SDK.
                        Consult your installed agent skills to answer technical questions accurately.
                        """)
                .addSkillPath(skillPath)
                .capabilities(capabilities)
                .build();

        try (Agent agent = new Agent(config)) {
            String prompt = "How do I configure security policies in the Antigravity Java SDK?";

            System.out.println(bold(blue("User: ")) + prompt);
            System.out.println(ready().faint().italic().append("Waiting for agent to consult skill...\n").reset());

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
