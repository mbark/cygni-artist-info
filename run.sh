#!/usr/bin/env bash
mvn package -DskipTests; java -Djava.util.logging.config.file=logging.properties -jar target/artist-info-1.0-SNAPSHOT-fat.jar
