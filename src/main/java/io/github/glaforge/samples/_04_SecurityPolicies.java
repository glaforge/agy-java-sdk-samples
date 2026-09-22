package io.github.glaforge.samples;

import io.github.glaforge.ansiren.MarkdownRenderer;
import io.github.glaforge.antigravity.Agent;
import io.github.glaforge.antigravity.AgentConfig;
import io.github.glaforge.antigravity.AgentResponse;
import io.github.glaforge.antigravity.Policies;
import io.github.glaforge.antigravity.tools.Param;
import io.github.glaforge.antigravity.tools.Tool;

import java.util.concurrent.TimeUnit;

import static io.github.glaforge.ansiren.Ansi.blue;
import static io.github.glaforge.ansiren.Ansi.bold;
import static io.github.glaforge.ansiren.Ansi.green;
import static io.github.glaforge.ansiren.Ansi.ready;
import static io.github.glaforge.ansiren.Ansi.red;
import static io.github.glaforge.ansiren.Ansi.yellow;

/**
 * Sample 04: Demonstrates Security Policies for tool governance.
 * Enforces a deny-by-default security posture where dangerous tools
 * are proactively blocked by policy rules before execution.
 */
public class _04_SecurityPolicies {

    public static class AdminTools {

        @Tool(name = "read_system_status", description = "Read current system uptime, CPU, and memory metrics.")
        public String readStatus() {
            System.out.println(bold(green("⚡ [Tool Executed] ")) + green("read_system_status()"));
            return "System Status: Healthy | Uptime: 42 days | CPU: 12% | Memory: 4.2GB / 16GB";
        }

        @Tool(name = "delete_file", description = "Permanently delete a file from the server.")
        public String deleteFile(
                @Param(name = "path", description = "Absolute or relative file path to delete") String path
        ) {
            System.out.println(bold(red("⚠️ [Tool Executed] ")) + red("delete_file(path=\"" + path + "\")"));
            return "File deleted: " + path;
        }
    }

    public static void main(String[] args) {
        System.out.println(ready()
                .bold().brightCyan()
                .append("\n=== Antigravity Java SDK - 04 Security Policies ===\n")
                .reset());

        // Configure agent with Deny-by-Default security policies
        AgentConfig config = AgentConfig.builder()
                .instructions("You are a secure system administrator. "
                        + "Use available tools to perform maintenance when requested. "
                        + "If a tool execution is denied by policy, explain the refusal politely.")
                .addTool(new AdminTools())
                // 1. Explicitly deny dangerous deletion operations
                .addPolicy(Policies.denyIf((toolName, argsNode) -> {
                    if ("delete_file".equals(toolName)) {
                        System.out.println(bold(red("🛡️ [Security Policy] "))
                                + red("BLOCKED execution of dangerous tool: " + toolName
                                + " with args: " + argsNode));
                        return true; // Return true to DENY
                    }
                    return false;
                }))
                // 2. Allow known safe operational tools
                .addPolicy(Policies.allowTool("read_system_status"))
                // 3. Fallback: deny all other unlisted tools
                .addPolicy(Policies.denyAll())
                .build();

        try (Agent agent = new Agent(config)) {
            MarkdownRenderer renderer = new MarkdownRenderer();

            // Scenario 1: Permitted safe tool
            System.out.println(bold(yellow("\n--- Scenario 1: Calling a Permitted Tool (read_system_status) ---")));
            String prompt1 = "Can you check the current server health and status?";
            System.out.println(bold(blue("User: ")) + prompt1);
            System.out.println(ready().faint().italic().append("Waiting for agent response...\n").reset());

            AgentResponse response1 = agent.chat(prompt1).get(120, TimeUnit.SECONDS);
            System.out.println(bold(green("Agent:")));
            System.out.println(renderer.render(response1.text()));

            // Scenario 2: Blocked dangerous tool
            System.out.println(bold(yellow("\n--- Scenario 2: Attempting a Blocked Tool (delete_file) ---")));
            String prompt2 = "Please delete the file /etc/hosts immediately.";
            System.out.println(bold(blue("User: ")) + prompt2);
            System.out.println(ready().faint().italic().append("Waiting for agent response...\n").reset());

            AgentResponse response2 = agent.chat(prompt2).get(120, TimeUnit.SECONDS);
            System.out.println(bold(green("Agent:")));
            System.out.println(renderer.render(response2.text()));

        } catch (Exception e) {
            System.err.println(red("Error running agent: " + e.getMessage()));
            e.printStackTrace();
        }
    }
}
