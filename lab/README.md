# Authors
1. Ohad Ronen.
2. Gal Rabin

## Description
A command line tool that down load file in multithreaded manner.

## Directory manifest
| File | Purpose |
| --- | --- |
| IdcDm | Main module which execute the HTTP request threads. |
| DivideManager | Represent an object which divide the download to different chunks. |
| ChunkBytes | Represent an object which represent a division of DivideManager. |
| DownloadWorker | Thread implementation to HTTP rage request. |
| Helpers | Helper function which mainly parsing objects (to keep the code clean). |
| ConsoleColors | Simple object include colors code which been used in printing pretty to console. |

