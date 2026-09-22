/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.glaforge.samples;

import io.github.glaforge.ansiren.MarkdownRenderer;
import io.github.glaforge.antigravity.Agent;
import io.github.glaforge.antigravity.AgentConfig;
import io.github.glaforge.antigravity.AgentResponse;
import io.github.glaforge.antigravity.CapabilitiesConfig;

import java.nio.file.Path;
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

        Path projectDir = SkillResolver.getProjectDir();
        System.out.println(ready().faint().append("• Target project directory: " + projectDir + "\n").reset());

        // Configure native harness capabilities
        CapabilitiesConfig capabilities = CapabilitiesConfig.builder()
                .enableListDir(true)
                .enableViewFile(true)
                .enableWebSearch(true)
                .build();

        AgentConfig config = AgentConfig.builder()
                .instructions("""
                        You are a helpful assistant with native capabilities.
                        The active project workspace directory is: """ + projectDir + """
                        Use list_dir and view_file to inspect the project directory when asked.
                        """)
                .capabilities(capabilities)
                .build();

        try (Agent agent = new Agent(config)) {
            String prompt = "Please inspect the project directory at " + projectDir + " and summarize what files exist and what this project is.";
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
