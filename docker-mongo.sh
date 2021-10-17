# Run a local instance of Mongo for debugging and local development as a Docker daemon (-d) process
mkdir -p "$(pwd)/data"
docker-compose up -d mongodb

# After you have the Daemon running, you can use the Mongo CLI for ad-hoc queries like this:
# docker exec -it MongoDBLocal mongosh
