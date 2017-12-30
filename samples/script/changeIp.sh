#!/bin/bash
sed -e "s/localhost/$1/g" jndi.properties > ../config/jndi.properties | sed -e "s/localhost/$1/g" joramAdmin.xml > ../src/joram/classic/joramAdmin.xml