#!/bin/bash

echo "Creating lecturer..."
curl -X POST http://localhost:8080/lecturers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John",
    "surname": "Smith",
    "lecturerId": "PROF001"
  }'

sleep 2

echo "Adding student to lecturer..."
curl -X POST http://localhost:8080/lecturers/PROF001/add \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice",
    "surname": "Johnson",
    "studentId": "STU001"
  }'

echo ""
sleep 2

echo "Getting lecturer with students..."
curl -X GET http://localhost:8080/lecturers/PROF001
