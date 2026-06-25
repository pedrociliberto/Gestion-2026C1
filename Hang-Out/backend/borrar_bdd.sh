#!/bin/bash

docker exec -it hang-out-db_dev-1 psql -U hangout_usr -d postgres \
-c "DROP DATABASE hangout;" \
-c "CREATE DATABASE hangout;"\