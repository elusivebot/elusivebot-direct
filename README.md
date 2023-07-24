# elusivebot-direct


## Setup

### Python

Run `python3 -m venv venv` to create a virtual environment.  Enable with
`source venv/bin/activate`.  Install supplementary Python tools with
`pip3 install -r requirements.txt` - make sure to source the venv first!


## Deploy

### Build

`./bin/docker` builds the distribution zip and creates the Docker image.
Requires non-sudo access to Docker at the moment.

### Environmental Variables

* `DIRECT_LISTEN_HOST`: IP address to listen on, default is `0.0.0.0`
* `DIRECT_LISTEN_PORT`: port to listen to
* `DIRECT_REDIS_HOST`: hostname of the Redis server
* `DIRECT_REDIS_PORT`: port of the Redis server
* `DIRECT_RABBITMQ_HOST`: hostname of the RabbitMQ exchange
* `DIRECT_RABBITMQ_PORT`: post of the RabbitMQ exchange
