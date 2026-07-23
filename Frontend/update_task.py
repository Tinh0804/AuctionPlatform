import os

filepath = '/Users/macbookpro/.gemini/antigravity-ide/brain/c0428178-f004-4242-9c9f-c88b10db9857/task.md'
with open(filepath, 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if line.startswith('- `[ ]`'):
        lines[i] = line.replace('- `[ ]`', '- `[x]`')

with open(filepath, 'w') as f:
    f.writelines(lines)
