# Implementation notes

The task is very open-ended, and the assignment allows much room for the interpretation. A lot of work where therefore put into extracting the requirements, 
coming up with reasonable assumptions, making implementation choices etc. - these originally came as [tickets](../../issues/). This document compiles the notes for esasier reading.

## The client 

### Assumptions and discussion

- we assume a torrent-like thing, running in background
  - app activity and user interaction are mostly decoupled - user requests/shares files, not expecting immediate 
     arrival/delivery 
- the best would be to implement a "watchdog" service on the client side, which would
  - track the client session from start of the app to its termination, assigning unique `SessionID` to it
  - externally measure main processes CPU
  - capture internal events from the app by some local lightweight protocol (sockets, named pipes or sth like this)
  - format the data as json messages and send to the #7 over the net
  - not in scope of this project, but can also terminate/restart main process in case of problems  

> Which information does the client pass to the service?

We will focus on 1 core _metric_ - %CPU consumed by our app - this is measured periodically. Also we send _events_ , so that we can relate CPU value at particular moment to
application state and other parameters (see [Messages object](../../blob/master/src/main/scala/filesfu/collector/protocol/Messages.scala))

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

#### Protocol

We accept messages by http POST, formatted as [JSON lines](https://jsonlines.org/) - each line of the request body is a message. They are treated as a stream, fed directly to the database. There's no restriction on the request length, nor need to close it. It seems good idea for the client just to keep connection open and send data as it comes, in this case server would backpressure if it's overloaded and client could handle this e.g. by taking measures less often.

Message structures are defined in [Messages object](../../blob/master/src/main/scala/filesfu/collector/protocol/Messages.scala) 

Currently only `/sessions` endpoint is implemented accepting `Session` messages

#### Simulation

> Please feel free to omit most of the client implementation, and if you want client to send
> something, like client id, please feel free to assume it does, and place any constraints on it. You
> can write a simple client implementation or use any API tools available to simulate the requests.

We simulate the client in [Integration Tests](../tree/master/src/it). The [Simulation.scala](../../blob/master/src/it/scala/filesfu/Simulation.scala) uses Akka Http testkit together with ScalaCheck generators to feed random data with probabilities tuned for the case, illustrated by our example report (see below) 
