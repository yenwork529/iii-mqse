export docker_host=127.0.0.1

export ESD_SILO_MODE=sr

export ENV=dev
export ESD_ENV=dev
#export ESD_REPO=repo.treescale.com/hylinker07
export ESD_REPO=esd-java

export ESD_AGENT_PORT=8060 # New ESD-Collector
export ESD_AUTH_PORT=57000 # the ESD-Auth
export ESD_DNP_PORT=8585 # Outstation Port for Server
export ESD_DNP_EXP_PORT=20005 # Outstation Port for DNP Master
export ESD_MONGO_PORT=27017 # the Mongo
export ESD_MONGO_EXPRESS_PORT=8081 # the Mongo Express
export ESD_SERVER_PORT=58001 # the Backend API
export ESD_TC_PORT=8088 # New Thin Client
export ESD_TAFC_PORT=8089 # New AFC Thin Client
export ESD_WEB_PORT=8092 # the Web Port

export ESD_AUTH_URL=http://${docker_host}:${ESD_AUTH_PORT}/auth/
export ESD_AGENT_URL=http://${docker_host}:${ESD_AGENT_PORT}
export ESD_BACKEND_URL=http://${docker_host}:${ESD_SERVER_PORT}
export ESD_DNP_URL=http://${docker_host}:${ESD_DNP_PORT}
export ESD_MONGO_IP=${docker_host}
export ESD_SERVER_URL=http://${docker_host}:${ESD_SERVER_PORT}/esd/
export ESD_TC_URL=http://${docker_host}:${ESD_TC_PORT}


# to be deprecated
export ESD_QSE_ID=33000033
export ESD_AFC_ID=1
export ESD_TX_GROUP=3
export ESD_RES_ID=3
