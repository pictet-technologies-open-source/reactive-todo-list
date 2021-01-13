#!/bin/bash
if [ "$#" -eq 1 ]; then
if [ $1 = "-mongo-only" ]; then 
      docker-compose -f ./docker/docker-compose-mongo.yml up
   else
      echo "Illegal argument, usage: start.sh [-mongo-only]"
   fi
else
  docker-compose -f ./docker/docker-compose.yml up
fi
