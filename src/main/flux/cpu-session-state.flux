selectedVersion = "700"

src     = from(bucket: "FilesFU") |> range(start: -3d)
cpu     = src |> filter(fn: (r) => r._field == "cpu" )


versions = src |> filter(fn: (r) => r.version == selectedVersion) 
               |> keep(columns: ["version","sessionID"])
states   = src |> filter(fn: (r) => exists r.sessionState )
               |> keep(columns: ["sessionState","sessionID","_time"])    

richCpu = join( tables: {v:versions,cpu:cpu}, on: ["sessionID"])

richerCpu  = join(tables: {"cpu":richCpu, "state":states}, on: ["sessionID"]) 
               |> filter(fn: (r) => r["_time_state"] < r["_time_cpu"] )
               |> group(columns: ["_value","_time_cpu"]) |> max(column: "_time_state")
               |> rename(columns: {"_time_cpu":"_time"}) 


richerCpu 
    |> group(columns: ["sessionID", "sessionState","version"])
    |> window(every: 5s) |> mean()
    |> group(columns: ["sessionID", "sessionState", "version"]) |> max()