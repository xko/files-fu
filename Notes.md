# Implementation notes

The task is very open-ended, and the assignment allows much room for the interpretation. A lot of work where therefore put into extracting the requirements, 
coming up with reasonable assumptions, making implementation choices etc. - these originally came as [tickets](../../issues/). This document compiles the notes for esasier reading.

## Features 

These are high level aspects the functionality or the project contents. Essentially, this is how the task was understood and what is assumed about it.

### The client 

#### Assumptions and discussion

- we assume a torrent-like thing, running in background
  - app activity and user interaction are mostly decoupled - user requests/shares files, not expecting immediate 
     arrival/delivery 
- the best would be to implement a "watchdog" service on the client side, which would
  - track the client session from start of the app to its termination, assigning unique `SessionID` to it
  - externally measure main processes CPU
  - capture internal events from the app by some local lightweight protocol (sockets, named pipes or sth like this)
  - format the data as json messages and send to the #7 over the net
  - not in scope of this project, but can also terminate/restart main process in case of problems  

> Please feel free to omit most of the client implementation, and if you want client to send
> something, like client id, please feel free to assume it does, and place any constraints on it. You
> can write a simple client implementation or use any API tools available to simulate the requests.

We simulate the client in [Integration Tests](../tree/master/src/it)  (#22).  

> Details like authentication or an actual way to measure CPU consumption can be mentioned but
> please don't focus on that part of the solution.
> 
> Which information does the client pass to the service?

see #21 

> How often does the client send data?

Looks reasonable to measure CPU  as often as possible, but without impacting client system - needs research. 
1-2kb/s is reasonable traffic to consume - enough to send 10 x100-byte messages per second. 
Current impl (#21)  streaming protocol allowing request of any length, without need to terminate. It seems good idea for the client just to keep connection open and send data as it comes, in this case server would backpressure if it's overloaded and client could handle this e.g. by taking measures less often.
  
> What happens if the backend is unavailable?
> What happens if the client fails to send data because it consumed all the CPU?

Client (or watchdog) would also write logs to disk and keep them for some sensible time. When connection/normal operation is restored, 
logs are sent to collector which parses them and reconstructs the metrics, events and their timestamps. 
This is likely to be several MB, so we ask the user explicitly before we do this. On the collector side separate endpoint is needed for that - not implemented here. 


### Implementation
- [x] #21
- [x] #22
