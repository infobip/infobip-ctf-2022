# Solution

A PIE binary that stores the flag address to `[rsp+16]` so you can easily `pop rsi` three times and craft a syscall to write it to stdout.

Solution is in `golfrev-solution.py`.
