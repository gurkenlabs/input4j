Branch Scope: gh-pages
This AGENTS.md applies only to the gh-pages branch. It contains guidance for the Jekyll-based GitHub Pages site. If you switch branches, this guidance may not apply.

Overview
- This branch hosts a static site built with Jekyll.
- Local development requires Ruby (2.7+) and Bundler.
- The site is served at /input4j (baseurl in _config.yml).

Local Setup
Note: Commands below must run in WSL since Ruby is not installed on Windows.
1. Verify Ruby: ruby -v
2. Install Bundler: gem install bundler
3. Install dependencies: bundle install

Build and Run
- Build site: bundle exec jekyll build
- Serve locally: bundle exec jekyll serve --host 0.0.0.0 --port 4000
- Preview: http://localhost:4000/input4j/
- Production build: JEKYLL_ENV=production bundle exec jekyll build
- Windows: set JEKYLL_ENV=production && bundle exec jekyll build

Validation
- Markdown: run markdownlint on "**/*.md"
- Ensure YAML front matter is valid in all pages
- Check _config.yml for syntax errors

CI
- CI should install Ruby, run bundle install, then bundle exec jekyll build
- Verify the built _site directory is generated without errors

Content Guidelines
- All pages must include valid YAML front matter (layout, title)
- Use relative links with {{ relative_url }} for internal navigation
- Keep Markdown files linted and free of broken links

Cursor/Copilot Rules
- None detected for this repo. Place rules in .cursor/rules/ or .github/copilot-instructions.md if added later.

Quick Start
- Run bundle exec jekyll serve to preview changes locally
- Verify links work with the baseurl prefix
- Ensure build passes before pushing
