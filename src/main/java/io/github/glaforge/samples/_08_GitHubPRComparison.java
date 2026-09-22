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
import io.github.glaforge.antigravity.Policies;
import io.github.glaforge.antigravity.RunCommandConfig;
import io.github.glaforge.antigravity.hooks.HookResult;
import io.github.glaforge.antigravity.tools.Param;
import io.github.glaforge.antigravity.tools.Tool;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.glaforge.ansiren.Ansi.blue;
import static io.github.glaforge.ansiren.Ansi.bold;
import static io.github.glaforge.ansiren.Ansi.cyan;
import static io.github.glaforge.ansiren.Ansi.green;
import static io.github.glaforge.ansiren.Ansi.ready;
import static io.github.glaforge.ansiren.Ansi.red;

/**
 * Sample 08: GitHub Pull Request Comparison Agent.
 * Demonstrates:
 * 1. Loading custom Agent Skills (github-pr-review) for code review rubrics.
 * 2. Enabling subagent orchestration and built-in capabilities (URL reading, shell commands).
 * 3. Providing custom GitHub tools for retrieving pull request diffs and REST API metadata.
 * 4. Applying security policies to permit network and execution tools.
 * 5. Streaming live thought reasoning and tool calls before rendering a final Markdown synthesis.
 */
public class _08_GitHubPRComparison {

    /**
     * Custom tool providing reliable retrieval of GitHub pull request diffs and metadata.
     */
    public static class GitHubTools {
        private static final Pattern PR_URL_PATTERN =
                Pattern.compile("https?://github\\.com/([^/]+)/([^/]+)/pull/([0-9]+).*");

        private final HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();

        @Tool(name = "fetch_github_diff", description = "Fetches the raw code diff for a GitHub pull request URL (e.g., https://github.com/langchain4j/langchain4j/pull/6457)")
        public String fetchPullRequestDiff(@Param(name = "prUrl", description = "The GitHub PR URL or diff URL") String prUrl) {
            String apiUrl;
            Matcher matcher = PR_URL_PATTERN.matcher(prUrl);
            if (matcher.matches()) {
                String owner = matcher.group(1);
                String repo = matcher.group(2);
                String prNumber = matcher.group(3);
                apiUrl = String.format("https://api.github.com/repos/%s/%s/pulls/%s", owner, repo, prNumber);
            } else if (prUrl.startsWith("https://api.github.com/")) {
                apiUrl = prUrl;
            } else {
                apiUrl = prUrl.endsWith(".diff") ? prUrl : prUrl + ".diff";
            }

            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .header("User-Agent", "Antigravity-Java-SDK")
                        .header("Accept", "application/vnd.github.v3.diff")
                        .timeout(Duration.ofSeconds(30))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return response.body();
                } else {
                    return "Error: HTTP " + response.statusCode() + " fetching diff from " + apiUrl;
                }
            } catch (Exception e) {
                return "Failed to fetch diff: " + e.getMessage();
            }
        }

        @Tool(name = "fetch_github_pr_metadata", description = "Fetches pull request details (title, state, author, additions, deletions) from GitHub REST API")
        public String fetchPullRequestMetadata(
                @Param(name = "owner", description = "Repository owner (e.g., langchain4j)") String owner,
                @Param(name = "repo", description = "Repository name (e.g., langchain4j)") String repo,
                @Param(name = "prNumber", description = "Pull request number (e.g., 6457)") int prNumber) {
            String apiUrl = String.format("https://api.github.com/repos/%s/%s/pulls/%d", owner, repo, prNumber);
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .header("User-Agent", "Antigravity-Java-SDK")
                        .header("Accept", "application/vnd.github+json")
                        .timeout(Duration.ofSeconds(20))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return response.body();
                } else {
                    return "Error: HTTP " + response.statusCode() + " fetching PR metadata from " + apiUrl;
                }
            } catch (Exception e) {
                return "Failed to fetch PR metadata: " + e.getMessage();
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(ready()
                .bold().brightCyan()
                .append("\n=== Antigravity Java SDK - 08 GitHub PR Comparison ===\n")
                .reset());

        String skillPath = SkillResolver.resolveSkillPath("skills/github-pr-review");
        System.out.println(ready().faint().append("• Registering Agent Skill: " + skillPath).reset());
        System.out.println(ready().faint().append("• Enabling subagents, URL reading, shell access, and GitHub tools").reset());

        // Configure full capabilities including subagent orchestration and unconfined command execution
        CapabilitiesConfig capabilities = CapabilitiesConfig.builder()
                .enableSubagents(true)
                .enableUrlReading(true)
                .enableWebSearch(true)
                .enableShell(true)
                .runCommandConfig(RunCommandConfig.builder().enableSandbox(false).build())
                .enableViewFile(true)
                .enableWriteFile(true)
                .enableFileEdit(true)
                .enableListDir(true)
                .enableGrepSearch(true)
                .build();

        // Configure agent with skill, tools, security policies, and lifecycle hooks
        AgentConfig config = AgentConfig.builder()
                .instructions("""
                        You are a principal software engineer and open-source project maintainer reviewing GitHub Pull Requests.
                        Always follow the guidelines and comparative rubrics in your installed 'github-pr-review' skill.
                        Use your GitHub tools to examine the PR diffs and metadata.
                        Once you have inspected the diffs and details of both PRs, proceed directly to synthesize your comparative report.
                        Provide an objective, structured, and in-depth comparison and practical recommendation.
                        """)
                .addSkillPath(skillPath)
                .addTool(new GitHubTools())
                .capabilities(capabilities)
                .addPolicy(Policies.allowAll())
                .addPreToolCallDecideHook((toolCall, ctx) -> {
                    System.out.println(ready().faint().brightYellow()
                            .append("  ⚙ [Tool Call] " + toolCall.name() + "(" + toolCall.args() + ")\n")
                            .reset());
                    return CompletableFuture.completedFuture(HookResult.allowed());
                })
                .build();

        try (Agent agent = new Agent(config)) {
            String prompt = """
                    Analyze and compare two competing Pull Requests submitted to langchain4j/langchain4j:
                    - PR #6457: https://github.com/langchain4j/langchain4j/pull/6457
                    - PR #6462: https://github.com/langchain4j/langchain4j/pull/6462

                    Both PRs address issue #6456: surfacing generated images from Google GenAI chat responses.

                    Please perform the review:
                    1. Fetch the code diffs and PR metadata using your fetch_github_diff and fetch_github_pr_metadata tools.
                    2. Use the 'github-pr-review' skill to structure your evaluation.
                    3. Compare the architectural designs, streaming image chunk handling, defensive checks (MIME types, null safety), and test quality.
                    4. Deliver a side-by-side comparison table, discuss trade-offs and synergies, and provide your final recommendation on which PR to favor or how they should complement each other.
                    """;

            System.out.println("\n" + bold(blue("User Request:")));
            System.out.println(prompt.trim() + "\n");
            System.out.println(ready().faint().italic().append("Starting agent analysis (streaming thoughts & tool calls)...\n").reset());

            AtomicBoolean firstThought = new AtomicBoolean(true);
            AtomicBoolean firstText = new AtomicBoolean(true);
            StringBuilder textBuffer = new StringBuilder();

            CompletableFuture<AgentResponse> future = agent.chatStream(prompt, chunk -> {
                if (!chunk.thoughtsDelta().isEmpty()) {
                    if (firstThought.compareAndSet(true, false)) {
                        System.out.print(ready().faint().italic().cyan().append("\n[Thinking] ").reset());
                    }
                    System.out.print(ready().faint().italic().append(chunk.thoughtsDelta()).reset());
                }

                if (!chunk.textDelta().isEmpty()) {
                    if (firstText.compareAndSet(true, false)) {
                        if (!firstThought.get()) {
                            System.out.println("\n");
                        }
                        System.out.println(bold(green("\nAgent Comparison Report:")));
                    }
                    textBuffer.append(chunk.textDelta());
                }
            });

            AgentResponse response = future.get(600, TimeUnit.SECONDS);

            System.out.println("\n" + bold(green("=== Final Rendered Report ===")));
            MarkdownRenderer markdownRenderer = new MarkdownRenderer();
            System.out.println(markdownRenderer.render(response.text()));

            System.out.println(ready().faint().append("\nAnalysis completed successfully (Tokens used: "
                    + (response.usageMetadata() != null ? response.usageMetadata().totalTokenCount() : "N/A") + ").").reset());
        } catch (Exception e) {
            System.err.println(red("Error during GitHub PR comparison: " + e.getMessage()));
            e.printStackTrace();
        }
    }
}
