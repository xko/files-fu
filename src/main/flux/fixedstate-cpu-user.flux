selectedVersion = "700"
selectedState = "login"
src     = from(bucket: "FilesFU") |> range(start: v.timeRangeStart, stop: v.timeRangeStop)

cpu     = src |> filter(fn: (r) => r._field == "cpu" )


versions = src |> filter(fn: (r) => r.version == selectedVersion)
               |> keep(columns: ["version","sessionID"])
states   = src |> filter(fn: (r) => exists r.sessionState )
               |> keep(columns: ["sessionState","sessionID","userID","_time"])

richCpu = join( tables: {v:versions,cpu:cpu}, on: ["sessionID"])

richerCpu  = join(tables: {"cpu":richCpu, "state":states}, on: ["sessionID"])
               |> filter(fn: (r) => r["_time_state"] <= r["_time_cpu"] )
               |> group(columns: ["_value","_time_cpu"]) |> max(column: "_time_state")
               |> rename(columns: {"_time_cpu":"_time"})

richerCpu |> filter(fn: (r) => r.sessionState == selectedState)
