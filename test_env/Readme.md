This is a folder simply to facilitate the setup of a local docker based
Kafka cluster, which will have appd Agent hooks for as well.

It can be used to vet the extension but will also have java agent
instrumentation in them so we can test against a controller numerous
aspects of kafka monitoring with AppD.

1. CD into this directory.
2. Run the command: docker-compose up -f kafka-zoo-cluster -d
3. Download https://kafkatool.com/download.html
4. Install Offset Explorer


docker network create kafkakraft-network