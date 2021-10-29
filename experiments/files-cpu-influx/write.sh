#!/usr/bin/env bash

# see https://docs.influxdata.com/influxdb/cloud/reference/cli/influx/write/

bucket=FilesFU

influx bucket delete -n $bucket
influx bucket create -n $bucket
influx write $@ --header "#constant measurement,cpu" \
                --header "#datatype tag,double,double,dateTime:RFC3339" \
                -b $bucket -f data.csv
