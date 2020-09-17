# senseless

NOTE: This is currently in a state where it is probably not very useful for other people, but I'll try to improve on
      this. It is still a bit of a hacky project.

## What does this do?

I wanted to have a very simple way in order to push sensor values from a environmental monitoring station to a central
location, where these values could be retrieved as JSON in order to display them on a local web site. All values are
also scraped from this and pushed into a graphite database.

## Current state

Autumn 2020: This thing has been running on a local server for a bit over a year now, with, at times, several months of
uptime (meaning that I did not restart the server after kernel updates — shame on me!). 

## Features

* Flexible configuration
* Protecting end-points with simple tokens for writing and optionally reading
* Dumps data periodically and on shutdown
* Indicate if values are stale (not updated after exceeding lifetime)
* Format values, adding an offset, scaling them, etc.
* …

## Non-Goals

* Retaining historical data beyond some min / max / average values for the last 24 hours (think weather station)
* Generally doing too much

## Examples

Configuration (collections.json)
```json
[
  {
    "path": "/weather",
    "writeToken": "7d8e034d-6f90-42ad-a98b-152383f4eac9",
    "valueConfig": [
      {
        "path": "/nc2.5",
	"lifetime": 30

      },
      {
        "path": "/temperature",
        "format": "0.0",
        "scale": 0.01,
	"lifetime": 30

      },
      {
        "path": "/humidity",
        "format": "0.0",
        "scale": 0.001,
	"lifetime": 30

      },
      {
        "path": "/pressure",
        "format": "0.00",
        "scale": 0.001,
	"lifetime": 30

      },
      {
        "path": "/co2",
        "type": "INTEGER",
	"lifetime": 30

      }
    ]
  }
]
```

Which may return values such as the following when queried via `curl --silent http://localhost:7000/weather/`.
```json
{
  "/co2": {
    "timestamp": {
      "nano": 844653000,
      "epochSecond": 1600376332
    },
    "lifetime": 30,
    "rawValue": 428,
    "rawMax": 509,
    "rawMin": 346,
    "value": "428",
    "min": "346",
    "max": "509",
    "unformattedValue": 428,
    "stale": false,
    "age": 5
  },
  "/pressure": {
    "timestamp": {
      "nano": 282272000,
      "epochSecond": 1600375749
    },
    "lifetime": 30,
    "rawValue": 1008791,
    "rawMax": 1008981,
    "rawMin": 1007548,
    "value": "1008.79",
    "min": "1007.55",
    "max": "1008.98",
    "unformattedValue": 1008.791,
    "stale": true,
    "age": 588
  },
  "/temperature": {
    "timestamp": {
      "nano": 246501000,
      "epochSecond": 1600375749
    },
    "lifetime": 30,
    "rawValue": 2185,
    "rawMax": 2387,
    "rawMin": 2061,
    "value": "21.9",
    "min": "20.6",
    "max": "23.9",
    "unformattedValue": 21.85,
    "stale": true,
    "age": 588
  },
  "/humidity": {
    "timestamp": {
      "nano": 316782000,
      "epochSecond": 1600375749
    },
    "lifetime": 30,
    "rawValue": 59056,
    "rawMax": 79302,
    "rawMin": 57998,
    "value": "59.1",
    "min": "58.0",
    "max": "79.3",
    "unformattedValue": 59.056000000000004,
    "stale": true,
    "age": 588
  },
  "/nc2.5": {
    "timestamp": {
      "nano": 234946000,
      "epochSecond": 1600376330
    },
    "lifetime": 30,
    "rawValue": 50.23,
    "rawMax": 93.61,
    "rawMin": 37.51,
    "value": "50.23",
    "min": "37.51",
    "max": "93.61",
    "unformattedValue": 50.23,
    "stale": false,
    "age": 7
  }
}
```

Updating a value via curl may look like this `curl -d "1234" -X POST http://localhost:7000/weather/pressure?token=7d8e034d-6f90-42ad-a98b-152383f4eac9`.