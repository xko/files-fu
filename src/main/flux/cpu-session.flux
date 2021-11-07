src      = from(bucket: "FilesFU") |> range(start: -3d)
cpu      = src |> filter(fn: (r) => r._field == "cpu" )

versions = src |> filter(fn: (r) => exists r.version ) 
               |> keep(columns: ["version","sessionID"])

richCpu = join( tables: {v:versions,cpu:cpu}, on: ["sessionID"])

richCpu 
    |> group(columns: ["sessionID","version"])
    |> window(every: 5s) |> mean()
    |> group(columns: ["sessionID","version"]) |> max()