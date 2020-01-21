# Authors
1. Ohad Ronen.
2. Gal Rabin

## Description
A command line tool that down load file in multithreaded manner.

## Compile
1. `javac -d build -source 11 lab/*.java `
2. `cd build`
3. `java lab.DownloadManager http://archive.org/download/Mario1_500/Mario1_500.avi 15`

## Usage
`<java compiled file> <url> <threads>`
`<java compiled file> <url mirrors file> <threads>`

## Example
`<java compiled file> https://archive.org/download/Mario1_500/Mario1_500.avi 15`