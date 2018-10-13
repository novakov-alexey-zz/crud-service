#!/usr/bin/env bash
docker run --name postgres -p 5432:5432 -e POSTGRES_PASSWORD=ads -e POSTGRES_USER=ads -e POSTGRES_DB=ads -d postgres:10.4