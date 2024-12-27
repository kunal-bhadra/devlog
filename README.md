# Devlog

This is a CLI application to get the summary of a GitHub user's recent public events.

## Features
- [x] Fetches all public events of a GitHub user
- [x] Filters and stores the following in memory for each repo:
  - [x] Starred status
  - [x] Commit count and messages
  - [x] Pull Request Open count
  - [x] Pull Request Comment count and messages
  - [x] New Members added count
  - [x] Open Issues count
  - [x] Issue Comment count and messages
- [x] LLM Parsing of GitHub Summary with the persona of
  - [x] Senior Software Architect
  - [x] HR Recruiter
  - [x] Curious Partner
- [ ] Deploy

## Demo
```bash
# Run the program
mvn clean compile exec:java -Dexec.args="kunal-bhadra"
```
```text
# Output
- Pushed 20 commits to kunal-bhadra/cli-task-tracker
- Starred 1 repo cheahjs/free-llm-api-resources
- Pushed 1 commit to kunal-bhadra/task-tracker-cli
- Starred 1 repo linsomniac/spotify_to_ytmusic
```

## Build Environment
Java: `openjdk 21.0.5 2024-10-15`\
Maven: `Apache Maven 3.6.3`

---

This projects extends the functionality of a [coding challenge](https://roadmap.sh/projects/github-user-activity) to hopefully make it more useful and fun for everybody else.
