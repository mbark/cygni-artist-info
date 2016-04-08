# cygny-artist-info
A simple REST-based API to get information about artists using their MusicBrainz identifier (mbid).

## How to run
```
# Clone the repo
git clone https://github.com/mbark/cygny-artist-info

# Open the directory
cd cygni-artist-info

# And run
./run.sh
```
Then you can send your GET requests to localhost:8080/:mbid

As an example try:
- localhost:8080?mbid=5b11f4ce-a62d-471e-81fc-a69a8278c7da

## Testing
```
mvn clean test
```
Currently there are very few tests, support for these need to be added
through some form of dependency injection.
