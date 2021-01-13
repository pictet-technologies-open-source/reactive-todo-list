FROM mongo:4.4.3

RUN echo "rs.initiate({_id: 'replset0', members: [{_id: 0, host: '127.0.0.1:27017'}]}); rs.slaveOk();" > /docker-entrypoint-initdb.d/init-replica.js
CMD ["--replSet", "replset0", "--bind_ip", "0.0.0.0", "--oplogSize", "500"]
