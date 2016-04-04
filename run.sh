#!/usr/bin/env bash
mvn package -DskipTests; java -jar target/artist-info-1.0-SNAPSHOT-fat.jar
