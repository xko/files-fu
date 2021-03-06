CPU metrcis collecition and analysis demo
-----------------------------

> _Future of the [Files-Fu](../.. "assignment mentions both 'FilesForYou' and 'FilesFromYou', I came up with this variant instead - IMO cooler and punchier. BTW I'd like to retain the rights for the name, maybe one day I'll use it for a startup :)") - a revolutionary file sharing service - is in danger, 
> as their client app seems to consume too much CPU, 
> making the service unusable..._

This is a proof-of-concept demo, addressing some aspects of the [home assignment](Hive_Streaming_Backend_Home_Assignment.pdf) for the interview with [Hive Streaming](https://www.hivestreaming.com/)


### Implementation overview 

See [Implementation Notes](Notes.md) for techical discussion, motivations etc. This README focuses further on now to build and run the demo.  

### The demo

#### 0. Prerequisites

- JDK and SBT available on `$PATH`to build and run the service
- Running [InfluxDB](https://www.influxdata.com/get-influxdb/) instance to store the data and render the reports. 
  This was tested with free tier of the [Cloud service](https://www.influxdata.com/products/influxdb-cloud/), but local install should work as well   
  - an API token with full access rights (unsafe, but we assume you have separate organization in Influx for this demo, or at least don't have anything important in there)
- [InfluxDB CLI](https://github.com/influxdata/influx-cli)  available on `$PATH`to [initialize DB](bin/reset-data) and run the [query examples](src/main/flux)

#### 1. Configuration

- create `src/main/resources/influx-auth.properties` with 
  ```
  user="<your organization>"
  password="<your API token>"
  ```
  this will be used by scala code in integration tests and simulation 
- run `bin/init-config` to initialize your `~/.influxdbv2/configs` for the CLI

#### 2. Building and running

- Initialize the bucket in InfluxDB with `bin/reset-data`
- Use the following SBT commands:
  - `test` : to build and unit test as usual
  - `it:testOnly filesfu.Acceptance` : small acceptance test to make sure data really ends up in influx
  - `it:testOnly filesfu.Simulation` : a simulation, feeding random data to illustrate particular scenario. After this is done, reports in the next section should show something sensible
     - the [source](src/it/scala/filesfu/Simulation.scala) can be modified to alter the scenario
  - `run` : to simply run the server to play with it using `curl` or similar tool
    - it currently only supports `POST` at the path `/sessions` and accepts JSON messages - one per line. The message structure is defined
      in [Messages.scala](src/main/scala/filesfu/collector/protocol/Messages.scala)
- To empty the database and start over, use `bin/reset-data` any time


#### 3. Report example

To illustrate how the report could look like we created some histograms for the InfluxDB web console. To recreate it, navigate to Dashboards->Create dashboard->Import dashboard. Import [example-dashboard.json](src/main/flux/example-dashboard.json). With the data from the simulation above it would look like:

![image](https://user-images.githubusercontent.com/8439412/140663667-8b7b20ff-8b7a-4ba4-9f7a-0518aa75df9d.png)
