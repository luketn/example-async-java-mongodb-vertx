# Run a local instance of Mongo for debugging and local development as a Docker daemon (-d) process
mkdir -p "$(pwd)/data"
docker run -d --rm --name MongoDBLocal -p 27017:27017 -v "$(pwd)/data:/etc/mongo" mongo:5.0.3 mongod

# After you have the Daemon running, you can use the Mongo CLI for ad-hoc queries like this:
# docker exec -it MongoDBLocal mongo
