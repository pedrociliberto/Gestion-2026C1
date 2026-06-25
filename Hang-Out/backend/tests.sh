#!/bin/bash

# 1. Ejecutar tests unitarios

cd spec
ls | grep _spec | xargs -I {} sh -c 'echo "Archivo: {}"; APP_ENV=dev python3 {}; echo ""'


# 2. Ejecutar tests de gherkin
#cd ..
#behave --tags="not wip"