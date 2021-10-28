#!/usr/bin/env bash

# see https://docs.influxdata.com/influxdb/cloud/reference/cli/influx/write/

influx bucket delete -n FilesFromYou
influx bucket create -n FilesFromYou
influx write $@ --header "#constant measurement,cpu" \
                --header "#datatype tag,double,double,dateTime:RFC3339" \
                -b FilesFromYou -f data.csv
